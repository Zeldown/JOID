package be.zeldown.joid.msdf.atlas;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import be.zeldown.joid.msdf.font.Kerning;

public final class MsdfWriter {

	private static final int BYTES   = 3;
	private static final int VERSION = 2;

	private static final byte[] MAGIC = {'J', 'O', 'I', 'D', 'M', 'S', 'D', 'F'};

	public static void write(final File file, final List<GlyphEntry> glyphs, final Kerning kerning, final double[] metrics, final double size, final double range, final int[] pixels, final int width, final int height) throws IOException {
		try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
			output.write(MsdfWriter.MAGIC);

			final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
			try (DataOutputStream data = new DataOutputStream(new DeflaterOutputStream(output, deflater, 1 << 16))) {
				data.writeByte(MsdfWriter.VERSION);
				data.writeInt(width);
				data.writeInt(height);
				data.writeFloat((float) range);
				data.writeFloat((float) size);
				for (final double metric : metrics) {
					data.writeFloat((float) metric);
				}

				data.writeInt(glyphs.size());
				for (final GlyphEntry glyph : glyphs) {
					data.writeInt(glyph.getCodepoint());
					data.writeFloat((float) glyph.getAdvance());
					data.writeBoolean(glyph.isDrawable());
					if (glyph.isDrawable()) {
						data.writeFloat((float) glyph.getLeft());
						data.writeFloat((float) glyph.getBottom());
						data.writeFloat((float) glyph.getRight());
						data.writeFloat((float) glyph.getTop());
						data.writeFloat(glyph.getX() - 0.5F);
						data.writeFloat(height - glyph.getY() - glyph.getHeight() - 0.5F);
						data.writeFloat(glyph.getX() + glyph.getWidth() - 0.5F);
						data.writeFloat(height - glyph.getY() - 0.5F);
					}
				}

				data.writeShort(kerning.getUnitsPerEm());
				MsdfWriter.kerning(data, kerning.getKerning());

				data.write(MsdfWriter.scanlines(pixels, width, height));
			}
			deflater.end();
		}
	}

	private static void kerning(final DataOutputStream data, final Map<Long, Integer> pairs) throws IOException {
		final Map<Integer, Map<Integer, Integer>> groups = new TreeMap<>();
		for (final Map.Entry<Long, Integer> entry : pairs.entrySet()) {
			groups.computeIfAbsent((int) (entry.getKey() >> 32), key -> new TreeMap<>()).put(entry.getKey().intValue(), entry.getValue());
		}

		MsdfWriter.variable(data, groups.size());
		int first = 0;
		for (final Map.Entry<Integer, Map<Integer, Integer>> group : groups.entrySet()) {
			MsdfWriter.variable(data, group.getKey() - first);
			MsdfWriter.variable(data, group.getValue().size());
			first = group.getKey();

			int second = 0;
			for (final Map.Entry<Integer, Integer> entry : group.getValue().entrySet()) {
				MsdfWriter.variable(data, entry.getKey() - second);
				MsdfWriter.variable(data, entry.getValue() << 1 ^ entry.getValue() >> 31);
				second = entry.getKey();
			}
		}
	}

	private static void variable(final DataOutputStream data, final int value) throws IOException {
		int remaining = value;
		while ((remaining & ~0x7F) != 0) {
			data.writeByte(remaining & 0x7F | 0x80);
			remaining >>>= 7;
		}

		data.writeByte(remaining);
	}

	private static byte[] scanlines(final int[] pixels, final int width, final int height) {
		final int stride = width * MsdfWriter.BYTES;
		final byte[] output = new byte[(stride + 1) * height];
		final byte[] row = new byte[stride];
		final byte[] previous = new byte[stride];
		final byte[][] candidates = new byte[5][stride];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int pixel = pixels[x + y * width];
				row[x * MsdfWriter.BYTES] = (byte) (pixel >> 16);
				row[x * MsdfWriter.BYTES + 1] = (byte) (pixel >> 8);
				row[x * MsdfWriter.BYTES + 2] = (byte) pixel;
			}

			int best = 0;
			long bestScore = Long.MAX_VALUE;
			for (int filter = 0; filter < candidates.length; filter++) {
				long score = 0L;
				for (int i = 0; i < stride; i++) {
					final int left = i >= MsdfWriter.BYTES ? row[i - MsdfWriter.BYTES] & 255 : 0;
					final int up = previous[i] & 255;
					final int corner = i >= MsdfWriter.BYTES ? previous[i - MsdfWriter.BYTES] & 255 : 0;
					final int value = row[i] & 255;
					final int filtered;
					switch (filter) {
					case 1:
						filtered = value - left;
						break;
					case 2:
						filtered = value - up;
						break;
					case 3:
						filtered = value - (left + up) / 2;
						break;
					case 4:
						filtered = value - MsdfWriter.paeth(left, up, corner);
						break;
					default:
						filtered = value;
						break;
					}

					candidates[filter][i] = (byte) filtered;
					score += Math.abs((byte) filtered);
				}

				if (score < bestScore) {
					bestScore = score;
					best = filter;
				}
			}

			final int offset = y * (stride + 1);
			output[offset] = (byte) best;
			System.arraycopy(candidates[best], 0, output, offset + 1, stride);
			System.arraycopy(row, 0, previous, 0, stride);
		}

		return output;
	}

	private static int paeth(final int left, final int up, final int corner) {
		final int estimate = left + up - corner;
		final int leftDistance = Math.abs(estimate - left);
		final int upDistance = Math.abs(estimate - up);
		final int cornerDistance = Math.abs(estimate - corner);
		return leftDistance <= upDistance && leftDistance <= cornerDistance ? left : upDistance <= cornerDistance ? up : corner;
	}

}