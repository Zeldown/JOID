package dev.joid.lib.font.impl.msdf.dto.source;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.dto.MsdfAtlas;
import dev.joid.lib.font.impl.msdf.dto.MsdfBounds;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.dto.MsdfGlyph;
import dev.joid.lib.font.impl.msdf.dto.MsdfMetrics;
import dev.joid.tool.msdf.atlas.MsdfWriter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfBinarySource extends MsdfSource {

	private static final int BYTES = 3;

	private static final byte[] MAGIC = {'J', 'O', 'I', 'D', 'M', 'S', 'D', 'F'};

	private final Asset asset;

	public static @NonNull MsdfBinarySource of(final @NonNull Object handle) {
		return new MsdfBinarySource(Asset.of(handle));
	}

	@Override
	public @NonNull String describe() {
		return "read from a .msdf file";
	}

	@Override
	protected @NonNull MsdfFontFace parse() throws IOException {
		try (InputStream stream = this.asset.open()) {
			return MsdfBinarySource.parse(stream);
		}
	}

	private static @NonNull MsdfFontFace parse(final @NonNull InputStream stream) throws IOException {
		final byte[] magic = new byte[MsdfBinarySource.MAGIC.length];
		new DataInputStream(stream).readFully(magic);
		if (!Arrays.equals(magic, MsdfBinarySource.MAGIC)) {
			throw new IOException("Not a JOID msdf font");
		}

		final DataInputStream input = new DataInputStream(new InflaterInputStream(stream));
		final int version = input.readUnsignedByte();
		if (version != MsdfWriter.VERSION) {
			throw new IOException("Unsupported msdf font version " + version + ", only the version " + MsdfWriter.VERSION + " loads: generate it again");
		}

		final FontWeight weight = FontWeight.of(input.readUnsignedShort());
		final boolean italic = input.readBoolean();
		final String name = input.readUTF();
		final int width = input.readInt();
		final int height = input.readInt();
		final float distanceRange = input.readFloat();
		final float size = input.readFloat();
		final MsdfAtlas atlas = new MsdfAtlas(width, height, size, distanceRange);
		final MsdfMetrics metrics = new MsdfMetrics(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());

		final int glyphCount = input.readInt();
		final Map<Integer, MsdfGlyph> glyphs = new HashMap<>(glyphCount * 2);
		for (int i = 0; i < glyphCount; i++) {
			final int codepoint = input.readInt();
			final float advance = input.readFloat();
			if (!input.readBoolean()) {
				glyphs.put(codepoint, new MsdfGlyph(codepoint, advance, null, null));
				continue;
			}

			final MsdfBounds planeBounds = new MsdfBounds(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());
			glyphs.put(codepoint, new MsdfGlyph(codepoint, advance, planeBounds, new MsdfBounds(input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort())));
		}

		final int unitsPerEm = input.readUnsignedShort();
		final int groups = MsdfBinarySource.variable(input);
		final Map<Long, Float> kerningPairs = new HashMap<>();
		int first = 0;
		for (int group = 0; group < groups; group++) {
			first += MsdfBinarySource.variable(input);
			final int pairs = MsdfBinarySource.variable(input);

			int second = 0;
			for (int pair = 0; pair < pairs; pair++) {
				second += MsdfBinarySource.variable(input);
				final int value = MsdfBinarySource.variable(input);
				kerningPairs.put(MsdfFontFace.pair(first, second), (value >>> 1 ^ -(value & 1)) / (float) unitsPerEm);
			}
		}

		return MsdfFontFace.create(atlas, metrics, glyphs, kerningPairs, MsdfBinarySource.image(input, width, height), name, weight, italic);
	}

	private static int paeth(final int left, final int up, final int corner) {
		final int estimate = left + up - corner;
		final int leftDistance = Math.abs(estimate - left);
		final int upDistance = Math.abs(estimate - up);
		final int cornerDistance = Math.abs(estimate - corner);
		return leftDistance <= upDistance && leftDistance <= cornerDistance ? left : upDistance <= cornerDistance ? up : corner;
	}

	private static int variable(final @NonNull DataInputStream input) throws IOException {
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

	private static @NonNull BufferedImage image(final @NonNull DataInputStream input, final int width, final int height) throws IOException {
		final int stride = width * MsdfBinarySource.BYTES;
		final byte[] row = new byte[stride];
		final byte[] previous = new byte[stride];
		final int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			final int filter = input.readUnsignedByte();
			input.readFully(row);

			for (int i = 0; i < stride; i++) {
				final int left = i >= MsdfBinarySource.BYTES ? row[i - MsdfBinarySource.BYTES] & 255 : 0;
				final int up = previous[i] & 255;
				final int corner = i >= MsdfBinarySource.BYTES ? previous[i - MsdfBinarySource.BYTES] & 255 : 0;
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
					row[i] = (byte) (row[i] + MsdfBinarySource.paeth(left, up, corner));
					break;
				default:
					break;
				}
			}

			for (int x = 0; x < width; x++) {
				pixels[x + y * width] = 255 << 24 | (row[x * MsdfBinarySource.BYTES] & 255) << 16 | (row[x * MsdfBinarySource.BYTES + 1] & 255) << 8 | row[x * MsdfBinarySource.BYTES + 2] & 255;
			}

			System.arraycopy(row, 0, previous, 0, stride);
		}

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

}