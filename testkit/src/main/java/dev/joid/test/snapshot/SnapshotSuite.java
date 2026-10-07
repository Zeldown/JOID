package dev.joid.test.snapshot;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
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
	public void matchesStaticSnapshots() {
		this.verify("static");
	}

	@Test
	public void matchesWindowSnapshots() {
		this.verify("window");
	}

	@Test
	public void matchesResourceSnapshots() {
		this.verify("resource");
	}

	@Test
	public void matchesTransitionSnapshots() {
		this.verify("transition");
	}

	@Test
	public void matchesInteractionSnapshots() {
		this.verify("interaction");
	}

	@Test
	public void resetsTheInterfaceScaleBetweenScenarios() {
		this.getRunner().execute("ui " + TraceUI.class.getName(), "scale 2");
		this.getRunner().execute("ui " + TraceUI.class.getName());
		Assert.assertEquals(1D, JOID.getUI(TraceUI.class).getView().getInterfaceScale(), 0D);
	}

	@Test
	public void rendersAFrameAfterAMove() {
		this.getRunner().execute("ui " + TraceUI.class.getName(), "move 100 200");
		final TraceUI ui = JOID.getUI(TraceUI.class);
		Assert.assertEquals(ui.getView().toUiX(100D), ui.getMouseX(), 0D);
		Assert.assertEquals(ui.getView().toUiY(200D), ui.getMouseY(), 0D);
	}

	@Test
	public void dragsWithThePositionOfEachStep() {
		this.getRunner().execute("ui " + TraceUI.class.getName(), "move 100 100", "press LEFT", "moveto 300 140 160", "release LEFT");
		final TraceUI ui = JOID.getUI(TraceUI.class);
		Assert.assertEquals(ui.getView().toUiX(300D), ui.getDragX(), 0D);
		Assert.assertEquals(ui.getView().toUiY(140D), ui.getDragY(), 0D);
	}

	@Test
	public void advancesTheClockByTheExactDuration() {
		this.getRunner().execute();
		final long start = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.getRunner().execute("wait 40");
		Assert.assertEquals(start + 40L, BridgeHandler.CLOCK.get().currentTimeMillis());
		this.getRunner().execute("moveto 10 10 40");
		Assert.assertEquals(start + 40L, BridgeHandler.CLOCK.get().currentTimeMillis());
	}

	@Test
	public void waitsForTheDownloadsBeforeAShot() {
		final Resource resource = Resource.of(new DownloadAsset());
		this.getRunner().execute("shot download");
		Assert.assertNotNull(resource.getResourceData().getDecoder());
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

	public static final class TraceUI extends UI {

		@Getter private double dragX;
		@Getter private double dragY;

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
			this.dragX = mouseX;
			this.dragY = mouseY;
		}

	}

	private static final class DownloadAsset extends Asset {

		private DownloadAsset() {
			super("snapshot-download");
		}

		@Override
		public boolean isRemote() {
			return true;
		}

		@Override
		public @NonNull InputStream open() {
			try {
				Thread.sleep(300L);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png");
		}

	}

}