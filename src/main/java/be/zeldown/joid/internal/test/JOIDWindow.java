package be.zeldown.joid.internal.test;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.draw.text.builder.utils.TextOverflow;
import be.zeldown.joid.lib.draw.text.utils.TextMode;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.utils.align.Align;

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
		GL11.glClearColor(0F, 0F, 0F, 0F);
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
				TextInfo.create(TestFont.MONTSERRAT, 25, Color.WHITE).lineHeight(-5.5F),
				TextInfo.create(TestFont.BATUPHAT, 25, Color.WHITE).lineHeight(-5.5F),
				TextInfo.create(TestFont.SPACE_GROTESK, 25, Color.WHITE).lineHeight(-5.5F)
		};

		final String[] texts = {
				"lorem impsum",
				"italic",
				"spacing",
				"n-spacing",
				"0123456789",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
				"splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text",
				"overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow",
				"colored shadow text",
				"shadow text"
		};

		DrawUtils.RESOURCE.drawResource(0, 0, this.displayMode.getWidth(), this.displayMode.getHeight(), Resource.of("https://images.pexels.com/photos/235985/pexels-photo-235985.jpeg"));

		final double x = 10;
		final double y = 10;
		final double margin = 8.5;
		final double width = this.displayMode.getWidth() - 20;

		double oy = 0;
		for (int i = 0; i < fonts.length; i++) {
			final TextInfo info = fonts[i];
			final Align align = i == 0 ? Align.START : i == 1 ? Align.CENTER : Align.END;
			for (final String text : texts) {
				final boolean italic = text.contains("italic");
				final boolean spacing = text.contains("spacing");
				final boolean negativeSpacing = text.contains("n-spacing");
				final boolean hasShadow = text.contains("shadow");
				final boolean hasColoredShadow = text.contains("colored");
				final boolean split = text.contains("splitted");
				final boolean overflow = text.contains("overflow");

				final TextInfo infoCopy = info.copy().italic(italic).letterSpacing(negativeSpacing ? -4F : spacing ? 10F : 0F).shadow(hasShadow ? hasColoredShadow ? Color.RAINBOW() : Color.BLACK : null);
				oy += DrawUtils.TEXT.drawText(x, y + oy, width, infoCopy.getHeight(), Text.create(text, infoCopy, align).overflow(TextOverflow.ELLIPSIS), split ? TextMode.SPLIT : overflow ? TextMode.OVERFLOW : TextMode.NORMAL).getHeight() + margin;
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