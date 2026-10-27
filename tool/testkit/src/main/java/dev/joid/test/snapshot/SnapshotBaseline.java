package dev.joid.test.snapshot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotBaseline {

	public static void main(final String[] arguments) {
		final ISnapshotBackend backend;
		try {
			final Constructor<?> constructor = Class.forName(arguments[0]).getDeclaredConstructor();
			constructor.setAccessible(true);
			backend = (ISnapshotBackend) constructor.newInstance();
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException("The snapshot backend " + arguments[0] + " cannot be created: the class does not exist, check the name and the classpath", e);
		} catch (final InvocationTargetException e) {
			throw new IllegalArgumentException("The snapshot backend " + arguments[0] + " cannot be created: its constructor threw " + e.getCause(), e.getCause());
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException("The snapshot backend " + arguments[0] + " cannot be created: give it a constructor without arguments, of any visibility", e);
		}

		final File output = new File(arguments[1]);
		final File[] previous = output.listFiles((directory, name) -> name.endsWith(".png"));
		if (previous != null) {
			for (final File shot : previous) {
				shot.delete();
			}
		}

		final SnapshotRunner runner = SnapshotRunner.start(backend);
		runner.writeRenderer(output);
		int count = 0;
		for (final String scenario : SnapshotRunner.getScenarios()) {
			for (final Map.Entry<String, SnapshotImage> shot : runner.run(scenario).entrySet()) {
				shot.getValue().write(new File(output, shot.getKey() + ".png"));
				count++;
			}
		}

		System.out.println("[JOID] " + runner.getRenderer() + ": " + count + " snapshots rendered into " + output);
		runner.stop();
		System.exit(0);
	}

}