package com.example.running;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.cache.FileCacheArea;

public final class RunningFolders {

	// #region area
	private static final FileCacheArea REPORTS = FileCache.area("reports").retention(Duration.ofDays(7L));
	// #endregion

	private RunningFolders() {}

	public static Lwjgl3Backend register(final long window) {
		// #region folders
		final File home = new File(System.getProperty("user.home"), ".my-application");
		return Lwjgl3Backend
		.create(window)
		.dataDir(new File(home, "data"))
		.cacheDir(new File(home, "cache"))
		.register();
		// #endregion
	}

	public static void print() {
		// #region read
		System.out.println("Settings in " + JOIDEnv.getDataDir().getAbsolutePath());
		System.out.println("Caches in " + JOIDEnv.getCacheDir().getAbsolutePath());
		// #endregion
	}

	// #region compute
	public static File report(final String day) throws IOException {
		return RunningFolders.REPORTS.compute(day, file -> {
			final String report = RunningFolders.build(day);
			Files.write(file.toPath(), report.getBytes(StandardCharsets.UTF_8));
		});
	}
	// #endregion

	private static String build(final String day) {
		return "Report of " + day;
	}

}