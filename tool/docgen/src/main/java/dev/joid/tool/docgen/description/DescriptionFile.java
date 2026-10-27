package dev.joid.tool.docgen.description;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.joid.tool.docgen.model.TypeNames;
import lombok.Getter;

@Getter
public final class DescriptionFile {

	private final File                file;
	private final String              fqcn;
	private final Map<String, String> front;
	private final Map<String, String> entries;

	private String summary;
	private String description;

	private DescriptionFile(final File file, final String fqcn) {
		this.file        = file;
		this.fqcn        = fqcn;
		this.front       = new LinkedHashMap<>();
		this.entries     = new LinkedHashMap<>();
		this.summary     = "";
		this.description = "";
	}

	public static DescriptionFile read(final File file) throws IOException {
		final String name = file.getName();
		final DescriptionFile description = new DescriptionFile(file, name.substring(0, name.length() - 3));
		final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

		int index = 0;
		if (!lines.isEmpty() && "---".equals(lines.get(0).trim())) {
			index = 1;
			while (index < lines.size() && !"---".equals(lines.get(index).trim())) {
				final String line = lines.get(index);
				final int separator = line.indexOf(':');
				if (separator > 0) {
					description.front.put(line.substring(0, separator).trim(), DescriptionFile.unquote(line.substring(separator + 1).trim()));
				}
				index++;
			}
			index++;
		}
		description.summary = description.front.getOrDefault("summary", "");

		String key = null;
		boolean fence = false;
		final StringBuilder text = new StringBuilder();
		for (; index < lines.size(); index++) {
			final String line = lines.get(index);
			if (line.trim().startsWith("```")) {
				fence = !fence;
			}

			if (!fence && line.startsWith("## ")) {
				description.store(key, text.toString().trim());
				key = TypeNames.normalizeKey(line.substring(3));
				text.setLength(0);
				continue;
			}
			text.append(line).append('\n');
		}
		description.store(key, text.toString().trim());
		return description;
	}

	public boolean has(final String key) {
		final String text = this.entries.get(key);
		return text != null && !text.isEmpty();
	}

	public String get(final String key) {
		return this.entries.get(key);
	}

	public String option(final String key) {
		final String value = this.front.get(key);
		return value == null || value.isEmpty() ? null : value;
	}

	private void store(final String key, final String text) {
		if (key == null) {
			this.description = text;
		} else {
			this.entries.put(key, text);
		}
	}

	private static String unquote(final String value) {
		if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

}