package dev.joid.lib.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileCache {

	private static final Map<String, FileCacheArea> AREA_MAP = new ConcurrentHashMap<>();

	public static @NonNull FileCacheArea area(final @NonNull String name) {
		return FileCache.AREA_MAP.computeIfAbsent(FileCacheArea.check("area", name), FileCacheArea::new);
	}

	public static long clean() {
		final File[] directories = JOIDEnv.getCacheDir().listFiles(directory -> new File(directory, FileCacheArea.MARKER).isFile());
		if (directories == null) {
			return 0L;
		}

		Arrays.sort(directories);
		final long now = System.currentTimeMillis();
		final List<String> details = new ArrayList<>();
		long freed = 0L;
		for (final File directory : directories) {
			final AtomicInteger files = new AtomicInteger();
			freed += FileCache.AREA_MAP.computeIfAbsent(directory.getName(), FileCacheArea::new).clean(now, files);
			if (files.get() > 0) {
				details.add(directory.getName() + ": " + files.get() + (files.get() == 1 ? " file" : " files"));
			}
		}

		if (!details.isEmpty()) {
			JOIDLog.info("The cache freed " + FileCache.describe(freed) + " (" + String.join(", ", details) + ")");
		}
		return freed;
	}

	private static @NonNull String describe(final long bytes) {
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