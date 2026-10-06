package dev.joid.lib.render.framebuffer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.DrawMode;

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
	public void runsItsFillInsideItsHandle() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.NEAREST);
		final IFrameBuffer[] bound = new IFrameBuffer[1];
		Assert.assertSame(frameBuffer, frameBuffer.fill(() -> bound[0] = this.render.getState().getFrameBuffer()));
		Assert.assertSame(frameBuffer.getHandle(), bound[0]);
		Assert.assertNull(this.render.getState().getFrameBuffer());
		Assert.assertTrue(frameBuffer.isFilled());
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

	@Test(expected = RuntimeException.class)
	public void refusesToDrawBeforeItsFill() {
		FrameBuffer.create(64, 32, TextureFilter.NEAREST).draw(0D, 0D, 64D, 32D);
	}

	@Test
	public void drawsItsTextureOnAQuad() {
		final FrameBuffer frameBuffer = FrameBuffer.create(64, 32, TextureFilter.LINEAR).fill(() -> {});
		Assert.assertSame(frameBuffer, frameBuffer.draw(10D, 20D, 100D, 50D));
		final Capture capture = this.single();
		Assert.assertSame(DrawMode.TRIANGLES, capture.getMode());
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

}