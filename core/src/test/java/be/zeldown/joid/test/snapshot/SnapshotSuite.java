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

	private static final int TOLERANCE = 8;

	private static SnapshotRunner runner;

	protected abstract @NonNull ISnapshotBackend createBackend();

	@AfterClass
	public static void stopRunner() {
		if (SnapshotSuite.runner != null) {
			SnapshotSuite.runner.stop();
			SnapshotSuite.runner = null;
		}
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

	private SnapshotRunner getRunner() {
		if (SnapshotSuite.runner == null) {
			SnapshotSuite.runner = SnapshotRunner.start(this.createBackend());
		}
		return SnapshotSuite.runner;
	}

	private void verify(final String scenario) {
		final File references = new File(System.getProperty("joid.snapshot.references"));
		final File output = new File(System.getProperty("joid.snapshot.output"));
		final boolean update = Boolean.getBoolean("joid.snapshot.update");

		final List<String> failures = new ArrayList<>();
		for (final Map.Entry<String, SnapshotImage> shot : this.getRunner().run(scenario).entrySet()) {
			final File reference = new File(references, shot.getKey() + ".png");
			shot.getValue().write(new File(output, shot.getKey() + ".png"));
			if (update) {
				shot.getValue().write(reference);
				continue;
			}

			if (!reference.exists()) {
				failures.add(shot.getKey() + ": no reference snapshot, run ./gradlew test -PupdateSnapshots");
				continue;
			}

			final SnapshotDifference difference = shot.getValue().compare(SnapshotImage.read(reference), SnapshotSuite.TOLERANCE);
			if (difference.getPixels() > 0) {
				difference.getImage().write(new File(output, "diff/" + shot.getKey() + ".png"));
				failures.add(shot.getKey() + ": " + difference.getPixels() + " pixels differ from the reference, maximum channel delta " + difference.getMaximum());
			}
		}

		Assert.assertTrue(String.join(System.lineSeparator(), failures), failures.isEmpty());
	}

}