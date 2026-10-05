package be.zeldown.joid.test.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import lombok.NonNull;

public abstract class SnapshotSuite {

	private static final List<SnapshotEntry> ENTRIES = new ArrayList<>();

	private static SnapshotRunner runner;

	protected abstract @NonNull ISnapshotBackend createBackend();

	@AfterClass
	public static void stopRunner() {
		SnapshotSuite.pruneReferences();
		if (SnapshotSuite.runner == null) {
			return;
		}

		SnapshotReport.write(new File(SnapshotSettings.getOutput(), "report.html"), "JOID snapshots - " + SnapshotSuite.runner.getRenderer(), SnapshotSuite.ENTRIES);
		SnapshotSuite.ENTRIES.clear();
		SnapshotSuite.runner.stop();
		SnapshotSuite.runner = null;
	}

	@Test
	public void matchesDevSnapshots() {
		this.verify("dev");
	}

	@Test
	public void matchesPopupSnapshots() {
		this.verify("popup");
	}

	@Test
	public void matchesVideoSnapshots() {
		this.verify("video");
	}

	@Test
	public void matchesStaticSnapshots() {
		this.verify("static");
	}

	@Test
	public void matchesWindowSnapshots() {
		this.verify("window");
	}

	@Test
	public void matchesTransitionSnapshots() {
		this.verify("transition");
	}

	@Test
	public void matchesInteractionSnapshots() {
		this.verify("interaction");
	}

	private SnapshotRunner getRunner() {
		if (SnapshotSuite.runner == null) {
			SnapshotSuite.runner = SnapshotRunner.start(this.createBackend());
		}
		return SnapshotSuite.runner;
	}

	private void verify(final String scenario) {
		final File references = new File(SnapshotSettings.getReferences(), this.getRunner().getRenderer());
		final File output = SnapshotSettings.getOutput();
		final boolean update = SnapshotSettings.isUpdate();

		final List<String> failures = new ArrayList<>();
		for (final Map.Entry<String, SnapshotImage> shot : this.getRunner().run(scenario).entrySet()) {
			final String name = shot.getKey() + ".png";
			final File reference = new File(references, name);
			final File render = new File(output, name);
			shot.getValue().write(render);
			if (update || !reference.exists()) {
				shot.getValue().write(reference);
				SnapshotSuite.ENTRIES.add(SnapshotEntry.create(shot.getKey(), update ? SnapshotStatus.UPDATED : SnapshotStatus.RECORDED, 0, reference, render));
				continue;
			}

			final SnapshotDifference difference = shot.getValue().compare(SnapshotImage.read(reference), 0);
			if (difference.getPixels() == 0) {
				SnapshotSuite.ENTRIES.add(SnapshotEntry.create(shot.getKey(), SnapshotStatus.IDENTICAL, 0, reference, render));
				continue;
			}

			SnapshotSuite.ENTRIES.add(SnapshotEntry.create(shot.getKey(), SnapshotStatus.DIFFERENT, difference.getPixels(), reference, render));
			failures.add(shot.getKey() + ": " + difference.getPixels() + " pixels differ from the reference, maximum channel delta " + difference.getMaximum());
		}

		Assert.assertTrue(String.join(System.lineSeparator(), failures) + System.lineSeparator() + "Report: " + new File(output, "report.html").toPath().toUri().toASCIIString(), failures.isEmpty());
	}

	private static void pruneReferences() {
		final Set<String> shots = new HashSet<>();
		for (final String scenario : SnapshotRunner.getScenarios()) {
			shots.addAll(SnapshotRunner.getShots(scenario));
		}

		final File[] renderers = SnapshotSettings.getReferences().listFiles(File::isDirectory);
		if (renderers == null) {
			return;
		}

		for (final File renderer : renderers) {
			final File[] references = renderer.listFiles((directory, name) -> name.endsWith(".png") && !shots.contains(name.substring(0, name.length() - 4)));
			if (references == null) {
				continue;
			}

			for (final File reference : references) {
				if (reference.delete()) {
					System.out.println("Removed orphan reference " + reference);
				}
			}
		}
	}

}