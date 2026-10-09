package dev.joid.demo.ui.font.pixel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
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
	public void sizesATextNodeAtItsFontSizeInEveryWindow() {
		final TextNode node = TextNode.create(100D, 100D).text(Text.create("Hello", DemoPixelFontTest.info(16F)));
		this.bridges.open(new NodeUI(node));
		final int[][] windows = {{1920, 1080}, {1280, 720}, {480, 270}, {1600, 900}, {2560, 1369}, {3840, 2160}};
		for (final int[] window : windows) {
			this.bridges.resize(window[0], window[1]).frames(2);
			Assert.assertEquals(window[0] + "x" + window[1], 48D, node.getWidth(), 1E-4D);
			Assert.assertEquals(window[0] + "x" + window[1], 18D, node.getHeight(), 1E-4D);
		}
	}

	@Test
	public void measuresEverySizeApart() {
		final float[] sizes = {8F, 12F, 16F, 20F, 32F};
		final double[] widths = new double[sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			widths[i] = DemoPixelFontTest.info(sizes[i]).getWidth("Size");
		}
		for (int i = 0; i < sizes.length; i++) {
			Assert.assertEquals(widths[0] * sizes[i] / sizes[0], widths[i], 1E-4D);
		}
	}

	@Test
	public void drawsEveryTexelOnWholeScreenPixelsAtAWholeScale() {
		this.bridges.open(new NodeUI(TextNode.create(100.3D, 100.6D).text(Text.create("Ag ~", DemoPixelFontTest.info(16F)))));
		final int[][] windows = {{1920, 1080}, {960, 540}, {3840, 2160}};
		final double[] texels = {2D, 1D, 4D};
		for (int i = 0; i < windows.length; i++) {
			this.bridges.resize(windows[i][0], windows[i][1]).frames(2);
			final List<Draw> glyphs = this.glyphs();
			final String label = windows[i][0] + "x" + windows[i][1];
			Assert.assertEquals(label, 3, glyphs.size());
			for (final Draw glyph : glyphs) {
				Assert.assertEquals(label, Math.rint(glyph.getLeft()), glyph.getLeft(), 1E-3D);
				Assert.assertEquals(label, Math.rint(glyph.getTop()), glyph.getTop(), 1E-3D);
				Assert.assertEquals(label, texels[i] * 8D + 2D, glyph.getRight() - glyph.getLeft(), 1E-3D);
				Assert.assertEquals(label, texels[i] * 8D + 2D, glyph.getBottom() - glyph.getTop(), 1E-3D);
			}
			Assert.assertEquals(label, texels[i] * 6D, glyphs.get(1).getLeft() - glyphs.get(0).getLeft(), 1E-3D);
			Assert.assertEquals(label, texels[i] * 10D, glyphs.get(2).getLeft() - glyphs.get(1).getLeft(), 1E-3D);
			Assert.assertArrayEquals(label, new float[] {(float) (1D / texels[i]), (float) (1D / texels[i])}, (float[]) this.shader().getValues().get("pixel"), 1E-6F);
		}
	}

	@Test
	public void givesEveryTexelTheSameWidthAtAFractionalScale() {
		this.bridges.open(new NodeUI(TextNode.create(100.3D, 100.6D).text(Text.create("MMMMMM", DemoPixelFontTest.info(16F)))));
		final int[][] windows = {{2560, 1369}, {1996, 1123}, {1366, 768}, {960, 540}, {480, 270}};
		for (final int[] window : windows) {
			this.bridges.resize(window[0], window[1]).frames(2);
			final double scale = Math.min(window[0] / 1920D, window[1] / 1080D);
			final List<Draw> glyphs = this.glyphs();
			final String label = window[0] + "x" + window[1];
			Assert.assertEquals(label, 6, glyphs.size());
			Assert.assertEquals(label, Math.rint(glyphs.get(0).getLeft()), glyphs.get(0).getLeft(), 1E-3D);
			for (int i = 0; i < glyphs.size(); i++) {
				Assert.assertEquals(label, 16D * scale + 2D, glyphs.get(i).getRight() - glyphs.get(i).getLeft(), 1E-3D);
				Assert.assertEquals(label, 16D * scale + 2D, glyphs.get(i).getBottom() - glyphs.get(i).getTop(), 1E-3D);
				Assert.assertEquals(label, glyphs.get(0).getTop(), glyphs.get(i).getTop(), 1E-3D);
				Assert.assertEquals(label, glyphs.get(0).getLeft() + i * 12D * scale, glyphs.get(i).getLeft(), 1E-3D);
			}
			final float pixel = (float) (DemoPixelFont.SIZE / (16D * scale));
			Assert.assertArrayEquals(label, new float[] {pixel, pixel}, (float[]) this.shader().getValues().get("pixel"), 1E-5F);
		}
	}

	@Test
	public void boundsTheSamplingToTheCellOfTheGlyph() {
		this.bridges.open(new NodeUI(TextNode.create(100D, 100D).text(Text.create("A", DemoPixelFontTest.info(16F)))));
		this.bridges.frames(2);
		final float[] bounds = {DemoPixelFontFace.cellX('A'), DemoPixelFontFace.cellY('A'), DemoPixelFontFace.cellX('A') + 8F, DemoPixelFontFace.cellY('A') + 8F};
		Assert.assertArrayEquals(bounds, (float[]) this.shader().getValues().get("bounds"), 0F);
		Assert.assertArrayEquals(new float[] {1F / 128F, 1F / 48F}, (float[]) this.shader().getValues().get("texel"), 0F);
	}

	@Test
	public void wrapsAtItsFontSizeInEveryWindow() {
		final TextInfo info = DemoPixelFontTest.info(16F);
		for (final int[] window : new int[][] {{1920, 1080}, {480, 270}, {2560, 1369}}) {
			this.bridges.resize(window[0], window[1]);
			Assert.assertEquals(Arrays.asList("aaa aaa", "aaa"), DrawUtils.TEXT.getLines(info.getWidth("aaa aaa"), "aaa aaa aaa", info));
		}
	}

	@Test
	public void putsTheCaretAtTheSamePlaceInEveryWindow() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 600D).info(DemoPixelFontTest.info(16F)).text("abcd");
		this.bridges.open(new NodeUI(field));
		this.click(1D, 124D, 120D);
		final int caret = field.getCursorPos();
		this.bridges.resize(480, 270).getClock().advance(1000L);
		this.click(0.25D, 124D, 120D);
		Assert.assertEquals(caret, field.getCursorPos());
		this.bridges.resize(2560, 1369).getClock().advance(1000L);
		this.click(1369D / 1080D, 124D, 120D);
		Assert.assertEquals(caret, field.getCursorPos());
	}

	private List<Draw> glyphs() {
		final RecordingShader shader = this.shader();
		return this.bridges.getRender().getDraws().stream().filter(draw -> draw.getShader() == shader).collect(Collectors.toList());
	}

	private RecordingShader shader() {
		for (final Draw draw : this.bridges.getRender().getDraws()) {
			if (draw.getShader() instanceof RecordingShader && ((RecordingShader) draw.getShader()).getValues().containsKey("bounds")) {
				return (RecordingShader) draw.getShader();
			}
		}
		throw new IllegalStateException("No glyph was drawn");
	}

	private void click(final double scale, final double x, final double y) {
		this.bridges.move(x * scale, y * scale).frames(2);
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