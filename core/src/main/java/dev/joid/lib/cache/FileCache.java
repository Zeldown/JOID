package dev.joid.lib.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.JOIDEnv;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileCache {

	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

	public static long clean() {
		final File[] directories = JOIDEnv.getCacheDir().listFiles(directory -> new File(directory, FileCacheArea.MARKER).isFile() && FileCache.NAME.matcher(directory.getName()).matches());
		if (directories == null) {
			return 0L;
		}

		Arrays.sort(directories);
		final long now = System.currentTimeMillis();
		final List<String> details = new ArrayList<>();
		long freed = 0L;
		for (final File directory : directories) {
			final AtomicInteger files = new AtomicInteger();
			freed += FileCacheArea.of(directory.getName()).clean(now, files);
			if (files.get() > 0) {
				details.add(directory.getName() + ": " + files.get() + (files.get() == 1 ? " file" : " files"));
			}
		}

		if (!details.isEmpty()) {
			JOIDLog.info("The cache freed " + FileCache.describe(freed) + " (" + String.join(", ", details) + ")");
		}
		return freed;
	}

	private static String describe(final long bytes) {
		if (bytes >= 1L << 30) {
			return String.format(Locale.ROOT, "%.1f GB", bytes / (double) (1L << 30));
		}

		if (bytes >= 1L << 20) {
			return String.format(Locale.ROOT, "%.1f MB", bytes / (double) (1L << 20));
		}

		if (bytes >= 1L << 10) {
			return String.format(Locale.ROOT, "%.1f KB", bytes / (double) (1L << 10));
		}
		return bytes + " B";
	}

}