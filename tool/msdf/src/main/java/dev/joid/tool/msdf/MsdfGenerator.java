package dev.joid.tool.msdf;

import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import dev.joid.tool.msdf.atlas.GlyphEntry;
import dev.joid.tool.msdf.atlas.MsdfWriter;
import dev.joid.tool.msdf.atlas.Packer;
import dev.joid.tool.msdf.font.FontFile;
import dev.joid.tool.msdf.font.Glyphs;
import dev.joid.tool.msdf.font.Kerning;
import dev.joid.tool.msdf.geometry.Coloring;
import dev.joid.tool.msdf.geometry.Msdf;
import dev.joid.tool.msdf.geometry.Shape;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfGenerator {

	public static final int    WIDTH   = 2048;
	public static final int    HEIGHT  = 2048;
	public static final double RANGE   = 24D;
	public static final String CHARSET = "[32, 563]";

	public static void main(final String[] arguments) throws Exception {
		final Map<String, String> options = MsdfGenerator.options(arguments);
		if (!options.containsKey("font")) {
			System.out.println("Usage: msdf --font <file.ttf|file.otf> [--output <directory>] [--charset <file|ranges>] [--range <pixels>] [--width <pixels>] [--height <pixels>] [--size <pixels>]");
			return;
		}

		final File font = new File(options.get("font"));
		final File output = new File(options.getOrDefault("output", "output"));
		final int width = Integer.parseInt(options.getOrDefault("width", String.valueOf(MsdfGenerator.WIDTH)));
		final int height = Integer.parseInt(options.getOrDefault("height", String.valueOf(MsdfGenerator.HEIGHT)));
		final double range = Double.parseDouble(options.getOrDefault("range", String.valueOf(MsdfGenerator.RANGE)));
		final int[] codepoints = MsdfGenerator.codepoints(options.getOrDefault("charset", MsdfGenerator.CHARSET));

		MsdfGenerator.generate(font, output, codepoints, width, height, range, options.containsKey("size") ? Double.parseDouble(options.get("size")) : 0D);
	}

	public static void generate(final File file, final File output, final int[] codepoints, final int width, final int height, final double range, final double requested) throws Exception {
		final long start = System.currentTimeMillis();
		final File target = new File(output, "font.msdf");
		output.mkdirs();

		final String summary = MsdfGenerator.generate(Files.readAllBytes(file.toPath()), target, codepoints, width, height, range, requested);
		System.out.println(file.getName() + " -> " + summary + ", " + target.length() / 1024L + "kb, " + (System.currentTimeMillis() - start) + "ms");
	}

	public static String generate(final byte[] data, final File target, final int[] codepoints, final int width, final int height, final double range, final double requested) throws Exception {
		final Font font = Glyphs.load(data);
		final double[] metrics = Glyphs.metrics(font);

		final List<GlyphEntry> glyphs = new ArrayList<>();
		for (final int codepoint : codepoints) {
			if (!font.canDisplay(codepoint) || codepoint != 32 && Glyphs.code(font, codepoint) == 0) {
				continue;
			}

			final Shape shape = Glyphs.outline(font, codepoint);
			Coloring.apply(shape, 3D);
			glyphs.add(new GlyphEntry(codepoint, Glyphs.advance(font, codepoint), shape));
		}

		final double size = requested > 0D ? MsdfGenerator.measure(glyphs, requested, range, width, height) : Packer.fit(glyphs, range, width, height);
		final int[] pixels = new int[width * height];
		glyphs.parallelStream().filter(GlyphEntry::isDrawable).forEach(glyph -> {
			final int[] bitmap = Msdf.generate(glyph.getShape(), glyph.getWidth(), glyph.getHeight(), size, glyph.getLeft(), glyph.getTop(), range);
			for (int y = 0; y < glyph.getHeight(); y++) {
				System.arraycopy(bitmap, y * glyph.getWidth(), pixels, glyph.getX() + (glyph.getY() + y) * width, glyph.getWidth());
			}
		});

		final FontFile source = FontFile.read(data);
		final Kerning kerning = Kerning.read(source, font, codepoints);
		MsdfWriter.write(target, glyphs, kerning, metrics, font.getFontName(Locale.ROOT), source.getWeight(), source.isItalic(), size, range, pixels, width, height);
		return font.getFontName(Locale.ROOT) + ", weight " + source.getWeight() + (source.isItalic() ? " italic, " : ", ") + glyphs.size() + " glyphs, " + kerning.getKerning().size() + " kerning pairs, size " + size + "px";
	}

	public static int[] codepoints(final String charset) throws Exception {
		final File file = new File(charset);
		final String text = file.isFile() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : charset;
		final List<Integer> values = new ArrayList<>();
		for (final String part : text.trim().split("(?<=\\])|,(?![^\\[]*\\])")) {
			final String entry = part.replace(",", "").trim();
			if (entry.isEmpty()) {
				continue;
			}

			if (entry.startsWith("[")) {
				final String[] bounds = entry.replace("[", "").replace("]", "").split("\\s+");
				for (int codepoint = Integer.parseInt(bounds[0].trim()); codepoint <= Integer.parseInt(bounds[bounds.length - 1].trim()); codepoint++) {
					values.add(codepoint);
				}
				continue;
			}

			values.add(Integer.parseInt(entry));
		}

		final int[] codepoints = new int[values.size()];
		for (int i = 0; i < codepoints.length; i++) {
			codepoints[i] = values.get(i);
		}
		return codepoints;
	}

	private static double measure(final List<GlyphEntry> glyphs, final double size, final double range, final int width, final int height) {
		if (!Packer.pack(glyphs, size, range, width, height)) {
			throw new IllegalStateException("The glyphs do not fit in " + width + "x" + height + " at " + size + "px");
		}
		return size;
	}

	private static Map<String, String> options(final String[] arguments) {
		final Map<String, String> options = new HashMap<>();
		for (int i = 0; i < arguments.length - 1; i += 2) {
			options.put(arguments[i].replace("-", ""), arguments[i + 1]);
		}
		return options;
	}

}