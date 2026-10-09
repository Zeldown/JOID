package dev.joid.lib.bridge.render.framebuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.texture.MipmapChain;
import dev.joid.lib.bridge.render.texture.Texture;
import lombok.Getter;
import lombok.NonNull;

public class FrameBufferHandleTest {

	@Test
	public void takesItsSizeFromItsTexture() {
		final HookFrameBuffer frameBuffer = new HookFrameBuffer(64, 32);
		Assert.assertEquals(64, frameBuffer.getWidth());
		Assert.assertEquals(32, frameBuffer.getHeight());
	}

	@Test
	public void deletesItselfAndItsTextureOnce() {
		final HookFrameBuffer frameBuffer = new HookFrameBuffer(8, 8);
		frameBuffer.delete();
		frameBuffer.delete();
		Assert.assertEquals(1, frameBuffer.getDeletions());
		Assert.assertTrue(frameBuffer.isDeleted());
		Assert.assertTrue(frameBuffer.getTexture().isDeleted());
	}

	@Getter
	private static final class HookFrameBuffer extends FrameBufferHandle<Texture> {

		private int deletions;

		private HookFrameBuffer(final int width, final int height) {
			super(new HookTexture());
			super.getTexture().allocate(width, height);
		}

		@Override
		protected void deleteHandle() {
			this.deletions++;
		}

	}

	private static final class HookTexture extends Texture {

		@Override
		protected void allocateStorage(final @NonNull MipmapChain chain) {}

		@Override
		protected void uploadPixels(final @NonNull int[] pixels, final @NonNull MipmapChain chain) {}

		@Override
		protected void generateMipmapLevels(final @NonNull MipmapChain chain, final int allocatedLevels) {}

		@Override
		protected void deleteStorage() {}

	}

}