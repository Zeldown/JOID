package be.zeldown.joid.internal;

import java.io.InputStream;

import org.lwjgl.LWJGLException;

import be.zeldown.joid.internal.test.JOIDWindow;
import lombok.NonNull;

public class JOID {

	public static void main(final String[] args) {
		JOIDWindow window;
		try {
			window = JOIDWindow.get();
			window.run();
		} catch (final LWJGLException e) {
			e.printStackTrace();
		}
	}

	public static @NonNull InputStream get(final @NonNull String path) {
		return JOID.class.getResourceAsStream(path);
	}

}