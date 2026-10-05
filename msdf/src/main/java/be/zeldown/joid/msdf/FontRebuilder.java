package be.zeldown.joid.msdf;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class FontRebuilder {

	public static void main(final String[] arguments) throws Exception {
		final File sources = new File(arguments[0]);
		final File resources = new File(arguments[1]);
		final File list = new File(arguments[2]);
		final File charset = new File(arguments[3]);
		final int atlas = arguments.length > 4 ? Integer.parseInt(arguments[4]) : 2048;
		final double range = arguments.length > 5 ? Double.parseDouble(arguments[5]) : 24D;

		final int[] codepoints = MsdfGenerator.codepoints(charset.getAbsolutePath());
		final List<String> entries = Files.readAllLines(list.toPath(), StandardCharsets.UTF_8);
		for (final String entry : entries) {
			final String line = entry.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			final String[] parts = line.split("\\|");
			final File source = new File(sources, parts[1].trim());
			if (!source.isFile()) {
				throw new IllegalStateException("Missing font file: " + source.getAbsolutePath());
			}

			MsdfGenerator.generate(source, new File(resources, parts[0].trim()), codepoints, atlas, atlas, range, 0D);
		}
	}

}