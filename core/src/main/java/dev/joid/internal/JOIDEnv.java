package dev.joid.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JOIDEnv {

	private static final boolean DEV  = JOIDEnv.class.getResource("/dev/joid/lib/ui/node/impl/dev/DevNode.class") != null;
	private static final boolean DEMO = JOIDEnv.class.getResource("/dev/joid/demo/DemoFont.class") != null;

	@Getter private static boolean devMode  = JOIDEnv.DEV;
	@Getter private static boolean demoMode = JOIDEnv.DEMO;

	public static void devMode(final boolean devMode) {
		if (devMode && !JOIDEnv.DEV) {
			throw new IllegalStateException("The dev mode needs the dev classes of JOID, use its dev or demo jar");
		}

		JOIDEnv.devMode = devMode;
	}

	public static void demoMode(final boolean demoMode) {
		if (demoMode && !JOIDEnv.DEMO) {
			throw new IllegalStateException("The demo mode needs the demo classes of JOID, use its demo jar");
		}

		JOIDEnv.demoMode = demoMode;
	}

}