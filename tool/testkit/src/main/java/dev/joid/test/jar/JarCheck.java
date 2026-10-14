package dev.joid.test.jar;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;

import dev.joid.internal.JOID;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JarCheck {

	public static void run(final @NonNull String variant, final @NonNull Supplier<Backend<?>> register, final @NonNull Runnable present) {
		final PrintStream console = System.err;
		final ByteArrayOutputStream errors = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errors, true));
		int status = 0;
		try {
			final UI ui = JarCheck.open(variant, register.get(), present);
			if (errors.toString().contains("\tat ")) {
				throw new IllegalStateException("The " + variant + " jar threw while it drew " + ui.getClass().getSimpleName() + ", see the stack traces above");
			}

			System.out.println("[JOID] The " + variant + " jar started JOID and drew " + ui.getClass().getSimpleName());
		} catch (final Throwable throwable) {
			throwable.printStackTrace();
			status = 1;
		}

		System.setErr(console);
		console.print(errors);
		System.exit(status);
	}

	private static UI open(final String variant, final Backend<?> backend, final Runnable present) throws ReflectiveOperationException {
		if ("development".equals(JOID.VERSION)) {
			throw new IllegalStateException("The " + variant + " jar gives no version to JOID: it misses dev/joid/internal/joid.properties or its version was not expanded");
		}

		if (JOIDEnv.isDevMode() == "prod".equals(variant) || JOIDEnv.isDemoMode() != "demo".equals(variant)) {
			throw new IllegalStateException("The " + variant + " jar runs with the dev mode " + JOIDEnv.isDevMode() + " and the demo mode " + JOIDEnv.isDemoMode() + ": check what excludeVariant leaves in it");
		}

		final UI ui = "demo".equals(variant) ? (UI) Class.forName("dev.joid.demo.ui.simple.UIDemoSimple").getDeclaredConstructor().newInstance() : new CheckUI();
		JOID.open(ui);
		for (int frame = 0; frame < 60; frame++) {
			backend.frame();
			present.run();
		}

		if (!JOID.isOpen(ui)) {
			throw new IllegalStateException(ui.getClass().getSimpleName() + " is no longer open after 60 frames of the " + variant + " jar");
		}
		return ui;
	}

	private static final class CheckUI extends UI {

		@Override
		public void init() {
			RectNode.create(40, 40, 200, 120).color(Color.RED).attach(this);
		}

	}

}