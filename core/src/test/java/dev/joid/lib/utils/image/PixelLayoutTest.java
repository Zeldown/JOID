package dev.joid.lib.utils.image;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class PixelLayoutTest {

	@Test
	public void writesTheChannelsInTheOrderOfTheLayout() {
		Assert.assertArrayEquals(new byte[] {0x11, 0x22, 0x33, (byte) 0x80}, PixelLayout.RGBA8.write(new int[] {0x80112233}, ByteBuffer.allocate(4)).array());
		Assert.assertArrayEquals(new byte[] {0x33, 0x22, 0x11, (byte) 0x80}, PixelLayout.BGRA8.write(new int[] {0x80112233}, ByteBuffer.allocate(4)).array());
	}

	@Test
	public void writesFromThePositionOfTheTarget() {
		final ByteBuffer target = ByteBuffer.allocate(8);
		target.position(4);
		PixelLayout.RGBA8.write(new int[] {0xFF0A0B0C}, target);
		Assert.assertArrayEquals(new byte[] {0, 0, 0, 0, 0x0A, 0x0B, 0x0C, (byte) 0xFF}, target.array());
		Assert.assertEquals(4, target.position());
	}

	@Test
	public void readsBackWhatItWrites() {
		final int[] pixels = {0x80112233, 0x00FFFFFF, 0xFF000000, 0x7F010203};
		for (final PixelLayout layout : PixelLayout.values()) {
			Assert.assertArrayEquals(layout.name(), pixels, layout.read(layout.write(pixels, ByteBuffer.allocate(16)), 2, 2, false));
		}
	}

	@Test
	public void readsTheRowsFromTheBottomWhenAsked() {
		final ByteBuffer source = ByteBuffer.wrap(new byte[] {0x10, 0x20, 0x30, (byte) 0xFF, 0x40, 0x50, 0x60, (byte) 0xFF});
		Assert.assertArrayEquals(new int[] {0xFF102030, 0xFF405060}, PixelLayout.RGBA8.read(source, 1, 2, false));
		Assert.assertArrayEquals(new int[] {0xFF405060, 0xFF102030}, PixelLayout.RGBA8.read(source, 1, 2, true));
		Assert.assertArrayEquals(new int[] {0xFF302010, 0xFF605040}, PixelLayout.BGRA8.read(source, 2, 1, false));
	}

}