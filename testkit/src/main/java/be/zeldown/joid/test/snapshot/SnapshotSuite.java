package be.zeldown.joid.test.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.test.CoreShaders;
import lombok.NonNull;

public abstract class SnapshotSuite {

	private static final List<SnapshotEntry> ENTRIES = new ArrayList<>();

	private static SnapshotRunner runner;

	protected abstract @NonNull ISnapshotBackend createBackend();

	@AfterClass
	public static void stopRunner() {
		if (SnapshotSuite.runner == null) {
			return;
		}

		SnapshotReport.write(new File(SnapshotSuite.getOutput(), "report.html"), "JOID snapshots - " + SnapshotSuite.runner.getRenderer(), SnapshotSuite.ENTRIES);
		SnapshotSuite.ENTRIES.clear();
		SnapshotSuite.runner.stop();
		SnapshotSuite.runner = null;
	}

	@Test
	public void compilesCoreShaders() {
		this.getRunner();
		for (final String name : CoreShaders.getNames()) {
			final IShader shader = BridgeHandler.RENDER.get().createShader(CoreShaders.read(name, ShaderStage.VERTEX), CoreShaders.read(name, ShaderStage.FRAGMENT), BlendState.NORMAL);
			Assert.assertTrue("The " + name + " shader does not compile", shader.isActive());
		}
	}

	@Test
	public void matchesStaticSnapshots() {
		this.verify("static");
	}

	@Test
	public void matchesInteractionSnapshots() {
		this.verify("interaction");
	}

	@Test
	public void matchesTransitionSnapshots() {
		this.verify("transition");
	}

	@Test
	public void matchesPopupSnapshots() {
		this.verify("popup");
	}

	@Test
	public void matchesWindowSnapshots() {
		this.verify("window");
	}

	@Test
	public void matchesDevSnapshots() {
		this.verify("dev");
	}

	private SnapshotRunner getRunner() {
		if (SnapshotSuite.runner == null) {
			SnapshotSuite.runner = SnapshotRunner.start(this.createBackend());
		}
		return SnapshotSuite.runner;
	}

	private void verify(final String scenario) {
		final File references = new File(System.getProperty("joid.snapshot.references"), this.getRunner().getRenderer());
		final File output = SnapshotSuite.getOutput();
		final boolean update = Boolean.getBoolean("joid.snapshot.update");

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

		Assert.assertTrue(String.join(System.lineSeparator(), failures) + System.lineSeparator() + "Report: " + new File(output, "report.html"), failures.isEmpty());
	}

	private static File getOutput() {
		return new File(System.getProperty("joid.snapshot.output"));
	}

}