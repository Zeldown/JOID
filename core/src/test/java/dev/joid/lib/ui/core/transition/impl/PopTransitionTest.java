package dev.joid.lib.ui.core.transition.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.transition.Transition.TransitionState;
import dev.joid.lib.ui.core.transition.impl.PopTransition.PopInTransition;
import dev.joid.lib.ui.core.transition.impl.PopTransition.PopOutTransition;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class PopTransitionTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void popsInThenOut() {
		final PopTransition transition = new PopTransition();
		Assert.assertTrue(transition.getIn() instanceof PopInTransition);
		Assert.assertTrue(transition.getOut() instanceof PopOutTransition);
	}

	@Test
	public void growsFromThreeQuartersOfTheUi() {
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D));
		final PopInTransition in = new PopInTransition();
		in.init(ui);
		Assert.assertEquals(0F, in.getAnimator().getValue(), 0F);
		this.assertScale(ui, in, 0.75D);
	}

	@Test
	public void reachesItsFullSizeIn130Milliseconds() {
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D));
		final PopInTransition in = new PopInTransition();
		in.start();
		Assert.assertTrue(in.isRunning());
		this.bridges.getClock().advance(65L);
		in.update();
		Assert.assertTrue(in.getAnimator().getValue() > 0.5F && in.getAnimator().getValue() < 1F);
		this.bridges.getClock().advance(100L);
		in.update();
		Assert.assertEquals(1F, in.getAnimator().getValue(), 0F);
		this.assertScale(ui, in, 1D);
	}

	@Test
	public void shrinksBackToThreeQuartersOfTheUi() {
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D));
		final PopOutTransition out = new PopOutTransition();
		out.init(ui);
		this.assertScale(ui, out, 1D);
		out.start();
		this.bridges.getClock().advance(65L);
		out.update();
		Assert.assertTrue(out.getAnimator().getValue() > 0F && out.getAnimator().getValue() < 1F);
		this.bridges.getClock().advance(100L);
		out.update();
		Assert.assertEquals(0F, out.getAnimator().getValue(), 0F);
		this.assertScale(ui, out, 0.75D);
	}

	@Test
	public void popsAUiInWhenItOpens() {
		final NodeUI ui = new NodeUI(RectNode.create(860D, 490D, 200D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)));
		ui.setTransition(new PopTransition());
		this.bridges.open(ui);
		Assert.assertTrue(this.width() > 150D && this.width() < 200D);
		this.bridges.frames(20);
		Assert.assertEquals(200D, this.width(), 1E-3D);
	}

	@Test
	public void popsAUiOutBeforeItCloses() {
		final NodeUI ui = new NodeUI(RectNode.create(860D, 490D, 200D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)));
		ui.setTransition(new PopTransition());
		this.bridges.open(ui).frames(20);
		Assert.assertFalse(ui.fireClose());
		this.bridges.frames(2);
		Assert.assertTrue(this.width() < 200D);
		Assert.assertTrue(this.bridges.getUi().getUiList().contains(ui));
		this.bridges.frames(20);
		Assert.assertFalse(this.bridges.getUi().getUiList().contains(ui));
	}

	private void assertScale(final UI ui, final TransitionState state, final double scale) {
		final float[] before = this.bridges.getRender().getModelView().getMatrix().clone();
		final double anchorX = ui.getData().getAnchorPositionX();
		state.pre(ui, 0D, 0D);
		try {
			final PixelGrid grid = this.bridges.getRender().getPixelGrid();
			Assert.assertEquals(anchorX, grid.toScreenX(anchorX), 1E-3D);
			Assert.assertEquals(anchorX + 100D * scale, grid.toScreenX(anchorX + 100D), 1E-3D);
		} finally {
			state.post(ui, 0D, 0D);
		}

		Assert.assertArrayEquals(before, this.bridges.getRender().getModelView().getMatrix(), 0F);
	}

	private double width() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0).getRight() - draws.get(0).getLeft();
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

}