package dev.joid.lib.resource.dto.animation.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.resource.dto.animation.IResourceAnimationReader;
import dev.joid.lib.resource.dto.animation.ResourceAnimation;

public class ResourceAnimationReaderTest {

	@Test
	public void composesTheFramesOfAGif() throws IOException {
		final ResourceAnimation animation = ResourceAnimationReaderTest.read(new GifResourceAnimationReader(), "blink.gif");
		ResourceAnimationReaderTest.assertFrames(animation, "blink-gif", 50L, 100L, 150L);
		Assert.assertEquals(0, animation.getPlays());
	}

	@Test
	public void composesTheFramesOfAnApng() throws IOException {
		final ResourceAnimation animation = ResourceAnimationReaderTest.read(new ApngResourceAnimationReader(), "blink.png");
		ResourceAnimationReaderTest.assertFrames(animation, "blink-apng", 40L, 80L, 120L);
		Assert.assertEquals(2, animation.getPlays());
	}

	@Test
	public void composesTheFramesOfAWebp() throws IOException {
		final ResourceAnimation animation = ResourceAnimationReaderTest.read(new WebpResourceAnimationReader(), "blink.webp");
		ResourceAnimationReaderTest.assertFrames(animation, "blink-webp", 60L, 90L, 120L);
		Assert.assertEquals(3, animation.getPlays());
	}

	@Test
	public void tellsAnAnimatedWebpFromAStillOne() throws IOException {
		Assert.assertTrue(WebpResourceAnimationReader.isAnimated(ResourceAnimationReaderTest.bytes("blink.webp")));
		Assert.assertFalse(WebpResourceAnimationReader.isAnimated(ResourceAnimationReaderTest.bytes("still.webp")));
		Assert.assertTrue(WebpResourceAnimationReader.isWebp(ResourceAnimationReaderTest.bytes("still.webp")));
	}

	@Test
	public void tellsAnApngFromAStillPng() throws IOException {
		Assert.assertTrue(ApngResourceAnimationReader.isAnimated(ResourceAnimationReaderTest.bytes("blink.png")).get());
		Assert.assertFalse(ApngResourceAnimationReader.isAnimated(ResourceAnimationReaderTest.bytes("still.png")).get());
		Assert.assertFalse(ApngResourceAnimationReader.isAnimated(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13}).isPresent());
	}

	@Test
	public void sizesAGifWithoutScreenOnItsFirstImage() throws IOException {
		final byte[] bytes = ResourceAnimationReaderTest.gif(ResourceAnimationReaderTest.frame(0, 3, 0xFF0000FF, "none"));
		Arrays.fill(bytes, 6, 10, (byte) 0);
		final ResourceAnimation animation = new GifResourceAnimationReader().read(new ByteArrayInputStream(bytes));
		Assert.assertEquals(3, animation.getWidth());
		Assert.assertEquals(1, animation.getHeight());
		Assert.assertArrayEquals(new int[] {0xFF0000FF, 0xFF0000FF, 0xFF0000FF}, animation.getFrames().get(0).getPixels());
	}

	@Test
	public void disposesAGifFrameToTheBackgroundOrToThePreviousCanvas() throws IOException {
		final byte[] bytes = ResourceAnimationReaderTest.gif(ResourceAnimationReaderTest.frame(0, 2, 0xFFFF0000, "restoreToBackgroundColor"), ResourceAnimationReaderTest.frame(1, 1, 0xFF00FF00, "restoreToPrevious"), ResourceAnimationReaderTest.frame(0, 1, 0xFF0000FF, "none"));
		final ResourceAnimation animation = new GifResourceAnimationReader().read(new ByteArrayInputStream(bytes));
		Assert.assertArrayEquals(new int[] {0xFFFF0000, 0xFFFF0000}, animation.getFrames().get(0).getPixels());
		Assert.assertArrayEquals(new int[] {0x0000FF00, 0xFF00FF00}, animation.getFrames().get(1).getPixels());
		Assert.assertArrayEquals(new int[] {0xFF0000FF, 0x000000FF}, animation.getFrames().get(2).getPixels());
	}

	@Test
	public void keepsTheLoopCountThroughAnotherApplicationExtension() throws IOException {
		final IIOImage first = ResourceAnimationReaderTest.extend(ResourceAnimationReaderTest.frame(0, 1, 0xFFFF0000, "none"), "NETSCAPE", "2.0", 1, 2, 0);
		final IIOImage second = ResourceAnimationReaderTest.extend(ResourceAnimationReaderTest.frame(0, 1, 0xFF00FF00, "none"), "XMP Data", "XMP", 60, 63);
		final ResourceAnimation animation = new GifResourceAnimationReader().read(new ByteArrayInputStream(ResourceAnimationReaderTest.gif(first, second)));
		Assert.assertEquals(2, animation.getFrames().size());
		Assert.assertEquals(3, animation.getPlays());
	}

	@Test
	public void copiesThePaletteAndItsTransparencyIntoEveryFrame() throws IOException {
		final byte[] second = ResourceAnimationReaderTest.deflate(0, 2);
		final byte[] bytes = ResourceAnimationReaderTest.png(ResourceAnimationReaderTest.chunk("IHDR", ByteBuffer.allocate(13).putInt(2).putInt(1).put((byte) 8).put((byte) 3).array()), ResourceAnimationReaderTest.chunk("acTL", ByteBuffer.allocate(8).putInt(2).putInt(0).array()), ResourceAnimationReaderTest.chunk("PLTE", new byte[] {(byte) 255, 0, 0, 0, (byte) 255, 0, 0, 0, (byte) 255}), ResourceAnimationReaderTest.chunk("tRNS", new byte[] {(byte) 255, (byte) 255, (byte) 128}), ResourceAnimationReaderTest.chunk("fcTL", ResourceAnimationReaderTest.control(0, 2, 0, 1)), ResourceAnimationReaderTest.chunk("IDAT", ResourceAnimationReaderTest.deflate(0, 0, 1)), ResourceAnimationReaderTest.chunk("fcTL", ResourceAnimationReaderTest.control(1, 1, 1, 2)), ResourceAnimationReaderTest.chunk("fdAT", ByteBuffer.allocate(4 + second.length).putInt(2).put(second).array()), ResourceAnimationReaderTest.chunk("IEND", new byte[0]));
		final ResourceAnimation animation = new ApngResourceAnimationReader().read(new ByteArrayInputStream(bytes));
		Assert.assertEquals(0, animation.getPlays());
		Assert.assertEquals(100L, animation.getFrames().get(0).getDuration());
		Assert.assertEquals(200L, animation.getFrames().get(1).getDuration());
		Assert.assertArrayEquals(new int[] {0xFFFF0000, 0xFF00FF00}, animation.getFrames().get(0).getPixels());
		Assert.assertArrayEquals(new int[] {0xFFFF0000, 0x800000FF}, animation.getFrames().get(1).getPixels());
	}

	@Test
	public void refusesAPngWithoutHeader() throws IOException {
		ResourceAnimationReaderTest.assertRefused(new ApngResourceAnimationReader(), ResourceAnimationReaderTest.png(ResourceAnimationReaderTest.chunk("IEND", new byte[0])), "Missing IHDR chunk");
	}

	@Test
	public void refusesAStillPng() throws IOException {
		ResourceAnimationReaderTest.assertRefused(new ApngResourceAnimationReader(), ResourceAnimationReaderTest.bytes("still.png"), "Not an animated PNG");
	}

	@Test(expected = IOException.class)
	public void refusesAnApngFrameItCannotDecode() throws IOException {
		final byte[] bytes = ResourceAnimationReaderTest.png(ResourceAnimationReaderTest.chunk("IHDR", ByteBuffer.allocate(13).putInt(2).putInt(1).put((byte) 8).put((byte) 2).array()), ResourceAnimationReaderTest.chunk("acTL", ByteBuffer.allocate(8).putInt(1).putInt(0).array()), ResourceAnimationReaderTest.chunk("fcTL", ResourceAnimationReaderTest.control(0, 2, 0, 1)), ResourceAnimationReaderTest.chunk("IDAT", "not deflated".getBytes(StandardCharsets.US_ASCII)), ResourceAnimationReaderTest.chunk("IEND", new byte[0]));
		new ApngResourceAnimationReader().read(new ByteArrayInputStream(bytes));
	}

	@Test
	public void refusesBytesThatAreNotAWebp() throws IOException {
		ResourceAnimationReaderTest.assertRefused(new WebpResourceAnimationReader(), ResourceAnimationReaderTest.bytes("blink.gif"), "Not a WebP file");
	}

	@Test
	public void refusesAStillWebp() throws IOException {
		ResourceAnimationReaderTest.assertRefused(new WebpResourceAnimationReader(), ResourceAnimationReaderTest.bytes("still.webp"), "Not an animated WebP");
	}

	@Test
	public void refusesWebpFramesWithoutCanvas() {
		ResourceAnimationReaderTest.assertRefused(new WebpResourceAnimationReader(), new byte[] {'R', 'I', 'F', 'F', 28, 0, 0, 0, 'W', 'E', 'B', 'P', 'A', 'N', 'M', 'F', 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0, 0, 0}, "Not an animated WebP");
	}

	@Test
	public void tellsAShortOrForeignHeaderIsNoApng() throws IOException {
		Assert.assertEquals(Optional.of(false), ApngResourceAnimationReader.isAnimated(new byte[] {(byte) 0x89, 'P', 'N', 'G'}));
		Assert.assertEquals(Optional.of(false), ApngResourceAnimationReader.isAnimated(ResourceAnimationReaderTest.bytes("blink.gif")));
	}

	private static ResourceAnimation read(final IResourceAnimationReader reader, final String name) throws IOException {
		try (InputStream stream = ResourceAnimationReaderTest.class.getResourceAsStream("/animation/" + name)) {
			return reader.read(stream);
		}
	}

	private static byte[] bytes(final String name) throws IOException {
		try (InputStream stream = ResourceAnimationReaderTest.class.getResourceAsStream("/animation/" + name)) {
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

	private static void assertFrames(final ResourceAnimation animation, final String prefix, final long... durations) throws IOException {
		Assert.assertEquals(8, animation.getWidth());
		Assert.assertEquals(8, animation.getHeight());
		Assert.assertEquals(durations.length, animation.getFrames().size());
		for (int i = 0; i < durations.length; i++) {
			Assert.assertEquals(durations[i], animation.getFrames().get(i).getDuration());
			final BufferedImage expected = ImageIO.read(ResourceAnimationReaderTest.class.getResourceAsStream("/animation/" + prefix + "-" + i + ".png"));
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

	private static void assertRefused(final IResourceAnimationReader reader, final byte[] bytes, final String message) {
		try {
			reader.read(new ByteArrayInputStream(bytes));
			Assert.fail("The bytes must be refused");
		} catch (final IOException expected) {
			Assert.assertEquals(message, expected.getMessage());
		}
	}

	private static byte[] gif(final IIOImage... frames) throws IOException {
		final ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
			writer.setOutput(stream);
			writer.prepareWriteSequence(null);
			for (final IIOImage frame : frames) {
				writer.writeToSequence(frame, null);
			}
			writer.endWriteSequence();
		} finally {
			writer.dispose();
		}
		return output.toByteArray();
	}

	private static IIOImage frame(final int x, final int width, final int color, final String disposal) throws IOException {
		final BufferedImage image = new BufferedImage(width, 1, BufferedImage.TYPE_INT_RGB);
		for (int column = 0; column < width; column++) {
			image.setRGB(column, 0, color);
		}

		final IIOMetadata metadata = ImageIO.getImageWritersByFormatName("gif").next().getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image), null);
		final IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_image_1.0");
		((IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0)).setAttribute("imageLeftPosition", Integer.toString(x));
		((IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0)).setAttribute("disposalMethod", disposal);
		metadata.setFromTree("javax_imageio_gif_image_1.0", root);
		return new IIOImage(image, null, metadata);
	}

	private static IIOImage extend(final IIOImage frame, final String application, final String code, final int... data) throws IOException {
		final byte[] bytes = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			bytes[i] = (byte) data[i];
		}

		final IIOMetadataNode extension = new IIOMetadataNode("ApplicationExtension");
		extension.setAttribute("applicationID", application);
		extension.setAttribute("authenticationCode", code);
		extension.setUserObject(bytes);
		final IIOMetadataNode extensions = new IIOMetadataNode("ApplicationExtensions");
		extensions.appendChild(extension);
		final IIOMetadataNode root = (IIOMetadataNode) frame.getMetadata().getAsTree("javax_imageio_gif_image_1.0");
		root.appendChild(extensions);
		frame.getMetadata().setFromTree("javax_imageio_gif_image_1.0", root);
		return frame;
	}

	private static byte[] png(final byte[]... chunks) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
		for (final byte[] chunk : chunks) {
			output.write(chunk);
		}
		return output.toByteArray();
	}

	private static byte[] chunk(final String name, final byte[] data) {
		final byte[] type = name.getBytes(StandardCharsets.US_ASCII);
		final CRC32 crc = new CRC32();
		crc.update(type);
		crc.update(data);
		return ByteBuffer.allocate(12 + data.length).putInt(data.length).put(type).put(data).putInt((int) crc.getValue()).array();
	}

	private static byte[] control(final int sequence, final int width, final int x, final int delay) {
		return ByteBuffer.allocate(26).putInt(sequence).putInt(width).putInt(1).putInt(x).putInt(0).putShort((short) delay).putShort((short) 10).array();
	}

	private static byte[] deflate(final int... row) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (DeflaterOutputStream stream = new DeflaterOutputStream(output)) {
			for (final int value : row) {
				stream.write(value);
			}
		}
		return output.toByteArray();
	}

}