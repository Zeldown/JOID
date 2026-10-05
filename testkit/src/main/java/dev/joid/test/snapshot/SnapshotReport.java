package dev.joid.test.snapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotReport {

	private static final String TEMPLATE = "/report/report.html";

	public static void write(final @NonNull File file, final @NonNull String title, final @NonNull List<SnapshotEntry> entries) {
		final File data = new File(file.getParentFile(), "report");
		data.mkdirs();

		final StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < entries.size(); i++) {
			final SnapshotEntry entry = entries.get(i);
			final String reference = entry.getStatus() == SnapshotStatus.DIFFERENT ? "\"" + SnapshotReport.encode(entry.getReference()) + "\"" : "null";
			SnapshotReport.writeText(new File(data, i + ".js"), "joidShot(" + i + ", \"" + SnapshotReport.encode(entry.getRender()) + "\", " + reference + ");");

			json.append(i == 0 ? "" : ",");
			json.append("{\"name\":\"").append(entry.getName().replace("\\", "\\\\").replace("\"", "\\\""));
			json.append("\",\"status\":\"").append(entry.getStatus().name().toLowerCase(Locale.ROOT));
			json.append("\",\"pixels\":").append(entry.getPixels()).append("}");
		}
		json.append("]");

		final String safeTitle = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		SnapshotReport.writeText(file, SnapshotReport.readTemplate().replace("__TITLE__", safeTitle).replace("__ENTRIES__", json.toString()));
	}

	private static String readTemplate() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(SnapshotReport.class.getResourceAsStream(SnapshotReport.TEMPLATE), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String encode(final File file) {
		try {
			return "data:image/png;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void writeText(final File file, final String text) {
		try {
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}