package dev.joid.lib;

import java.io.File;

import dev.joid.lib.utils.platform.Platform;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JOIDEnv {

	private static final boolean DEV  = JOIDEnv.class.getResource("/dev/joid/lib/ui/node/impl/dev/DevNode.class") != null;
	private static final boolean DEMO = JOIDEnv.class.getResource("/dev/joid/demo/DemoFont.class") != null;

	@Getter private static boolean devMode  = JOIDEnv.DEV;
	@Getter private static boolean demoMode = JOIDEnv.DEMO;

	@Getter private static File dataDir  = JOIDEnv.locateDataDir();
	@Getter private static File cacheDir = JOIDEnv.locateCacheDir();

	public static void devMode(final boolean devMode) {
		if (devMode && !JOIDEnv.DEV) {
			throw new IllegalStateException("The dev mode needs the dev classes of JOID, use its dev or demo jar");
		}

		JOIDEnv.devMode = devMode;
	}

	public static void dataDir(final @NonNull File dataDir) {
		JOIDEnv.dataDir = dataDir;
	}

	public static void cacheDir(final @NonNull File cacheDir) {
		JOIDEnv.cacheDir = cacheDir;
	}

	private static File locateDataDir() {
		final String property = System.getProperty("joid.data");
		return property != null ? new File(property) : new File("joid");
	}

	private static File locateCacheDir() {
		final String property = System.getProperty("joid.cache");
		return property != null ? new File(property) : new File(Platform.current().getCacheDir(System.getProperty("user.home"), System::getenv), "joid");
	}

}