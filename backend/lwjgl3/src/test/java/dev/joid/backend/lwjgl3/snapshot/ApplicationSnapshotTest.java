package dev.joid.backend.lwjgl3.snapshot;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.test.snapshot.SnapshotImage;
import dev.joid.test.snapshot.SnapshotRunner;

public class ApplicationSnapshotTest {

	@Test
	public void rendersTheUIOfAnApplication() {
		final SnapshotRunner runner = SnapshotRunner.start(new Lwjgl3SnapshotBackend());
		try {
			final SnapshotImage shot = runner.execute("ui " + PlaceholderUI.class.getName(), "shot placeholder").get("placeholder");
			Assert.assertEquals(0xFF323232, shot.getPixels()[0]);
			Assert.assertEquals(0xFFDDDDDD, shot.getPixels()[540 * 1920 + 960]);
		} finally {
			runner.stop();
		}
	}

	public static final class PlaceholderUI extends UI {

		@Override
		public void init() {
			RectNode.create(660, 290, 600, 500).color(Color.decode("#DDDDDD")).attach(this);
		}

	}

}