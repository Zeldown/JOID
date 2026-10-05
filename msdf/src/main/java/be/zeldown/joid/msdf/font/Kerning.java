package be.zeldown.joid.msdf.font;

import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

public final class Kerning {

	private static final int KERN = 0x6B65726E;

	private final byte[] data;
	private final int[]  glyphs;
	private final Map<String, Integer> tables = new HashMap<>();
	private final Map<Long, Integer> pairs = new HashMap<>();
	@Getter private final Map<Long, Integer> kerning = new LinkedHashMap<>();

	@Getter
	private int unitsPerEm = 1000;

	private Kerning(final byte[] data, final int[] glyphs) {
		this.data = data;
		this.glyphs = glyphs;
	}

	public static Kerning read(final File file, final Font font, final int[] codepoints) throws Exception {
		final Map<Integer, Integer> glyphs = new HashMap<>();
		for (final int codepoint : codepoints) {
			glyphs.put(codepoint, Glyphs.code(font, codepoint));
		}

		final int[] sorted = new int[glyphs.size()];
		int index = 0;
		for (final int glyph : glyphs.values()) {
			sorted[index++] = glyph;
		}

		Arrays.sort(sorted);
		final Kerning reader = new Kerning(Files.readAllBytes(file.toPath()), sorted);
		reader.parse();

		for (final int first : codepoints) {
			for (final int second : codepoints) {
				final Integer value = reader.pairs.get((long) glyphs.get(first) << 32 | glyphs.get(second) & 0xFFFFFFFFL);
				if (value != null && value != 0) {
					reader.kerning.put((long) first << 32 | second & 0xFFFFFFFFL, value);
				}
			}
		}

		return reader;
	}

	private void parse() {
		final int offset = this.integer(0) == 0x74746366 ? this.integer(12) : 0;
		final int count = this.unsigned(offset + 4);
		for (int i = 0; i < count; i++) {
			final int record = offset + 12 + i * 16;
			this.tables.put(new String(this.data, record, 4, StandardCharsets.US_ASCII), this.integer(record + 8));
		}

		final Integer head = this.tables.get("head");
		if (head != null) {
			this.unitsPerEm = this.unsigned(head + 18);
		}

		final Integer gpos = this.tables.get("GPOS");
		if (gpos != null) {
			this.parseGpos(gpos);
		}

		final Integer kern = this.tables.get("kern");
		if (kern != null && this.pairs.isEmpty()) {
			this.parseKern(kern);
		}
	}

	private void parseKern(final int offset) {
		final int count = this.unsigned(offset + 2);
		int subtable = offset + 4;
		for (int i = 0; i < count; i++) {
			final int length = this.unsigned(subtable + 2);
			final int coverage = this.unsigned(subtable + 4);
			if ((coverage >> 8 & 255) == 0 && (coverage & 1) == 1) {
				final int pairs = this.unsigned(subtable + 6);
				for (int pair = 0; pair < pairs; pair++) {
					final int record = subtable + 14 + pair * 6;
					this.pair(this.pairs, this.unsigned(record), this.unsigned(record + 2), this.signed(record + 4));
				}
			}

			subtable += length;
		}
	}

	private void parseGpos(final int offset) {
		final int featureList = offset + this.unsigned(offset + 6);
		final int lookupList = offset + this.unsigned(offset + 8);
		final int features = this.unsigned(featureList);
		final Set<Integer> parsed = new HashSet<>();

		for (int i = 0; i < features; i++) {
			final int record = featureList + 2 + i * 6;
			if (this.integer(record) == Kerning.KERN) {
				this.parseFeature(lookupList, featureList + this.unsigned(record + 4), parsed);
			}
		}
	}

	private void parseFeature(final int lookupList, final int feature, final Set<Integer> parsed) {
		final int lookups = this.unsigned(feature + 2);
		for (int i = 0; i < lookups; i++) {
			final int index = this.unsigned(feature + 4 + i * 2);
			if (parsed.add(index)) {
				this.parseLookup(lookupList + this.unsigned(lookupList + 2 + index * 2));
			}
		}
	}

	private void parseLookup(final int offset) {
		final Map<Long, Integer> lookup = new HashMap<>();
		final int type = this.unsigned(offset);
		final int subtables = this.unsigned(offset + 4);
		for (int i = 0; i < subtables; i++) {
			this.parseSubtable(lookup, type, offset + this.unsigned(offset + 6 + i * 2));
		}

		for (final Map.Entry<Long, Integer> entry : lookup.entrySet()) {
			if (entry.getValue() != 0) {
				this.pairs.merge(entry.getKey(), entry.getValue(), Integer::sum);
			}
		}
	}

	private void parseSubtable(final Map<Long, Integer> lookup, final int type, final int offset) {
		if (type == 9) {
			this.parseSubtable(lookup, this.unsigned(offset + 2), offset + this.integer(offset + 4));
			return;
		}

		if (type != 2) {
			return;
		}

		final int format = this.unsigned(offset);
		final int[] coverage = this.coverage(offset + this.unsigned(offset + 2));
		final int first = this.unsigned(offset + 4);
		final int second = this.unsigned(offset + 6);
		final int firstSize = Kerning.size(first);
		final int secondSize = Kerning.size(second);
		final int advance = Kerning.advance(first);
		if (advance < 0) {
			return;
		}

		if (format == 1) {
			final int sets = this.unsigned(offset + 8);
			for (int i = 0; i < sets && i < coverage.length; i++) {
				final int set = offset + this.unsigned(offset + 10 + i * 2);
				final int values = this.unsigned(set);
				for (int value = 0; value < values; value++) {
					final int record = set + 2 + value * (2 + firstSize + secondSize);
					this.pair(lookup, coverage[i], this.unsigned(record), this.signed(record + 2 + advance));
				}
			}
			return;
		}

		if (format != 2) {
			return;
		}

		final int firstCount = this.unsigned(offset + 12);
		final int secondCount = this.unsigned(offset + 14);
		final int[] firstClassMap = this.classDefinition(offset + this.unsigned(offset + 8));
		final int[] secondClassMap = this.classDefinition(offset + this.unsigned(offset + 10));

		for (final int glyph : coverage) {
			final int firstClass = glyph < firstClassMap.length ? firstClassMap[glyph] : 0;
			if (firstClass >= firstCount) {
				continue;
			}

			for (final int glyphSecond : this.glyphs) {
				final int secondClass = glyphSecond < secondClassMap.length ? secondClassMap[glyphSecond] : 0;
				if (secondClass >= secondCount) {
					continue;
				}

				final int record = offset + 16 + (firstClass * secondCount + secondClass) * (firstSize + secondSize);
				this.pair(lookup, glyph, glyphSecond, this.signed(record + advance));
			}
		}
	}

	private int[] coverage(final int offset) {
		final int format = this.unsigned(offset);
		if (format == 1) {
			final int count = this.unsigned(offset + 2);
			final int[] glyphs = new int[count];
			for (int i = 0; i < count; i++) {
				glyphs[i] = this.unsigned(offset + 4 + i * 2);
			}
			return glyphs;
		}

		final int ranges = this.unsigned(offset + 2);
		int total = 0;
		for (int i = 0; i < ranges; i++) {
			final int record = offset + 4 + i * 6;
			total += this.unsigned(record + 2) - this.unsigned(record) + 1;
		}

		final int[] glyphs = new int[total];
		int index = 0;
		for (int i = 0; i < ranges; i++) {
			final int record = offset + 4 + i * 6;
			for (int glyph = this.unsigned(record); glyph <= this.unsigned(record + 2); glyph++) {
				glyphs[index++] = glyph;
			}
		}
		return glyphs;
	}

	private int[] classDefinition(final int offset) {
		final int format = this.unsigned(offset);
		if (format == 1) {
			final int start = this.unsigned(offset + 2);
			final int count = this.unsigned(offset + 4);
			final int[] classes = new int[start + count];
			for (int i = 0; i < count; i++) {
				classes[start + i] = this.unsigned(offset + 6 + i * 2);
			}
			return classes;
		}

		final int ranges = this.unsigned(offset + 2);
		int maximum = 0;
		for (int i = 0; i < ranges; i++) {
			maximum = Math.max(maximum, this.unsigned(offset + 4 + i * 6 + 2));
		}

		final int[] classes = new int[maximum + 1];
		for (int i = 0; i < ranges; i++) {
			final int record = offset + 4 + i * 6;
			final int value = this.unsigned(record + 4);
			for (int glyph = this.unsigned(record); glyph <= this.unsigned(record + 2); glyph++) {
				classes[glyph] = value;
			}
		}
		return classes;
	}

	private void pair(final Map<Long, Integer> target, final int first, final int second, final int value) {
		if (Arrays.binarySearch(this.glyphs, first) >= 0 && Arrays.binarySearch(this.glyphs, second) >= 0) {
			target.putIfAbsent((long) first << 32 | second & 0xFFFFFFFFL, value);
		}
	}

	private int integer(final int offset) {
		return (this.data[offset] & 255) << 24 | (this.data[offset + 1] & 255) << 16 | (this.data[offset + 2] & 255) << 8 | this.data[offset + 3] & 255;
	}

	private int unsigned(final int offset) {
		return (this.data[offset] & 255) << 8 | this.data[offset + 1] & 255;
	}

	private int signed(final int offset) {
		return (short) this.unsigned(offset);
	}

	private static int size(final int format) {
		return Integer.bitCount(format & 0xFF) * 2;
	}

	private static int advance(final int format) {
		return (format & 0x0004) == 0 ? -1 : Integer.bitCount(format & 0x0003) * 2;
	}

}