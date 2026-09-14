package be.zeldown.joid.test.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SnapshotComparison {

	private static final int TOLERANCE  = 8;
	private static final int MAX_PIXELS = 2000;

	public static void main(final String[] arguments) {
		final File root = new File(arguments[0]);
		final String reference = arguments[1];
		final File[] shots = new File(root, reference).listFiles((directory, name) -> name.endsWith(".png"));
		if (shots == null || shots.length == 0) {
			System.err.println("No snapshot rendered by the " + reference + " backend in " + root);
			System.exit(1);
		}
		Arrays.sort(shots);

		final List<String> failures = new ArrayList<>();
		for (int i = 2; i < arguments.length; i++) {
			final String backend = arguments[i];
			int identical = 0;
			for (final File shot : shots) {
				final File candidate = new File(new File(root, backend), shot.getName());
				if (!candidate.exists()) {
					failures.add(backend + "/" + shot.getName() + ": not rendered");
					continue;
				}

				final SnapshotDifference difference = SnapshotImage.read(candidate).compare(SnapshotImage.read(shot), SnapshotComparison.TOLERANCE);
				if (difference.getPixels() == 0) {
					identical++;
					continue;
				}

				System.out.println(String.format("%-8s %-36s %8d pixels differ from %s, maximum channel delta %d", backend, shot.getName(), difference.getPixels(), reference, difference.getMaximum()));
				if (difference.getPixels() > SnapshotComparison.MAX_PIXELS) {
					difference.getImage().write(new File(root, "cross/" + backend + "/" + shot.getName()));
					failures.add(backend + "/" + shot.getName() + ": " + difference.getPixels() + " pixels differ from " + reference);
				}
			}
			System.out.println(backend + ": " + identical + "/" + shots.length + " snapshots identical to " + reference);
		}

		if (!failures.isEmpty()) {
			failures.forEach(System.err::println);
			System.exit(1);
		}
	}

}