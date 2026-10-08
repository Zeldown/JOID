package dev.joid.test.snapshot;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.utils.image.PixelLayout;

public class SnapshotImageTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void writesAPngThatReadsBackTheSame() {
		final File file = new File(this.folder.getRoot(), "renders/image.png");
		SnapshotImageTest.image(2, 1, 0xFF102030, 0xFFFFFFFF).write(file);
		final SnapshotImage image = SnapshotImage.read(file);
		Assert.assertEquals(2, image.getWidth());
		Assert.assertEquals(1, image.getHeight());
		Assert.assertArrayEquals(new int[] {0xFF102030, 0xFFFFFFFF}, image.getPixels());
	}

	@Test(expected = UncheckedIOException.class)
	public void failsToReadAMissingFile() {
		SnapshotImage.read(new File(this.folder.getRoot(), "missing.png"));
	}

	@Test
	public void readsOpaqueRgbaRowsFromTheTop() {
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0x10, 0x20, 0x30, 0, 0x40, 0x50, 0x60, 0});
		Assert.assertArrayEquals(new int[] {0xFF102030, 0xFF405060}, SnapshotImage.fromBytes(buffer, 1, 2, false, PixelLayout.RGBA8).getPixels());
	}

	@Test
	public void flipsTheRowsOfABottomUpCapture() {
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0x10, 0x20, 0x30, 0, 0x40, 0x50, 0x60, 0});
		Assert.assertArrayEquals(new int[] {0xFF405060, 0xFF102030}, SnapshotImage.fromBytes(buffer, 1, 2, true, PixelLayout.RGBA8).getPixels());
	}

	@Test
	public void swapsTheRedAndBlueOfBgraBytes() {
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0x10, 0x20, 0x30, 0, 0x40, 0x50, 0x60, 0});
		Assert.assertArrayEquals(new int[] {0xFF302010, 0xFF605040}, SnapshotImage.fromBytes(buffer, 2, 1, false, PixelLayout.BGRA8).getPixels());
	}

	@Test
	public void isSameOnlyWithTheSameSizeAndPixels() {
		final SnapshotImage image = SnapshotImageTest.image(2, 1, 0xFF000000, 0xFFFFFFFF);
		Assert.assertTrue(image.isSame(SnapshotImageTest.image(2, 1, 0xFF000000, 0xFFFFFFFF)));
		Assert.assertFalse(image.isSame(SnapshotImageTest.image(2, 1, 0xFF000000, 0xFFFFFFFE)));
		Assert.assertFalse(image.isSame(SnapshotImageTest.image(1, 2, 0xFF000000, 0xFFFFFFFF)));
		Assert.assertFalse(image.isSame(SnapshotImageTest.image(2, 2, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF)));
	}

	@Test
	public void fillsOnlyTheInsideOfTheImage() {
		final SnapshotImage image = SnapshotImageTest.image(3, 2, 0, 0, 0, 0, 0, 0);
		image.fill(-1, -1, 2, 2, 0xFFFF00FF);
		image.fill(2, 1, 5, 5, 0xFF00FF00);
		Assert.assertArrayEquals(new int[] {0xFFFF00FF, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF00FF00}, image.getPixels());
	}

	@Test
	public void toleratesASmallDeltaOnEachChannel() {
		final SnapshotImage reference = SnapshotImageTest.image(4, 1, 0xFF808080, 0xFF808080, 0xFF808080, 0xFF808080);
		final SnapshotImage render = SnapshotImageTest.image(4, 1, 0xFF818080, 0xFF808280, 0xFF80807D, 0xFF808080);
		final SnapshotDifference difference = render.compare(reference, 1);
		Assert.assertEquals(2, difference.getPixels());
		Assert.assertEquals(3, difference.getMaximum());
		Assert.assertEquals(0, render.compare(reference, 3).getPixels());
		Assert.assertEquals(3, render.compare(reference, 0).getPixels());
		Assert.assertEquals(0, reference.compare(reference, 0).getMaximum());
	}

	@Test
	public void countsEveryPixelWhenTheSizesDiffer() {
		final SnapshotImage image = SnapshotImageTest.image(2, 1, 0xFF000000, 0xFF000000);
		final SnapshotDifference difference = image.compare(SnapshotImageTest.image(1, 2, 0xFF000000, 0xFF000000), 255);
		Assert.assertEquals(2, difference.getPixels());
		Assert.assertEquals(255, difference.getMaximum());
		Assert.assertEquals(2, image.compare(SnapshotImageTest.image(2, 2, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000), 255).getPixels());
	}

	@Test(expected = UncheckedIOException.class)
	public void failsWithAnIoErrorOnAFileThatIsNotAnImage() throws IOException {
		final File file = this.folder.newFile("static-home.png");
		Files.write(file.toPath(), "version https://git-lfs.github.com/spec/v1".getBytes(StandardCharsets.UTF_8));
		SnapshotImage.read(file);
	}

	private static SnapshotImage image(final int width, final int height, final int... colors) {
		final ByteBuffer buffer = ByteBuffer.allocate(colors.length * 4);
		for (final int color : colors) {
			buffer.put((byte) (color >> 16)).put((byte) (color >> 8)).put((byte) color).put((byte) 255);
		}
		buffer.flip();
		return SnapshotImage.fromBytes(buffer, width, height, false, PixelLayout.RGBA8);
	}

}