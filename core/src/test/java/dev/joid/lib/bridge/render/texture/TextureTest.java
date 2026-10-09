package dev.joid.lib.bridge.render.texture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lombok.Getter;
import lombok.NonNull;

public class TextureTest {

	@Test
	public void isNotAllocatedBeforeItsFirstAllocation() {
		final HookTexture texture = new HookTexture();
		Assert.assertFalse(texture.isAllocated());
		texture.allocate(4, 2);
		Assert.assertTrue(texture.isAllocated());
		Assert.assertEquals(4, texture.getWidth());
		Assert.assertEquals(2, texture.getHeight());
		Assert.assertEquals(1, texture.getLevels());
	}

	@Test
	public void allocatesEveryLevelOfAMipmappedTexture() {
		final HookTexture texture = new HookTexture();
		texture.mipmap(true).allocate(256, 16);
		Assert.assertEquals(Arrays.asList("allocate 256x16 9"), texture.getCalls());
		Assert.assertEquals(9, texture.getLevels());
	}

	@Test
	public void keepsTheStorageOfTheSameSize() {
		final HookTexture texture = new HookTexture();
		texture.allocate(8, 8).allocate(8, 8);
		texture.allocate(8, 4);
		Assert.assertEquals(Arrays.asList("allocate 8x8 1", "allocate 8x4 1"), texture.getCalls());
	}

	@Test
	public void uploadsWithTheLevelsOfTheMipmapState() {
		final HookTexture texture = new HookTexture();
		texture.allocate(8, 8).upload(new int[64], 8, 8);
		texture.mipmap(true).upload(new int[64], 8, 8);
		Assert.assertEquals(Arrays.asList("allocate 8x8 1", "upload 8x8 1", "generate 8x8 4 from 1", "upload 8x8 4"), texture.getCalls());
		Assert.assertEquals(4, texture.getLevels());
	}

	@Test
	public void generatesTheLevelsOnlyOnceAllocated() {
		final HookTexture texture = new HookTexture();
		texture.mipmap(true).mipmap(false).mipmap(true);
		Assert.assertTrue(texture.getCalls().isEmpty());
		Assert.assertTrue(texture.isMipmapped());
	}

	@Test
	public void regeneratesTheLevelsWhenMipmappedAgain() {
		final HookTexture texture = new HookTexture();
		texture.mipmap(true).allocate(4, 4);
		texture.mipmap(false).mipmap(true);
		Assert.assertEquals(Arrays.asList("allocate 4x4 3", "generate 4x4 3 from 3"), texture.getCalls());
	}

	@Test
	public void capsTheLevelsAtTheLimitOfItsBackend() {
		final HookTexture texture = new HookTexture();
		texture.maxLevels = 5;
		texture.mipmap(true).allocate(256, 16).upload(new int[256 * 16], 256, 16);
		texture.mipmap(false).mipmap(true);
		Assert.assertEquals(Arrays.asList("allocate 256x16 5", "upload 256x16 5", "generate 256x16 5 from 5"), texture.getCalls());
		Assert.assertEquals(5, texture.getLevels());
	}

	@Test
	public void deletesOnce() {
		final HookTexture texture = new HookTexture();
		texture.allocate(2, 2);
		texture.delete();
		texture.delete();
		Assert.assertEquals(Arrays.asList("allocate 2x2 1", "delete"), texture.getCalls());
		Assert.assertTrue(texture.isDeleted());
		Assert.assertFalse(texture.isAllocated());
	}

	@Getter
	private static final class HookTexture extends Texture {

		private final List<String> calls = new ArrayList<>();

		private int maxLevels = Integer.MAX_VALUE;

		@Override
		protected void allocateStorage(final @NonNull MipmapChain chain) {
			this.calls.add("allocate " + chain.getWidth() + "x" + chain.getHeight() + " " + chain.getLevels());
		}

		@Override
		protected void uploadPixels(final @NonNull int[] pixels, final @NonNull MipmapChain chain) {
			this.calls.add("upload " + chain.getWidth() + "x" + chain.getHeight() + " " + chain.getLevels());
		}

		@Override
		protected void generateMipmapLevels(final @NonNull MipmapChain chain, final int allocatedLevels) {
			this.calls.add("generate " + chain.getWidth() + "x" + chain.getHeight() + " " + chain.getLevels() + " from " + allocatedLevels);
		}

		@Override
		protected void deleteStorage() {
			this.calls.add("delete");
		}

		@Override
		protected int getMaxLevels(final int width, final int height) {
			return this.maxLevels;
		}

	}

}