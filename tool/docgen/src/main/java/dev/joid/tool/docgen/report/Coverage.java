package dev.joid.tool.docgen.report;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.GsonBuilder;

import lombok.Getter;

@Getter
public final class Coverage {

	private final Map<String, int[]>        families;
	private final Map<String, List<String>> missing;
	private final List<String>              unknown;
	private final List<String>              broken;

	private Coverage() {
		this.families = new TreeMap<>();
		this.missing  = new TreeMap<>();
		this.unknown  = new ArrayList<>();
		this.broken   = new ArrayList<>();
	}

	public static Coverage create() {
		return new Coverage();
	}

	public void expect(final String family, final String file, final String key, final boolean described) {
		final int[] counts = this.families.computeIfAbsent(family, name -> new int[2]);
		counts[0]++;
		if (described) {
			counts[1]++;
			return;
		}
		this.missing.computeIfAbsent(file, name -> new ArrayList<>()).add(key);
	}

	public void unknown(final String file, final String key) {
		this.unknown.add(file + ": no documented member matches \"" + key + "\"");
	}

	public void masked(final String file, final String key, final String property) {
		this.unknown.add(file + ": \"" + key + "\" is never shown, the property \"" + property + "\" shows the entry of another of its members");
	}

	public void broken(final String file, final String link) {
		this.broken.add(file + ": " + link);
	}

	public int getExpected() {
		return this.families.values().stream().mapToInt(counts -> counts[0]).sum();
	}

	public int getDescribed() {
		return this.families.values().stream().mapToInt(counts -> counts[1]).sum();
	}

	public int getMissingCount() {
		return this.getExpected() - this.getDescribed();
	}

	public boolean hasErrors() {
		return this.getMissingCount() > 0 || !this.unknown.isEmpty() || !this.broken.isEmpty();
	}

	public String summary() {
		final int expected = this.getExpected();
		final double percent = expected == 0 ? 100D : this.getDescribed() * 100D / expected;
		return String.format(Locale.ROOT, "%d/%d descriptions (%.1f%%), %d missing, %d unknown keys, %d broken links", this.getDescribed(), expected, percent, this.getMissingCount(), this.unknown.size(), this.broken.size());
	}

	public void write(final File directory) throws IOException {
		directory.mkdirs();
		final StringBuilder markdown = new StringBuilder("# Reference coverage\n\n").append(this.summary()).append("\n\n| Family | Described | Expected | Coverage |\n|---|---|---|---|\n");
		for (final Map.Entry<String, int[]> family : this.families.entrySet()) {
			final int[] counts = family.getValue();
			markdown.append(String.format(Locale.ROOT, "| %s | %d | %d | %.1f%% |%n", family.getKey(), counts[1], counts[0], counts[0] == 0 ? 100D : counts[1] * 100D / counts[0]));
		}

		Coverage.section(markdown, "Unknown keys", this.unknown);
		Coverage.section(markdown, "Broken links", this.broken);
		markdown.append("\n## Missing descriptions\n");
		for (final Map.Entry<String, List<String>> file : this.missing.entrySet()) {
			markdown.append("\n### ").append(file.getKey()).append("\n\n");
			file.getValue().forEach(key -> markdown.append("- `").append(key).append("`\n"));
		}

		Files.write(new File(directory, "coverage.md").toPath(), markdown.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(directory, "coverage.json").toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(this).getBytes(StandardCharsets.UTF_8));
	}

	private static void section(final StringBuilder markdown, final String title, final List<String> lines) {
		if (lines.isEmpty()) {
			return;
		}

		markdown.append("\n## ").append(title).append("\n\n");
		lines.forEach(line -> markdown.append("- ").append(line).append('\n'));
	}

}