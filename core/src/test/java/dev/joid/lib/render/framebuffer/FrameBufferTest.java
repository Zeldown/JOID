package dev.joid.lib.render.framebuffer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;

public class FrameBufferTest {

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@Test
	public void createsAHandleOfTheRequestedSize() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.LINEAR);
		Assert.assertSame(this.render.getFrameBuffers().get(0), frameBuffer.getHandle());
		Assert.assertEquals(64, frameBuffer.getWidth());
		Assert.assertEquals(32, frameBuffer.getHeight());
		Assert.assertSame(TextureFilter.LINEAR, frameBuffer.getFilter());
		Assert.assertFalse(frameBuffer.isFilled());
	}

	@Test
	public void wrapsTheHandleItIsGiven() {
		final RecordingFrameBuffer handle = new RecordingFrameBuffer(8, 4);
		final FrameBuffer frameBuffer = new FrameBuffer(handle, TextureFilter.NEAREST);
		Assert.assertSame(handle, frameBuffer.getHandle());
		Assert.assertEquals(8, frameBuffer.getWidth());
		Assert.assertEquals(4, frameBuffer.getHeight());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingFilter() {
		FrameBuffer.create(64, 32, null);
	}

	@Test
	public void bindsItsHandle() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		Assert.assertSame(frameBuffer, frameBuffer.bind());
		Assert.assertSame(frameBuffer.getHandle(), this.render.getState().getFrameBuffer());
		Assert.assertSame(frameBuffer, frameBuffer.unbind());
		Assert.assertNull(this.render.getState().getFrameBuffer());
	}

	@Test
	public void restoresThePreviousTargetOnUnbind() {
		final FrameBuffer outer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		final FrameBuffer inner = FrameBuffer.create(16, 8, TextureFilter.NEAREST);
		outer.bind();
		this.render.getState().viewport(0, 0, 64, 32);
		inner.fill(() -> this.render.getState().viewport(0, 0, 16, 8));
		Assert.assertSame(outer.getHandle(), this.render.getState().getFrameBuffer());
		Assert.assertEquals(64, this.render.getState().getViewportWidth());
		outer.unbind();
		Assert.assertNull(this.render.getState().getFrameBuffer());
		Assert.assertEquals(1920, this.render.getState().getViewportWidth());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToBindTwice() {
		FrameBuffer.create(64, 32, TextureFilter.NEAREST).bind().bind();
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToUnbindWithoutBind() {
		FrameBuffer.create(64, 32, TextureFilter.NEAREST).unbind();
	}

	@Test
	public void runsItsFillInsideItsHandle() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		final IFrameBuffer[] bound = new IFrameBuffer[1];
		Assert.assertSame(frameBuffer, frameBuffer.fill(() -> bound[0] = this.render.getState().getFrameBuffer()));
		Assert.assertSame(frameBuffer.getHandle(), bound[0]);
		Assert.assertNull(this.render.getState().getFrameBuffer());
		Assert.assertTrue(frameBuffer.isFilled());
	}

	@Test
	public void fillsWithItsOwnViewportAndProjection() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		final int[] viewport = new int[4];
		final float[][] matrices = new float[2][];
		this.render.getModelView().translate(5D, 6D, 0D);
		final float[] projection = this.render.getProjection().getMatrix().clone();
		final float[] modelView = this.render.getModelView().getMatrix().clone();
		frameBuffer.fill(() -> {
			viewport[0] = this.render.getState().getViewportX();
			viewport[1] = this.render.getState().getViewportY();
			viewport[2] = this.render.getState().getViewportWidth();
			viewport[3] = this.render.getState().getViewportHeight();
			matrices[0] = this.render.getProjection().getMatrix().clone();
			matrices[1] = this.render.getModelView().getMatrix().clone();
		});
		Assert.assertArrayEquals(new int[] {0, 0, 64, 32}, viewport);
		Assert.assertArrayEquals(FrameBufferTest.ortho(0D, 64D, 32D, 0D), matrices[0], 0F);
		Assert.assertArrayEquals(new MatrixStack().getMatrix(), matrices[1], 0F);
		Assert.assertEquals(1920, this.render.getState().getViewportWidth());
		Assert.assertArrayEquals(projection, this.render.getProjection().getMatrix(), 0F);
		Assert.assertArrayEquals(modelView, this.render.getModelView().getMatrix(), 0F);
	}

	@Test
	public void fillsARegionOfTheCanvas() {
		final int[] viewport = new int[1];
		final float[][] projection = new float[1][];
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST).fill(10D, 20D, 100D, 50D, () -> {
			viewport[0] = this.render.getState().getViewportWidth();
			projection[0] = this.render.getProjection().getMatrix().clone();
		});
		Assert.assertEquals(64, viewport[0]);
		Assert.assertArrayEquals(FrameBufferTest.ortho(10D, 110D, 70D, 20D), projection[0], 0F);
		Assert.assertTrue(frameBuffer.isFilled());
	}

	@Test
	public void restoresTheMatricesOfAFailedFill() {
		final float[] projection = this.render.getProjection().getMatrix().clone();
		final float[] modelView = this.render.getModelView().getMatrix().clone();
		try {
			FrameBuffer.create(64, 32, TextureFilter.NEAREST).fill(() -> {
				throw new IllegalStateException("fill");
			});
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("fill", exception.getMessage());
		}
		Assert.assertArrayEquals(projection, this.render.getProjection().getMatrix(), 0F);
		Assert.assertArrayEquals(modelView, this.render.getModelView().getMatrix(), 0F);
		Assert.assertEquals(1920, this.render.getState().getViewportWidth());
	}

	@Test
	public void unbindsAFailedFill() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		try {
			frameBuffer.fill(() -> {
				throw new IllegalStateException("fill");
			});
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("fill", exception.getMessage());
		}
		Assert.assertNull(this.render.getState().getFrameBuffer());
		Assert.assertFalse(frameBuffer.isFilled());
	}

	@Test
	public void refusesToDrawBeforeItsFill() {
		try {
			FrameBuffer.create(64, 32, TextureFilter.NEAREST).draw(0D, 0D, 64D, 32D);
			Assert.fail("An empty framebuffer must not be drawn");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The framebuffer of 64x32 is empty, call fill(...) before draw(...)", expected.getMessage());
		}
	}

	@Test
	public void drawsItsTextureOnAQuad() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.LINEAR).fill(() -> {});
		Assert.assertSame(frameBuffer, frameBuffer.draw(10D, 20D, 100D, 50D));
		final Capture capture = this.single();
		Assert.assertSame(Primitive.TRIANGLES, capture.getPrimitive());
		Assert.assertEquals(6, capture.getCount());
		Assert.assertTrue(capture.isTexture());
		Assert.assertSame(frameBuffer.getHandle().getTexture(), capture.getState().getTexture());
		Assert.assertSame(TextureFilter.LINEAR, capture.getState().getTextureFilter());
		Assert.assertSame(TextureWrap.CLAMP_TO_BORDER, capture.getState().getTextureWrap());
		Assert.assertSame(BlendState.NORMAL, capture.getState().getBlend());
		Assert.assertEquals(10D, capture.getLeft(), 0D);
		Assert.assertEquals(110D, capture.getRight(), 0D);
		Assert.assertEquals(20D, capture.getTop(), 0D);
		Assert.assertEquals(70D, capture.getBottom(), 0D);
	}

	@Test
	public void flipsItsTextureVertically() {
		FrameBuffer.create(64, 32, TextureFilter.LINEAR).fill(() -> {}).draw(10D, 20D, 100D, 50D);
		final Capture capture = this.single();
		Assert.assertEquals(70F, capture.getY(0), 0F);
		Assert.assertEquals(0F, capture.getV(0), 0F);
		Assert.assertEquals(110F, capture.getX(2), 0F);
		Assert.assertEquals(1F, capture.getU(2), 0F);
		Assert.assertEquals(1F, capture.getV(2), 0F);
		Assert.assertEquals(0F, capture.getU(5), 0F);
		Assert.assertEquals(1F, capture.getV(5), 0F);
	}

	@Test
	public void releasesTheBlendingAndTheTextureAfterItsDraw() {
		FrameBuffer.create(64, 32, TextureFilter.LINEAR).fill(() -> {}).draw(10D, 20D, 100D, 50D);
		Assert.assertSame(BlendState.DISABLED, this.render.getState().getBlend());
		Assert.assertNull(this.render.getState().getTexture());
	}

	@Test
	public void deletesItsHandle() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		frameBuffer.delete();
		Assert.assertTrue(((RecordingFrameBuffer) frameBuffer.getHandle()).isDeleted());
	}

	private Capture single() {
		Assert.assertEquals(1, this.render.getCaptures().size());
		return this.render.getCaptures().get(0);
	}

	private static float[] ortho(final double left, final double right, final double bottom, final double top) {
		final MatrixStack stack = new MatrixStack();
		stack.ortho(left, right, bottom, top, -1000D, 1000D);
		return stack.getMatrix();
	}

}