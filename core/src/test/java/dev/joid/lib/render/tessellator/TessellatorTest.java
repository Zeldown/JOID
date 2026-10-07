package dev.joid.lib.render.tessellator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.vertex.DrawMode;

public class TessellatorTest {

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@Test
	public void sharesOneInstance() {
		Assert.assertSame(Tessellator.inst(), Tessellator.inst());
	}

	@Test
	public void copiesAnIndependentTessellator() {
		final Tessellator copy = Tessellator.inst().copy();
		copy.start(DrawMode.TRIANGLES);
		Assert.assertNotSame(Tessellator.inst(), copy);
		Assert.assertTrue(copy.isDrawing());
		Assert.assertFalse(Tessellator.inst().isDrawing());
		copy.draw();
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToStartTwice() {
		final Tessellator tessellator = Tessellator.inst().copy();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.start(DrawMode.TRIANGLES);
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToDrawWithoutStarting() {
		Tessellator.inst().copy().draw();
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingMode() {
		Tessellator.inst().copy().start(null);
	}

	@Test
	public void drawsTrianglesAsTheyCome() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.addVertex(1D, 2D, 3D);
		tessellator.addVertex(4D, 5D, 6D);
		tessellator.addVertex(7D, 8D, 9D);
		tessellator.draw();

		final Capture capture = this.single();
		Assert.assertSame(DrawMode.TRIANGLES, capture.getMode());
		Assert.assertEquals(3, capture.getCount());
		Assert.assertEquals(4F, capture.getX(1), 0F);
		Assert.assertEquals(8F, capture.getY(2), 0F);
		Assert.assertEquals(3F, capture.getZ(0), 0F);
		Assert.assertFalse(capture.isTexture());
		Assert.assertFalse(capture.isColor());
		Assert.assertFalse(capture.isNormal());
		Assert.assertFalse(tessellator.isDrawing());
		Assert.assertEquals(0, tessellator.getVertexCount());
	}

	@Test
	public void splitsEachQuadIntoTwoTriangles() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.quads();
		for (int i = 0; i < 6; i++) {
			tessellator.addVertex(i, 0D, 0D);
		}
		tessellator.draw();

		final Capture capture = this.single();
		Assert.assertSame(DrawMode.TRIANGLES, capture.getMode());
		Assert.assertArrayEquals(new float[] {0F, 1F, 2F, 0F, 2F, 3F}, TessellatorTest.xs(capture), 0F);
	}

	@Test
	public void fansAPolygonFromItsFirstVertex() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.POLYGON);
		for (int i = 0; i < 5; i++) {
			tessellator.addVertex(i, 0D, 0D);
		}
		tessellator.draw();

		final Capture capture = this.single();
		Assert.assertSame(DrawMode.TRIANGLES, capture.getMode());
		Assert.assertArrayEquals(new float[] {0F, 1F, 2F, 0F, 2F, 3F, 0F, 3F, 4F}, TessellatorTest.xs(capture), 0F);
	}

	@Test
	public void drawsLinesAsTheyCome() {
		final Capture capture = this.lines(DrawMode.LINES, 4);
		Assert.assertSame(DrawMode.LINES, capture.getMode());
		Assert.assertArrayEquals(new float[] {0F, 1F, 2F, 3F}, TessellatorTest.xs(capture), 0F);
	}

	@Test
	public void joinsALineStripIntoSegments() {
		final Capture capture = this.lines(DrawMode.LINE_STRIP, 3);
		Assert.assertSame(DrawMode.LINES, capture.getMode());
		Assert.assertArrayEquals(new float[] {0F, 1F, 1F, 2F}, TessellatorTest.xs(capture), 0F);
	}

	@Test
	public void closesALineLoop() {
		final Capture capture = this.lines(DrawMode.LINE_LOOP, 3);
		Assert.assertSame(DrawMode.LINES, capture.getMode());
		Assert.assertArrayEquals(new float[] {0F, 1F, 1F, 2F, 2F, 0F}, TessellatorTest.xs(capture), 0F);
	}

	@Test
	public void drawsNothingWithoutEnoughVertices() {
		final Tessellator tessellator = Tessellator.inst();
		for (final DrawMode mode : new DrawMode[] {DrawMode.POLYGON, DrawMode.LINE_STRIP, DrawMode.LINE_LOOP, DrawMode.QUADS, DrawMode.TRIANGLES}) {
			tessellator.start(mode);
			if (mode != DrawMode.TRIANGLES) {
				tessellator.addVertex(1D, 2D, 0D);
			}
			tessellator.draw();
			Assert.assertFalse(tessellator.isDrawing());
		}
		Assert.assertTrue(this.render.getCaptures().isEmpty());
	}

	@Test
	public void expandsSmoothLinesIntoQuads() {
		this.render.lineSmooth(true);
		this.render.lineWidth(3F);
		final Capture capture = this.lines(DrawMode.LINE_STRIP, 3);
		Assert.assertSame(DrawMode.TRIANGLES, capture.getMode());
		Assert.assertEquals(12, capture.getCount());
		Assert.assertTrue(capture.isTexture());
		Assert.assertTrue(capture.isNormal());
		Assert.assertArrayEquals(new float[] {0F, 0F, 1F, 0F, 1F, 1F, 1F, 1F, 2F, 1F, 2F, 2F}, TessellatorTest.xs(capture), 0F);
		Assert.assertEquals(1F, capture.getU(0), 0F);
		Assert.assertEquals(0F, capture.getU(2), 0F);
		Assert.assertEquals(0x8181, capture.getNormal(0));
		Assert.assertEquals(0x817F, capture.getNormal(1));
		Assert.assertEquals(0x7F7F, capture.getNormal(2));
		Assert.assertEquals(0x7F81, capture.getNormal(5));
	}

	@Test
	public void shadesSmoothLinesWithTheLineShader() {
		this.render.lineSmooth(true);
		this.render.lineWidth(3F);
		final Capture capture = this.lines(DrawMode.LINES, 2);
		Assert.assertTrue(capture.getState().getShader() instanceof RecordingShader);
		Assert.assertEquals(3F, (Float) capture.getUniforms().get("u_Width"), 0F);
		Assert.assertArrayEquals(new float[] {1920F, 1080F}, (float[]) capture.getUniforms().get("u_Viewport"), 0F);
		Assert.assertNull(this.render.getShader());
	}

	@Test
	public void expandsEverySegmentOfASmoothLoop() {
		this.render.lineSmooth(true);
		final Capture capture = this.lines(DrawMode.LINE_LOOP, 3);
		Assert.assertEquals(18, capture.getCount());
		Assert.assertEquals(2F, capture.getX(12), 0F);
		Assert.assertEquals(0F, capture.getX(14), 0F);
	}

	@Test
	public void expandsSeparateSmoothLines() {
		this.render.lineSmooth(true);
		final Capture capture = this.lines(DrawMode.LINES, 4);
		Assert.assertEquals(12, capture.getCount());
		Assert.assertEquals(2F, capture.getX(6), 0F);
		Assert.assertEquals(3F, capture.getX(8), 0F);
	}

	@Test
	public void keepsHardLinesUnderAnotherShader() {
		final RecordingShader shader = new RecordingShader();
		this.render.lineSmooth(true);
		this.render.shader(shader);
		final Capture capture = this.lines(DrawMode.LINE_STRIP, 3);
		Assert.assertSame(DrawMode.LINES, capture.getMode());
		Assert.assertEquals(4, capture.getCount());
		Assert.assertSame(shader, this.render.getShader());
	}

	@Test
	public void writesTheColorAsRgbaBytes() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setColor(0x336699);
		TessellatorTest.triangle(tessellator);

		final Capture capture = this.single();
		Assert.assertTrue(capture.isColor());
		Assert.assertArrayEquals(new byte[] {0x33, 0x66, (byte) 0x99, (byte) 0xFF}, TessellatorTest.bytes(capture.getColor(0)));
	}

	@Test
	public void readsEveryColorFormat() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setColor(0x102030, 0x40);
		tessellator.addVertex(0D, 0D, 0D);
		tessellator.setColor((byte) 0x50, (byte) 0x60, (byte) 0xF0);
		tessellator.addVertex(1D, 0D, 0D);
		tessellator.setColor(0.5F, 0.25F, 1F);
		tessellator.addVertex(1D, 1D, 0D);
		tessellator.setColor(0F, 1F, 0.5F, 0.5F);
		tessellator.addVertex(0D, 1D, 0D);
		tessellator.setColor(7, 8, 9);
		tessellator.addVertex(0D, 2D, 0D);
		tessellator.addVertex(1D, 2D, 0D);
		tessellator.draw();

		final Capture capture = this.single();
		Assert.assertArrayEquals(new byte[] {0x10, 0x20, 0x30, 0x40}, TessellatorTest.bytes(capture.getColor(0)));
		Assert.assertArrayEquals(new byte[] {0x50, 0x60, (byte) 0xF0, (byte) 0xFF}, TessellatorTest.bytes(capture.getColor(1)));
		Assert.assertArrayEquals(new byte[] {127, 63, (byte) 255, (byte) 255}, TessellatorTest.bytes(capture.getColor(2)));
		Assert.assertArrayEquals(new byte[] {0, (byte) 255, 127, 127}, TessellatorTest.bytes(capture.getColor(3)));
		Assert.assertArrayEquals(new byte[] {7, 8, 9, (byte) 255}, TessellatorTest.bytes(capture.getColor(4)));
	}

	@Test
	public void clampsTheColorChannels() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setColor(300, -5, 128, 999);
		TessellatorTest.triangle(tessellator);
		Assert.assertArrayEquals(new byte[] {(byte) 255, 0, (byte) 128, (byte) 255}, TessellatorTest.bytes(this.single().getColor(0)));
	}

	@Test
	public void ignoresTheColorOnceDisabled() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.disableColor();
		tessellator.setColor(255, 0, 0, 255);
		TessellatorTest.triangle(tessellator);
		Assert.assertFalse(this.single().isColor());

		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setColor(255, 0, 0, 255);
		TessellatorTest.triangle(tessellator);
		Assert.assertTrue(this.render.getLast().isColor());
	}

	@Test
	public void writesTheTextureCoordinates() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.addVertexWithUV(0D, 0D, 0D, 0.25D, 0.5D);
		tessellator.setTextureUV(0.75D, 1D);
		tessellator.addVertex(1D, 0D, 0D);
		tessellator.addVertex(1D, 1D, 0D);
		tessellator.draw();

		final Capture capture = this.single();
		Assert.assertTrue(capture.isTexture());
		Assert.assertEquals(0.25F, capture.getU(0), 0F);
		Assert.assertEquals(0.5F, capture.getV(0), 0F);
		Assert.assertEquals(0.75F, capture.getU(2), 0F);
		Assert.assertEquals(1F, capture.getV(2), 0F);
	}

	@Test
	public void packsTheNormalIntoSignedBytes() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setNormal(0F, 1F, -1F);
		TessellatorTest.triangle(tessellator);

		final Capture capture = this.single();
		Assert.assertTrue(capture.isNormal());
		Assert.assertEquals(127 << 8 | 0x81 << 16, capture.getNormal(0));
	}

	@Test
	public void forgetsTheFormatOfThePreviousDraw() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setColor(1, 2, 3);
		tessellator.setNormal(1F, 0F, 0F);
		tessellator.setTextureUV(1D, 1D);
		TessellatorTest.triangle(tessellator);
		tessellator.start(DrawMode.TRIANGLES);
		TessellatorTest.triangle(tessellator);

		final Capture capture = this.render.getLast();
		Assert.assertFalse(capture.isTexture());
		Assert.assertFalse(capture.isColor());
		Assert.assertFalse(capture.isNormal());
	}

	@Test
	public void offsetsTheFollowingVertices() {
		final Tessellator tessellator = Tessellator.inst().copy();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.translate(10F, 20F, 30F);
		tessellator.translate(1F, 2F, 3F);
		TessellatorTest.triangle(tessellator);

		final Capture capture = this.single();
		Assert.assertEquals(11F, capture.getX(0), 0F);
		Assert.assertEquals(22F, capture.getY(0), 0F);
		Assert.assertEquals(33F, capture.getZ(0), 0F);
		Assert.assertEquals(12F, capture.getX(1), 0F);
		Assert.assertEquals(11D, tessellator.getXOffset(), 0D);
	}

	@Test
	public void forgetsItsOffsetAtTheNextStart() {
		final Tessellator tessellator = Tessellator.inst().copy();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.translate(10F, 20F, 30F);
		TessellatorTest.triangle(tessellator);
		tessellator.start(DrawMode.TRIANGLES);
		Assert.assertEquals(0D, tessellator.getXOffset(), 0D);
		Assert.assertEquals(0D, tessellator.getYOffset(), 0D);
		Assert.assertEquals(0D, tessellator.getZOffset(), 0D);
		TessellatorTest.triangle(tessellator);

		final Capture capture = this.render.getLast();
		Assert.assertEquals(0F, capture.getX(0), 0F);
		Assert.assertEquals(0F, capture.getY(0), 0F);
		Assert.assertEquals(0F, capture.getZ(0), 0F);
		Assert.assertEquals(1F, capture.getX(1), 0F);
	}

	@Test
	public void growsWithALargeDraw() {
		final Tessellator tessellator = Tessellator.inst().copy();
		tessellator.start(DrawMode.TRIANGLES);
		for (int i = 0; i < 9999; i++) {
			tessellator.addVertex(i, 0D, 0D);
		}
		Assert.assertEquals(0x20000, tessellator.getRawBufferSize());
		tessellator.draw();

		final Capture capture = this.single();
		Assert.assertEquals(9999, capture.getCount());
		Assert.assertEquals(9998F, capture.getX(9998), 0F);
	}

	@Test
	public void releasesAHugeBufferAfterItsDraw() {
		final Tessellator tessellator = Tessellator.inst().copy();
		tessellator.quads();
		for (int i = 0; i < 20000; i++) {
			tessellator.addVertex(i, 0D, 0D);
		}
		Assert.assertEquals(0x40000, tessellator.getRawBufferSize());
		tessellator.draw();
		Assert.assertEquals(30000, this.single().getCount());
		Assert.assertEquals(0x10000, tessellator.getRawBufferSize());
		Assert.assertEquals(0x10000, tessellator.getRawBuffer().length);
	}

	private Capture lines(final DrawMode mode, final int count) {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(mode);
		for (int i = 0; i < count; i++) {
			tessellator.addVertex(i, i % 2, 0D);
		}
		tessellator.draw();
		return this.single();
	}

	private Capture single() {
		Assert.assertEquals(1, this.render.getCaptures().size());
		return this.render.getCaptures().get(0);
	}

	private static void triangle(final Tessellator tessellator) {
		tessellator.addVertex(0D, 0D, 0D);
		tessellator.addVertex(1D, 0D, 0D);
		tessellator.addVertex(1D, 1D, 0D);
		tessellator.draw();
	}

	private static float[] xs(final Capture capture) {
		final float[] xs = new float[capture.getCount()];
		for (int i = 0; i < xs.length; i++) {
			xs[i] = capture.getX(i);
		}
		return xs;
	}

	private static byte[] bytes(final int color) {
		return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(color).array();
	}

}