package dev.joid.lib.utils.platform;

import java.util.Locale;

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

}