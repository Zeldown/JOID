package com.example.running;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.test.snapshot.SnapshotDifference;
import dev.joid.test.snapshot.SnapshotImage;
import dev.joid.test.snapshot.SnapshotRunner;
import dev.joid.test.snapshot.SnapshotSettings;

public class MenuSnapshotTest {

	private static SnapshotRunner runner;

	// #region start
	@BeforeClass
	public static void start() {
		MenuSnapshotTest.runner = SnapshotRunner.start(new Lwjgl3SnapshotBackend());
	}

	@AfterClass
	public static void stop() {
		MenuSnapshotTest.runner.stop();
	}
	// #endregion

	// #region shot
	@Test
	public void showsThePlayButton() {
		final SnapshotImage shot = MenuSnapshotTest.runner
		.execute("ui " + RunningMenuUI.class.getName(), "shot menu")
		.get("menu");
		shot.write(new File("build/snapshots/menu.png"));
		Assert.assertEquals(0xFF262B35, shot.getPixels()[520 * shot.getWidth() + 870]);
	}
	// #endregion

	// #region click
	@Test
	public void startsTheGameOnClick() {
		final SnapshotImage shot = MenuSnapshotTest.runner.execute(
			"ui " + RunningMenuUI.class.getName(),
			"moveto 960 540 300",
			"press LEFT",
			"release LEFT",
			"wait 100",
			"shot clicked"
		).get("clicked");
		final File references = new File("src/test/resources/snapshots", MenuSnapshotTest.runner.getRenderer());
		final File reference = new File(references, "clicked.png");
		if (!reference.exists() && SnapshotSettings.isRecord()) {
			shot.write(reference);
		}
		final SnapshotDifference difference = shot.compare(SnapshotImage.read(reference), 2);
		Assert.assertEquals(0, difference.getPixels());
	}
	// #endregion

}