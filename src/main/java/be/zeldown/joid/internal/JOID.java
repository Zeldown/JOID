package be.zeldown.joid.internal;

import org.lwjgl.LWJGLException;

import be.zeldown.joid.internal.test.JOIDWindow;

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

}