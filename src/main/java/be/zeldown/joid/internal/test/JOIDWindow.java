package be.zeldown.joid.internal.test;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.text.TextAlign;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.font.dto.text.TextOverflow;

public class JOIDWindow {

	private static JOIDWindow instance;

	private final DisplayMode displayMode;

	private JOIDWindow() throws LWJGLException {
		this.displayMode = Display.getAvailableDisplayModes()[0];
		Display.setDisplayMode(this.displayMode);
		Display.create();
		Display.setTitle("JOID - Demo");
	}

	public void run() {
		this.init();
		this.loop();
	}

	public void init() {
		TestFont.load();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, this.displayMode.getWidth(), this.displayMode.getHeight(), 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, this.displayMode.getWidth(), this.displayMode.getHeight());
		GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
	}

	public void loop() {
		while (!Display.isCloseRequested()) {
			Display.update();

			if (!Display.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException inte) {
					inte.printStackTrace();
				}
				continue;
			}

			this.render();
			Display.sync(60);
		}

		Display.destroy();
		System.exit(0);
	}

	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		final TextInfo[] fonts = {
				TextInfo.create(TestFont.MONTSERRAT, 25, Color.WHITE).align(TextAlign.LEFT).lineHeight(-5.5F),
				TextInfo.create(TestFont.BATUPHAT, 25, Color.WHITE).align(TextAlign.CENTER).lineHeight(-5.5F),
				TextInfo.create(TestFont.SPACE_GROTESK, 25, Color.WHITE).align(TextAlign.RIGHT)
		};

		final String[] texts = {
				"lorem impsum",
				"§oitalic",
				"spacing",
				"n-spacing",
				"§00 §11 §22 §33 §44 §55 §66 §77 §88 §99 §aa §bb §cc §dd §ee §ff §pp",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
				"splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text",
				"overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow",
				"colored shadow text",
				"§cshadow §ltext"
		};

		final double x = 10;
		final double y = 10;
		final double margin = 8.5;
		final double width = this.displayMode.getWidth() - 20;

		double oy = 0;
		for (final TextInfo info : fonts) {
			final double ox = info.getAlign() == TextAlign.LEFT ? 0 : info.getAlign() == TextAlign.CENTER ? width / 2 : width;
			for (final String text : texts) {
				final boolean spacing = text.contains("spacing");
				final boolean negativeSpacing = text.contains("n-spacing");
				final boolean hasShadow = text.contains("shadow");
				final boolean hasColoredShadow = text.contains("colored");
				final boolean split = text.contains("splitted");
				final boolean overflow = text.contains("overflow");

				final TextInfo infoCopy = info.copy().letterSpacing(negativeSpacing ? -4F : spacing ? 10F : 0F).shadow(hasShadow ? hasColoredShadow ? Color.RAINBOW() : Color.BLACK : null);
				if (split) {
					DrawUtils.TEXT.drawText(x + ox, y + oy, width, text, infoCopy);
					oy += DrawUtils.TEXT.getLines(width, text, infoCopy).size() * info.getHeight() + margin;
				} else {
					DrawUtils.TEXT.drawText(x + ox, y + oy, width, text, infoCopy, overflow ? TextOverflow.ELLIPSIS : null);
					oy += info.getHeight() + margin;
				}
			}
		}
	}

	public static JOIDWindow get() throws LWJGLException {
		if (JOIDWindow.instance == null) {
			JOIDWindow.instance = new JOIDWindow();
		}

		return JOIDWindow.instance;
	}

}