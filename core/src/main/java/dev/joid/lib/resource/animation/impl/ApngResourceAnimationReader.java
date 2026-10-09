package dev.joid.lib.resource.animation.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import dev.joid.lib.resource.animation.IResourceAnimationReader;
import dev.joid.lib.resource.animation.ResourceAnimation;
import dev.joid.lib.resource.animation.ResourceAnimationCanvas;
import dev.joid.lib.resource.animation.ResourceAnimationCanvas.Blend;
import dev.joid.lib.resource.animation.ResourceAnimationCanvas.Disposal;
import dev.joid.lib.resource.animation.ResourceAnimationFrame;
import lombok.NonNull;

public class ApngResourceAnimationReader implements IResourceAnimationReader {

	private static final byte[] SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

	@Override
	public @NonNull ResourceAnimation read(final @NonNull InputStream stream) throws IOException {
		final DataInputStream input = new DataInputStream(stream);
		final byte[] signature = new byte[8];
		input.readFully(signature);

		final ByteArrayOutputStream header = new ByteArrayOutputStream();
		final List<Control> controls = new ArrayList<>();
		final List<ByteArrayOutputStream> datas = new ArrayList<>();
		byte[] ihdr = null;
		int plays = 1;
		boolean animated = false;
		boolean seenData = false;
		while (true) {
			final int length = input.readInt();
			final byte[] type = new byte[4];
			input.readFully(type);
			final byte[] data = new byte[length];
			input.readFully(data);
			input.readInt();

			final String name = new String(type, StandardCharsets.US_ASCII);
			if (name.equals("IEND")) {
				break;
			}

			switch (name) {
			case "IHDR":
				ihdr = data;
				break;
			case "acTL":
				animated = true;
				plays = ByteBuffer.wrap(data).getInt(4);
				break;
			case "fcTL":
				controls.add(Control.read(data));
				datas.add(new ByteArrayOutputStream());
				break;
			case "IDAT":
				seenData = true;
				if (controls.size() == 1 && datas.size() == 1) {
					datas.get(0).write(data);
				}
				break;
			case "fdAT":
				datas.get(datas.size() - 1).write(data, 4, data.length - 4);
				break;
			default:
				if (!seenData && controls.isEmpty() && Character.isLowerCase(name.charAt(0)) || name.equals("PLTE")) {
					ApngResourceAnimationReader.chunk(header, name, data);
				}
				break;
			}
		}

		if (ihdr == null) {
			throw new IOException("Missing IHDR chunk");
		}

		final ByteBuffer size = ByteBuffer.wrap(ihdr);
		final int width = size.getInt(0);
		final int height = size.getInt(4);
		if (!animated || controls.isEmpty()) {
			throw new IOException("Not an animated PNG");
		}

		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(width, height);
		final List<ResourceAnimationFrame> frames = new ArrayList<>();
		for (int i = 0; i < controls.size(); i++) {
			final Control control = controls.get(i);
			final BufferedImage image = ImageIO.read(new ByteArrayInputStream(ApngResourceAnimationReader.png(ihdr, header.toByteArray(), control, datas.get(i).toByteArray())));
			final int[] pixels = image.getRGB(0, 0, control.width, control.height, null, 0, control.width);
			final Disposal disposal = i == 0 && control.disposal == Disposal.PREVIOUS ? Disposal.BACKGROUND : control.disposal;
			frames.add(ResourceAnimationFrame.create(canvas.compose(pixels, control.x, control.y, control.width, control.height, control.blend, disposal), control.duration));
		}
		return ResourceAnimation.create(width, height, plays, frames);
	}

	public static Boolean isAnimated(final @NonNull byte[] bytes) {
		if (bytes.length < ApngResourceAnimationReader.SIGNATURE.length) {
			return false;
		}

		for (int i = 0; i < ApngResourceAnimationReader.SIGNATURE.length; i++) {
			if (bytes[i] != ApngResourceAnimationReader.SIGNATURE[i]) {
				return false;
			}
		}

		int offset = ApngResourceAnimationReader.SIGNATURE.length;
		while (offset + 8 <= bytes.length) {
			final int length = ByteBuffer.wrap(bytes, offset, 4).getInt();
			final String name = new String(bytes, offset + 4, 4, StandardCharsets.US_ASCII);
			if (name.equals("acTL")) {
				return true;
			}

			if (name.equals("IDAT") || length < 0) {
				return false;
			}
			offset += 12 + length;
		}
		return null;
	}

	private static byte[] png(final byte[] ihdr, final byte[] header, final Control control, final byte[] data) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(ApngResourceAnimationReader.SIGNATURE);
		final byte[] frameHeader = ihdr.clone();
		ByteBuffer.wrap(frameHeader).putInt(0, control.width).putInt(4, control.height);
		ApngResourceAnimationReader.chunk(output, "IHDR", frameHeader);
		output.write(header);
		ApngResourceAnimationReader.chunk(output, "IDAT", data);
		ApngResourceAnimationReader.chunk(output, "IEND", new byte[0]);
		return output.toByteArray();
	}

	private static void chunk(final ByteArrayOutputStream output, final String name, final byte[] data) throws IOException {
		final byte[] type = name.getBytes(StandardCharsets.US_ASCII);
		final CRC32 crc = new CRC32();
		crc.update(type);
		crc.update(data);
		output.write(ByteBuffer.allocate(4).putInt(data.length).array());
		output.write(type);
		output.write(data);
		output.write(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
	}

	private static final class Control {

		private int      width;
		private int      height;
		private int      x;
		private int      y;
		private long     duration;
		private Blend    blend;
		private Disposal disposal;

		private static Control read(final byte[] data) {
			final ByteBuffer buffer = ByteBuffer.wrap(data);
			final Control control = new Control();
			control.width = buffer.getInt(4);
			control.height = buffer.getInt(8);
			control.x = buffer.getInt(12);
			control.y = buffer.getInt(16);
			final int numerator = buffer.getShort(20) & 0xFFFF;
			final int denominator = buffer.getShort(22) & 0xFFFF;
			control.duration = numerator * 1000L / (denominator == 0 ? 100 : denominator);
			control.disposal = Disposal.values()[Math.min(2, data[24])];
			control.blend = data[25] == 1 ? Blend.OVER : Blend.SOURCE;
			return control;
		}

	}

}