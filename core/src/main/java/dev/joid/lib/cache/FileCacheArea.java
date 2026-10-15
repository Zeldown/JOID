package dev.joid.lib.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import lombok.Getter;
import lombok.NonNull;

public final class FileCacheArea {

	public static final String MARKER = ".area";

	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

	@Getter private final String      name;
	private final Map<String, Object> lockMap;

	private boolean          declared;
	@Getter private Duration retention;

	public FileCacheArea(final String name) {
		this.name = name;
		this.lockMap = new ConcurrentHashMap<>();
		this.retention = Duration.ofDays(30L);
	}

	public final synchronized @NonNull FileCacheArea retention(final @NonNull Duration retention) {
		if (retention.isNegative() || retention.isZero()) {
			throw new IllegalArgumentException("The retention " + retention + " of the cache area " + this.name + " is not positive, pass a duration above 0");
		}

		if (this.declared && !this.retention.equals(retention)) {
			throw new IllegalStateException("The cache area " + this.name + " is already declared with a retention of " + FileCacheArea.describe(this.retention) + ", it cannot be declared again with " + FileCacheArea.describe(retention) + ": give one of them another name");
		}

		this.declared = true;
		this.retention = retention;

		if (this.getDirectory().isDirectory()) {
			try {
				this.mark();
			} catch (final IOException exception) {
				JOIDLog.devWarnOnce("cache-marker-" + this.name, () -> "The retention of the cache area " + this.name + " cannot be written into " + this.getDirectory().getAbsolutePath() + " (" + exception + "), check that the cache folder can be written");
			}
		}
		return this;
	}

	public @NonNull File getDirectory() {
		return new File(JOIDEnv.getCacheDir(), this.name);
	}

	public File entry(final @NonNull String key) {
		synchronized (this.lock(FileCacheArea.check("entry", key))) {
			final File file = this.find(key);
			if (file != null) {
				FileCacheArea.touch(file);
			}
			return file;
		}
	}

	public @NonNull File compute(final @NonNull String key, final @NonNull IFileCacheWriter writer) throws IOException {
		synchronized (this.lock(FileCacheArea.check("entry", key))) {
			final File file = this.find(key);
			if (file != null) {
				FileCacheArea.touch(file);
				return file;
			}

			return this.publish(key, this.write("the cache entry " + key + " of the area " + this.name, writer));
		}
	}

	public @NonNull File store(final @NonNull IFileCacheWriter writer) throws IOException {
		final File temporary = this.write("a content entry of the cache area " + this.name, writer);
		final String key;
		try {
			key = FileCacheArea.hash(temporary);
		} catch (final IOException exception) {
			FileUtils.deleteQuietly(temporary);
			throw new IOException("The content written into the cache area " + this.name + " cannot be hashed at " + temporary.getAbsolutePath() + ", the writer of store must write a file", exception);
		}

		synchronized (this.lock(key)) {
			return this.publish(key, temporary);
		}
	}

	public boolean remove(final @NonNull String key) {
		synchronized (this.lock(FileCacheArea.check("entry", key))) {
			final File[] files = this.list(key);
			for (final File file : files) {
				if (this.delete(file) < 0L) {
					file.setLastModified(0L);
					JOIDLog.devWarnOnce("cache-remove-" + file.getAbsolutePath(), () -> "The cache entry " + file.getName() + " of the area " + this.name + " is in use, it is left to the next cleaning of the cache");
				}
			}
			return files.length > 0;
		}
	}

	public long clean(final long now, final AtomicInteger files) {
		final File[] entries = this.getDirectory().listFiles();
		if (entries == null) {
			return 0L;
		}

		final long retention = FileCacheArea.readRetention(this.getDirectory()).toMillis();
		long freed = 0L;
		for (final File entry : entries) {
			if (entry.getName().equals(FileCacheArea.MARKER)) {
				continue;
			}

			final boolean temporary = entry.getName().startsWith(".") && entry.getName().endsWith(".tmp");
			final long size = temporary ? this.expire(entry, now - TimeUnit.DAYS.toMillis(1L)) : this.expireEntry(entry, now - retention);
			if (size >= 0L) {
				freed += size;
				files.incrementAndGet();
			}
		}
		return freed;
	}

	private long expireEntry(final File entry, final long limit) {
		final int separator = entry.getName().indexOf('~');
		synchronized (this.lock(separator < 0 ? entry.getName() : entry.getName().substring(0, separator))) {
			return this.expire(entry, limit);
		}
	}

	private long expire(final File entry, final long limit) {
		return entry.lastModified() < limit ? this.delete(entry) : -1L;
	}

	private long delete(final File entry) {
		final File deleted = this.newTemporary();
		try {
			Files.move(entry.toPath(), deleted.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (final IOException exception) {
			return -1L;
		}

		final long size = FileUtils.sizeOf(deleted);
		FileUtils.deleteQuietly(deleted);
		return size;
	}

	private File find(final String key) {
		File found = null;
		for (final File file : this.list(key)) {
			if (file.lastModified() != 0L && (found == null || FileCacheArea.generation(key, file) > FileCacheArea.generation(key, found))) {
				found = file;
			}
		}
		return found;
	}

	private @NonNull File[] list(final String key) {
		final File[] files = this.getDirectory().listFiles((parent, name) -> name.equals(key) || (name.startsWith(key + "~") && name.substring(key.length() + 1).matches("[0-9]+")));
		return files != null ? files : new File[0];
	}

	private @NonNull File write(final String description, final IFileCacheWriter writer) throws IOException {
		final File directory = this.getDirectory();
		try {
			Files.createDirectories(directory.toPath());
			this.mark();
		} catch (final IOException exception) {
			throw new IOException("The cache area " + this.name + " cannot be created in " + directory.getAbsolutePath() + ", check that the cache folder can be written", exception);
		}

		final File temporary = this.newTemporary();
		try {
			writer.write(temporary);
		} catch (final IOException | RuntimeException exception) {
			FileUtils.deleteQuietly(temporary);
			throw exception;
		}

		if (!temporary.exists()) {
			throw new IOException("The writer of " + description + " wrote nothing at " + temporary.getAbsolutePath() + ", it must create the file or the folder it is given");
		}
		return temporary;
	}

	private @NonNull File publish(final String key, final File temporary) throws IOException {
		final File existing = this.find(key);
		if (existing != null) {
			FileUtils.deleteQuietly(temporary);
			FileCacheArea.touch(existing);
			return existing;
		}

		int generation = -1;
		for (final File file : this.list(key)) {
			generation = Math.max(generation, FileCacheArea.generation(key, file));
		}

		final File file = new File(this.getDirectory(), generation < 0 ? key : key + "~" + (generation + 1));
		try {
			Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (final IOException exception) {
			FileUtils.deleteQuietly(temporary);
			if (!file.exists()) {
				throw exception;
			}
		}

		FileCacheArea.touch(file);
		return file;
	}

	private void mark() throws IOException {
		final File marker = new File(this.getDirectory(), FileCacheArea.MARKER);
		final String content = this.declared ? this.retention.toString() : "";
		if (!marker.isFile() || (this.declared && !content.equals(new String(Files.readAllBytes(marker.toPath()), StandardCharsets.UTF_8)))) {
			Files.write(marker.toPath(), content.getBytes(StandardCharsets.UTF_8));
		}
	}

	private @NonNull File newTemporary() {
		return new File(this.getDirectory(), "." + UUID.randomUUID() + ".tmp");
	}

	private @NonNull Object lock(final String key) {
		return this.lockMap.computeIfAbsent(key, ignored -> new Object());
	}

	public static @NonNull String check(final String kind, final String name) {
		if (!FileCacheArea.NAME.matcher(name).matches()) {
			throw new IllegalArgumentException("The cache " + kind + " name " + name + " is not a plain file name: start with a letter or a digit, then use only letters, digits, '.', '-' and '_'");
		}
		return name;
	}

	private static @NonNull Duration readRetention(final File directory) {
		try {
			final Duration retention = Duration.parse(new String(Files.readAllBytes(new File(directory, FileCacheArea.MARKER).toPath()), StandardCharsets.UTF_8).trim());
			return retention.isNegative() || retention.isZero() ? Duration.ofDays(30L) : retention;
		} catch (final IOException | DateTimeParseException exception) {
			return Duration.ofDays(30L);
		}
	}

	private static int generation(final String key, final File file) {
		return file.getName().equals(key) ? 0 : Integer.parseInt(file.getName().substring(key.length() + 1));
	}

	private static void touch(final File file) {
		file.setLastModified(System.currentTimeMillis());
	}

	private static @NonNull String hash(final File file) throws IOException {
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (final NoSuchAlgorithmException exception) {
			throw new IllegalStateException("The file cache cannot name the content of " + file.getAbsolutePath() + ": this JVM has no SHA-1 digest, run JOID on a standard JVM", exception);
		}

		try (final InputStream stream = Files.newInputStream(file.toPath())) {
			final byte[] buffer = new byte[65536];
			for (int read = stream.read(buffer); read != -1; read = stream.read(buffer)) {
				digest.update(buffer, 0, read);
			}
		}
		return String.format(Locale.ROOT, "%040x", new BigInteger(1, digest.digest()));
	}

	private static @NonNull String describe(final Duration duration) {
		if (duration.toDays() > 0L && duration.equals(Duration.ofDays(duration.toDays()))) {
			return duration.toDays() + (duration.toDays() == 1L ? " day" : " days");
		}

		if (duration.toHours() > 0L && duration.equals(Duration.ofHours(duration.toHours()))) {
			return duration.toHours() + (duration.toHours() == 1L ? " hour" : " hours");
		}

		if (duration.toMinutes() > 0L && duration.equals(Duration.ofMinutes(duration.toMinutes()))) {
			return duration.toMinutes() + (duration.toMinutes() == 1L ? " minute" : " minutes");
		}
		return duration.toMillis() + " ms";
	}

}