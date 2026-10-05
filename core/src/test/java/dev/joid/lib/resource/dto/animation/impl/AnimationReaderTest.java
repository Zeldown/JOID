package dev.joid.lib.resource.dto.animation.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.resource.dto.animation.Animation;
import dev.joid.lib.resource.dto.animation.IAnimationReader;

public class AnimationReaderTest {

	@Test
	public void composesTheFramesOfAGif() throws IOException {
		final Animation animation = AnimationReaderTest.read(new GifAnimationReader(), "blink.gif");
		AnimationReaderTest.assertFrames(animation, "blink-gif", 50L, 100L, 150L);
		Assert.assertEquals(0, animation.getPlays());
	}

	@Test
	public void composesTheFramesOfAnApng() throws IOException {
		final Animation animation = AnimationReaderTest.read(new ApngAnimationReader(), "blink.png");
		AnimationReaderTest.assertFrames(animation, "blink-apng", 40L, 80L, 120L);
		Assert.assertEquals(2, animation.getPlays());
	}

	@Test
	public void composesTheFramesOfAWebp() throws IOException {
		final Animation animation = AnimationReaderTest.read(new WebpAnimationReader(), "blink.webp");
		AnimationReaderTest.assertFrames(animation, "blink-webp", 60L, 90L, 120L);
		Assert.assertEquals(3, animation.getPlays());
	}

	@Test
	public void tellsAnAnimatedWebpFromAStillOne() throws IOException {
		Assert.assertTrue(WebpAnimationReader.isAnimated(AnimationReaderTest.bytes("blink.webp")));
		Assert.assertFalse(WebpAnimationReader.isAnimated(AnimationReaderTest.bytes("still.webp")));
		Assert.assertTrue(WebpAnimationReader.isWebp(AnimationReaderTest.bytes("still.webp")));
	}

	@Test
	public void tellsAnApngFromAStillPng() throws IOException {
		Assert.assertTrue(ApngAnimationReader.isAnimated(AnimationReaderTest.bytes("blink.png")).get());
		Assert.assertFalse(ApngAnimationReader.isAnimated(AnimationReaderTest.bytes("still.png")).get());
		Assert.assertFalse(ApngAnimationReader.isAnimated(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13}).isPresent());
	}

	private static Animation read(final IAnimationReader reader, final String name) throws IOException {
		try (InputStream stream = AnimationReaderTest.class.getResourceAsStream("/animation/" + name)) {
			return reader.read(stream);
		}
	}

	private static byte[] bytes(final String name) throws IOException {
		try (InputStream stream = AnimationReaderTest.class.getResourceAsStream("/animation/" + name)) {
			final byte[] bytes = new byte[4096];
			int total = 0;
			int count;
			while ((count = stream.read(bytes, total, bytes.length - total)) > 0) {
				total += count;
			}
			final byte[] read = new byte[total];
			System.arraycopy(bytes, 0, read, 0, total);
			return read;
		}
	}

	private static void assertFrames(final Animation animation, final String prefix, final long... durations) throws IOException {
		Assert.assertEquals(8, animation.getWidth());
		Assert.assertEquals(8, animation.getHeight());
		Assert.assertEquals(durations.length, animation.getFrames().size());
		for (int i = 0; i < durations.length; i++) {
			Assert.assertEquals(durations[i], animation.getFrames().get(i).getDuration());
			final BufferedImage expected = ImageIO.read(AnimationReaderTest.class.getResourceAsStream("/animation/" + prefix + "-" + i + ".png"));
			final int[] pixels = animation.getFrames().get(i).getPixels();
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					final int want = expected.getRGB(x, y);
					final int got = pixels[y * 8 + x];
					Assert.assertEquals("alpha of frame " + i + " at " + x + "," + y, want >>> 24, got >>> 24);
					if (want >>> 24 != 0) {
						Assert.assertEquals("color of frame " + i + " at " + x + "," + y, want & 0xFFFFFF, got & 0xFFFFFF);
					}
				}
			}
		}
	}

}