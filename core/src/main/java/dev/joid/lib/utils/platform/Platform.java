package dev.joid.lib.utils.platform;

import java.io.File;
import java.util.Locale;
import java.util.function.Function;

import lombok.NonNull;

public enum Platform {

	WINDOWS,
	MACOS,
	LINUX;

	public static @NonNull Platform current() {
		final String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (name.contains("mac") || name.contains("darwin")) {
			return Platform.MACOS;
		}
		return name.contains("win") ? Platform.WINDOWS : Platform.LINUX;
	}

	public static boolean is64Bit() {
		return System.getProperty("os.arch", "").contains("64");
	}

	public @NonNull File getCacheDir(final @NonNull String home, final @NonNull Function<@NonNull String, String> environment) {
		if (this == Platform.WINDOWS) {
			final String local = environment.apply("LOCALAPPDATA");
			return local != null ? new File(local) : new File(home, "AppData/Local");
		}

		if (this == Platform.MACOS) {
			return new File(home, "Library/Caches");
		}

		final String cache = environment.apply("XDG_CACHE_HOME");
		return cache != null ? new File(cache) : new File(home, ".cache");
	}

}