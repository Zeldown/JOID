package dev.joid.lib.ui.node.impl.progress;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ProgressNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsEmptyFromTheLeftWithoutParts() {
		final ProgressNode progress = ProgressNode.create(100D, 100D, 200D, 50D);
		Assert.assertEquals(0F, progress.getProgress(), 0F);
		Assert.assertSame(ProgressDirection.LEFT_TO_RIGHT, progress.getDirection());
		Assert.assertNull(progress.getTrack());
		Assert.assertNull(progress.getFill());
	}

	@Test
	public void fillsFromTheLeft() {
		this.open(ProgressNodeTest.bar().progress(0.25F));
		this.assertFill(100D, 100D, 150D, 150D);
	}

	@Test
	public void fillsNoMoreThanItsBox() {
		final ProgressNode progress = ProgressNodeTest.bar().progress(1.5F);
		this.open(progress);
		this.assertFill(100D, 100D, 300D, 150D);
		Assert.assertEquals(1.5F, progress.getProgress(), 0F);
	}

	@Test
	public void fillsFromTheRight() {
		this.open(ProgressNodeTest.bar().progress(0.25F).direction(ProgressDirection.RIGHT_TO_LEFT));
		this.assertFill(250D, 100D, 300D, 150D);
	}

	@Test
	public void fillsFromTheTop() {
		this.open(ProgressNodeTest.bar().progress(0.4F).direction(ProgressDirection.TOP_TO_BOTTOM));
		this.assertFill(100D, 100D, 300D, 120D);
	}

	@Test
	public void fillsFromTheBottom() {
		this.open(ProgressNodeTest.bar().progress(0.4F).direction(ProgressDirection.BOTTOM_TO_TOP));
		this.assertFill(100D, 130D, 300D, 150D);
	}

	@Test
	public void drawsItsTrackUnderTheFill() {
		this.open(ProgressNodeTest.bar().progress(0.5F));
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertTrue(draws.indexOf(this.single(0F, 0F, 0F)) < draws.indexOf(this.single(1F, 1F, 1F)));
	}

	@Test
	public void fillsWithoutTrack() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).fill(RectNode.create(0D, 0D, 0D, 0D).color(Color.WHITE)).progress(0.5F));
		Assert.assertEquals(200D, this.single(1F, 1F, 1F).getRight(), 1E-3D);
		Assert.assertTrue(this.bridges.getRender().getDraws(0F, 0F, 0F).isEmpty());
	}

	@Test
	public void replacesEachPart() {
		final RectNode track = RectNode.create(0D, 0D, 0D, 0D);
		final RectNode fill = RectNode.create(0D, 0D, 0D, 0D);
		final ProgressNode progress = ProgressNodeTest.bar();
		final Node previous = progress.getFill();
		Assert.assertSame(progress, progress.track(track).fill(fill));
		Assert.assertSame(track, progress.getTrack());
		Assert.assertSame(fill, progress.getFill());
		Assert.assertEquals(2, progress.getChildren().size());
		Assert.assertFalse(progress.getChildren().contains(previous));
	}

	@Test
	public void refusesToShowWithoutFill() {
		try {
			this.open(ProgressNode.create(100D, 100D, 200D, 50D));
			Assert.fail("A progress bar needs a fill");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The ProgressNode has no fill, give it one with fill(...)", expected.getMessage());
		}
	}

	private void open(final ProgressNode progress) {
		this.bridges.open(new NodeUI(progress)).frame();
	}

	private void assertFill(final double left, final double top, final double right, final double bottom) {
		final Draw fill = this.single(1F, 1F, 1F);
		Assert.assertEquals(left, fill.getLeft(), 1E-3D);
		Assert.assertEquals(top, fill.getTop(), 1E-3D);
		Assert.assertEquals(right, fill.getRight(), 1E-3D);
		Assert.assertEquals(bottom, fill.getBottom(), 1E-3D);
		Assert.assertEquals(200D, this.single(0F, 0F, 0F).getRight() - this.single(0F, 0F, 0F).getLeft(), 1E-3D);
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private static ProgressNode bar() {
		return ProgressNode.create(100D, 100D, 200D, 50D).track(RectNode.create(0D, 0D, 0D, 0D).color(Color.BLACK)).fill(RectNode.create(0D, 0D, 0D, 0D).color(Color.WHITE));
	}

}