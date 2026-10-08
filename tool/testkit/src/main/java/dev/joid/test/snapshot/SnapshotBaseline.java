package dev.joid.test.snapshot;

import java.io.File;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotBaseline {

	public static void main(final String[] arguments) throws ReflectiveOperationException {
		final ISnapshotBackend backend = (ISnapshotBackend) Class.forName(arguments[0]).newInstance();
		final File output = new File(arguments[1]);
		final File[] previous = output.listFiles((directory, name) -> name.endsWith(".png"));
		if (previous != null) {
			for (final File shot : previous) {
				shot.delete();
			}
		}

		final SnapshotRunner runner = SnapshotRunner.start(backend);
		int count = 0;
		for (final String scenario : SnapshotRunner.getScenarios()) {
			for (final Map.Entry<String, SnapshotImage> shot : runner.run(scenario).entrySet()) {
				shot.getValue().write(new File(output, shot.getKey() + ".png"));
				count++;
			}
		}

		System.out.println(runner.getRenderer() + ": " + count + " snapshots rendered into " + output);
		runner.stop();
		System.exit(0);
	}

}