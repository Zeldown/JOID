package dev.joid.demo.ui.font.pixel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontScale;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;

public class DemoPixelFontTest {

	private static final Color INK = new Color(0.2F, 0.4F, 0.6F, 1F);

	private static DemoPixelFont font;

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@BeforeClass
	public static void load() throws IOException {
		DemoPixelFontTest.font = DemoPixelFont.create(ImageIO.read(JOID.class.getResourceAsStream("/assets/demo/fonts/Pixel/Pixel.png")));
	}

	@Test
	public void isABitmapFontOfEightTexels() {
		Assert.assertTrue(DemoPixelFontTest.font.isBitmap());
		Assert.assertEquals(DemoPixelFont.SIZE, DemoPixelFontTest.font.getBitmapSize());
		Assert.assertSame(DemoPixelFontProvider.inst(), DemoPixelFontTest.font.getFontProvider());
	}

	@Test
	public void sizesATextNodeAtTheScaleOfItsWindow() {
		final TextNode node = TextNode.create(100D, 100D).text(Text.create("Hello", DemoPixelFontTest.info(16F)));
		this.bridges.open(new NodeUI(node));
		final int[][] windows = {{1920, 1080}, {1280, 720}, {480, 270}, {1600, 900}, {3840, 2160}, {960, 540}};
		final double[] widths = {48D, 36D, 96D, 57.6D, 48D, 48D};
		for (int i = 0; i < windows.length; i++) {
			this.bridges.resize(windows[i][0], windows[i][1]).frames(2);
			Assert.assertEquals(windows[i][0] + "x" + windows[i][1], widths[i], node.getWidth(), 1E-4D);
			Assert.assertEquals(windows[i][0] + "x" + windows[i][1], widths[i] * 9D / 24D, node.getHeight(), 1E-4D);
		}
	}

	@Test
	public void drawsEveryTexelOnWholeScreenPixels() {
		this.bridges.open(new NodeUI(TextNode.create(100.3D, 100.6D).text(Text.create("Ag ~", DemoPixelFontTest.info(16F)))));
		final int[][] windows = {{1920, 1080}, {1280, 720}, {1366, 768}, {480, 270}, {1600, 900}};
		final double[] texels = {2D, 1D, 1D, 1D, 2D};
		for (int i = 0; i < windows.length; i++) {
			this.bridges.resize(windows[i][0], windows[i][1]).frames(2);
			final List<Draw> glyphs = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
			Assert.assertEquals(3, glyphs.size());
			for (final Draw glyph : glyphs) {
				final String label = windows[i][0] + "x" + windows[i][1];
				Assert.assertEquals(label, Math.rint(glyph.getLeft()), glyph.getLeft(), 1E-3D);
				Assert.assertEquals(label, Math.rint(glyph.getTop()), glyph.getTop(), 1E-3D);
				Assert.assertEquals(label, texels[i] * 8D, glyph.getRight() - glyph.getLeft(), 1E-3D);
				Assert.assertEquals(label, texels[i] * 8D, glyph.getBottom() - glyph.getTop(), 1E-3D);
			}
			Assert.assertEquals(texels[i] * 6D, glyphs.get(1).getLeft() - glyphs.get(0).getLeft(), 1E-3D);
			Assert.assertEquals(texels[i] * 10D, glyphs.get(2).getLeft() - glyphs.get(1).getLeft(), 1E-3D);
		}
	}

	@Test
	public void centersATextOnItsSnappedWidth() {
		this.bridges.resize(480, 270).open(new NodeUI(TextNode.create(960D, 540D).text(Text.create("Hi", DemoPixelFontTest.info(16F), Align.CENTER)).anchorX(Align.CENTER)));
		this.bridges.frames(2);
		final List<Draw> glyphs = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(236D, glyphs.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(242D, glyphs.get(1).getLeft(), 1E-3D);
	}

	@Test
	public void wrapsAtTheWidthItDraws() {
		final TextInfo info = DemoPixelFontTest.info(16F);
		final List<List<String>> lines = new ArrayList<>();
		FontScale.run(() -> 1D, () -> lines.add(DrawUtils.TEXT.getLines(200D, "aaa aaa aaa", info)));
		FontScale.run(() -> 0.25D, () -> lines.add(DrawUtils.TEXT.getLines(200D, "aaa aaa aaa", info)));
		FontScale.run(() -> 0.25D, () -> lines.add(Arrays.asList(String.valueOf(info.getWidth("aaa aaa")), String.valueOf(info.getHeight()))));
		Assert.assertEquals(Arrays.asList("aaa aaa aaa"), lines.get(0));
		Assert.assertEquals(Arrays.asList("aaa aaa", "aaa"), lines.get(1));
		Assert.assertEquals(Arrays.asList("160.0", "36.0"), lines.get(2));
	}

	@Test
	public void putsTheCaretWhereTheSnappedGlyphsAre() {
		final TextFieldNode wide = TextFieldNode.create(100D, 100D, 600D).info(DemoPixelFontTest.info(16F)).text("abcd");
		this.bridges.open(new NodeUI(wide));
		this.click(154D, 120D);
		Assert.assertEquals(4, wide.getCursorPos());
		this.bridges.resize(480, 270).getClock().advance(1000L);
		this.click(154D / 4D, 120D / 4D);
		Assert.assertEquals(2, wide.getCursorPos());
		this.click(176D / 4D, 120D / 4D);
		Assert.assertEquals(3, wide.getCursorPos());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
	}

	private static TextInfo info(final float size) {
		return TextInfo.create(DemoPixelFontTest.font, size, DemoPixelFontTest.INK);
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}