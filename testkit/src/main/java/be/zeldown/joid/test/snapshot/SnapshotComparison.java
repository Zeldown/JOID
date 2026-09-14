package be.zeldown.joid.test.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SnapshotComparison {

	public static void main(final String[] arguments) {
		final File report = new File(arguments[0], "report.html");
		final File reference = new File(arguments[1]);
		final File[] shots = reference.listFiles((directory, name) -> name.endsWith(".png"));
		if (shots == null || shots.length == 0) {
			System.err.println("No snapshot rendered in " + reference);
			System.exit(1);
		}
		Arrays.sort(shots);

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

				final SnapshotDifference difference = SnapshotImage.read(candidate).compare(SnapshotImage.read(shot), 1);
				if (difference.getPixels() == 0) {
					identical++;
					entries.add(SnapshotEntry.create(name, SnapshotStatus.IDENTICAL, 0, shot, candidate));
					continue;
				}

				entries.add(SnapshotEntry.create(name, SnapshotStatus.DIFFERENT, difference.getPixels(), shot, candidate));
				failures.add(name + ": " + difference.getPixels() + " pixels differ from " + reference.getName() + ", maximum channel delta " + difference.getMaximum());
			}
			System.out.println(candidates.getName() + ": " + identical + "/" + shots.length + " snapshots identical to " + reference.getName());
		}

		SnapshotReport.write(report, "JOID cross-backend comparison", entries);
		if (!failures.isEmpty()) {
			failures.forEach(System.err::println);
			System.err.println("Report: " + report.toPath().toUri().toASCIIString());
			System.exit(1);
		}
	}

}