package be.zeldown.joid.lib.font.dto.font;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import be.zeldown.joid.lib.font.dto.atlas.Atlas;
import be.zeldown.joid.lib.font.dto.atlas.AtlasBounds;
import be.zeldown.joid.lib.font.dto.data.Glyph;
import be.zeldown.joid.lib.font.dto.data.Metrics;
import be.zeldown.joid.lib.font.dto.data.PlaneBounds;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class MsdfFile {

	public static final int BYTES   = 3;
	public static final int VERSION = 2;

	public static final byte[] MAGIC = {'J', 'O', 'I', 'D', 'M', 'S', 'D', 'F'};

	private final FontInfo      fontInfo;
	private final BufferedImage image;

	private MsdfFile(final FontInfo fontInfo, final BufferedImage image) {
		this.fontInfo = fontInfo;
		this.image = image;
	}

	public static @NonNull MsdfFile read(final @NonNull InputStream stream) {
		try {
			final byte[] magic = new byte[MsdfFile.MAGIC.length];
			new DataInputStream(stream).readFully(magic);
			for (int i = 0; i < magic.length; i++) {
				if (magic[i] != MsdfFile.MAGIC[i]) {
					throw new IOException("Not a JOID msdf font");
				}
			}

			final DataInputStream input = new DataInputStream(new InflaterInputStream(stream));
			final int version = input.readUnsignedByte();
			if (version != MsdfFile.VERSION) {
				throw new IOException("Unsupported msdf font version " + version);
			}

			final int width = input.readInt();
			final int height = input.readInt();
			final Atlas atlas = new Atlas("msdf", input.readFloat(), input.readFloat(), width, height, "bottom", 0F, 0F, 0F);
			final Metrics metrics = new Metrics(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());

			final int glyphCount = input.readInt();
			final Map<Integer, Glyph> glyphs = new HashMap<>(glyphCount * 2);
			for (int i = 0; i < glyphCount; i++) {
				final int unicode = input.readInt();
				final float advance = input.readFloat();
				if (input.readBoolean()) {
					final PlaneBounds plane = new PlaneBounds(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());
					final AtlasBounds bounds = new AtlasBounds(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());
					glyphs.put(unicode, new Glyph(unicode, advance, plane, bounds));
					continue;
				}

				glyphs.put(unicode, new Glyph(unicode, advance, null, null));
			}

			final int unitsPerEm = input.readUnsignedShort();
			final int groups = MsdfFile.variable(input);
			final Map<Long, Float> kerning = new HashMap<>();
			int first = 0;
			for (int group = 0; group < groups; group++) {
				first += MsdfFile.variable(input);
				final int pairs = MsdfFile.variable(input);

				int second = 0;
				for (int pair = 0; pair < pairs; pair++) {
					second += MsdfFile.variable(input);
					final int value = MsdfFile.variable(input);
					kerning.put((long) first << 32 | second & 0xFFFFFFFFL, (value >>> 1 ^ -(value & 1)) / (float) unitsPerEm);
				}
			}

			return new MsdfFile(new FontInfo(atlas, metrics, glyphs, kerning), MsdfFile.image(input, width, height));
		} catch (final IOException exception) {
			throw new RuntimeException("Unable to read the msdf font", exception);
		}
	}

	private static int variable(final DataInputStream input) throws IOException {
		int value = 0;
		int shift = 0;
		int part;
		do {
			part = input.readUnsignedByte();
			value |= (part & 0x7F) << shift;
			shift += 7;
		} while ((part & 0x80) != 0);

		return value;
	}

	private static BufferedImage image(final DataInputStream input, final int width, final int height) throws IOException {
		final int stride = width * MsdfFile.BYTES;
		final byte[] row = new byte[stride];
		final byte[] previous = new byte[stride];
		final int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			final int filter = input.readUnsignedByte();
			input.readFully(row);

			for (int i = 0; i < stride; i++) {
				final int left = i >= MsdfFile.BYTES ? row[i - MsdfFile.BYTES] & 255 : 0;
				final int up = previous[i] & 255;
				final int corner = i >= MsdfFile.BYTES ? previous[i - MsdfFile.BYTES] & 255 : 0;
				switch (filter) {
				case 1:
					row[i] = (byte) (row[i] + left);
					break;
				case 2:
					row[i] = (byte) (row[i] + up);
					break;
				case 3:
					row[i] = (byte) (row[i] + (left + up) / 2);
					break;
				case 4:
					row[i] = (byte) (row[i] + MsdfFile.paeth(left, up, corner));
					break;
				default:
					break;
				}
			}

			for (int x = 0; x < width; x++) {
				pixels[x + y * width] = 255 << 24 | (row[x * MsdfFile.BYTES] & 255) << 16 | (row[x * MsdfFile.BYTES + 1] & 255) << 8 | row[x * MsdfFile.BYTES + 2] & 255;
			}

			System.arraycopy(row, 0, previous, 0, stride);
		}

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	public static int paeth(final int left, final int up, final int corner) {
		final int estimate = left + up - corner;
		final int leftDistance = Math.abs(estimate - left);
		final int upDistance = Math.abs(estimate - up);
		final int cornerDistance = Math.abs(estimate - corner);
		return leftDistance <= upDistance && leftDistance <= cornerDistance ? left : upDistance <= cornerDistance ? up : corner;
	}

}
