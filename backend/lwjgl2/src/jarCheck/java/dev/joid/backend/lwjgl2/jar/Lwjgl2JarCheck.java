package dev.joid.backend.lwjgl2.jar;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Lwjgl2Backend;
import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.platform.Platform;
import dev.joid.test.jar.JarCheck;
import dev.joid.test.snapshot.SnapshotWindowBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2JarCheck {

	public static void main(final String[] arguments) {
		JarCheck.run(arguments[0], () -> {
			Lwjgl2Natives.install();
			final PixelFormat format = new PixelFormat().withDepthBits(24).withStencilBits(8);
			try {
				if (Platform.current() == Platform.LINUX) {
					Display.setDisplayMode(new DisplayMode(640, 480));
					Display.create(format);
				} else {
					new Pbuffer(640, 480, format, null).makeCurrent();
				}
			} catch (final LWJGLException e) {
				throw new IllegalStateException("Unable to create the LWJGL 2 context", e);
			}

			final Lwjgl2Backend backend = Lwjgl2Backend.create().audio(false).register();
			final SnapshotWindowBridge window = new SnapshotWindowBridge();
			window.setWidth(640);
			window.setHeight(480);
			BridgeHandler.WINDOW.register(window);
			return backend;
		}, () -> {});
	}

}