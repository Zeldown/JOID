package dev.joid.test.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotComparison {

	public static void main(final String[] arguments) {
		final File report = new File(arguments[0], "report.html");
		final File reference = new File(arguments[1]);
		final File[] shots = reference.listFiles((directory, name) -> name.endsWith(".png"));
		if (shots == null || shots.length == 0) {
			System.err.println("[JOID] No snapshot rendered in " + reference + ", render the snapshots first");
			System.exit(1);
		}
		Arrays.sort(shots);

		final int tolerance = 1;
		final List<SnapshotEntry> entries = new ArrayList<>();
		final List<String> failures = new ArrayList<>();
		for (int i = 2; i < arguments.length; i++) {
			final File candidates = new File(arguments[i]);
			int identical = 0;
			for (final File shot : shots) {
				final String name = candidates.getName() + "/" + shot.getName();
				final File candidate = new File(candidates, shot.getName());
				if (!candidate.exists()) {
					failures.add(name + ": not rendered");
					continue;
				}

				final SnapshotEntry entry = SnapshotEntry.compare(name, SnapshotImage.read(candidate), candidate, shot, tolerance, reference.getName(), failures);
				entries.add(entry);
				if (entry.getStatus() == SnapshotStatus.IDENTICAL) {
					identical++;
				}
			}
			System.out.println("[JOID] " + candidates.getName() + ": " + identical + "/" + shots.length + " snapshots identical to " + reference.getName());
		}

		SnapshotReport.write(report, "JOID cross-backend comparison", tolerance, entries);
		if (!failures.isEmpty()) {
			failures.forEach(failure -> System.err.println("[JOID] " + failure));
			System.err.println("[JOID] Report: " + report.toPath().toUri().toASCIIString());
			System.exit(1);
		}
	}

}