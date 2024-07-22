package be.zeldown.joid.internal;

import org.lwjgl.LWJGLException;

import be.zeldown.joid.internal.test.JOIDWindow;

public class JOID {

	public static void main(String[] args) {
		JOIDWindow window;
		try {
			window = JOIDWindow.get();
			window.run();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

}