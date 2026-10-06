package dev.joid.lib.ui.node.impl.design.textfield;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

public class MultilineTextFieldNodeTest {

	private static final IFont FONT = () -> MultilineTextFieldNodeTest.PROVIDER;

	private static final IFontProvider PROVIDER = new IFontProvider() {

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			return new FontBounds(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			return text.length() * info.getFontSize();
		}

		@Override
		public double getHeight(final String text, final TextInfo info) {
			return info.getFontSize() * 2D;
		}

		@Override
		public double getLineHeight(final TextInfo info) {
			return info.getFontSize() * 2D;
		}

	};

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void pastesAWindowsLineEndingAsOneLineBreak() {
		final FieldUI ui = new FieldUI("");
		this.bridges.open(ui);
		this.bridges.getWindow().setClipboard("ab\r\ncd");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		ui.field.keyPressed('v', Key.V, InternalContext.create());
		Assert.assertEquals("ab\ncd", ui.field.getText());
	}

	@Test
	public void movesDownPastAWindowsLineEnding() {
		final FieldUI ui = new FieldUI("ab\r\ncd");
		this.bridges.open(ui);
		ui.field.cursorPosition(1);
		ui.field.keyPressed(' ', Key.DOWN, InternalContext.create());
		Assert.assertEquals(6, ui.field.getCursorPos());
	}

	public static final class FieldUI extends UI {

		private final String text;

		private MultilineTextFieldNode field;

		private FieldUI(final String text) {
			this.text = text;
		}

		@Override
		public void init() {
			this.field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(MultilineTextFieldNodeTest.FONT, 10F)).text(this.text).focused(true);
			this.field.attach(this);
		}

	}

}