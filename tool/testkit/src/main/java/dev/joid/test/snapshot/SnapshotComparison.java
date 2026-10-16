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
			System.err.println("No snapshot rendered in " + reference + ", render the snapshots first");
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

				final SnapshotImage expected = SnapshotImage.read(shot);
				final SnapshotImage actual = SnapshotImage.read(candidate);
				final SnapshotDifference difference = actual.compare(expected, tolerance);
				if (difference.getPixels() == 0) {
					identical++;
					entries.add(SnapshotEntry.create(name, SnapshotStatus.IDENTICAL, 0, candidate, shot));
					continue;
				}

				entries.add(SnapshotEntry.create(name, SnapshotStatus.DIFFERENT, difference.getPixels(), candidate, shot));
				if (actual.getWidth() != expected.getWidth() || actual.getHeight() != expected.getHeight()) {
					failures.add(name + ": " + actual.getWidth() + "x" + actual.getHeight() + " instead of the " + expected.getWidth() + "x" + expected.getHeight() + " of " + reference.getName());
				} else {
					failures.add(name + ": " + difference.getPixels() + " pixels differ from " + reference.getName() + ", maximum channel delta " + difference.getMaximum());
				}
			}
			System.out.println(candidates.getName() + ": " + identical + "/" + shots.length + " snapshots identical to " + reference.getName());
		}

		SnapshotReport.write(report, "JOID cross-backend comparison", tolerance, entries);
		if (!failures.isEmpty()) {
			failures.forEach(System.err::println);
			System.err.println("Report: " + report.toPath().toUri().toASCIIString());
			System.exit(1);
		}
	}

}