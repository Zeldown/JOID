package dev.joid.lib.ui.node.effect.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.color.Color;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TransformNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void movesTheNode() {
		this.open(TransformNodeEffect.create(Transformation.create().translate(Vector.create(30D, 40D))));
		final Draw draw = this.single();
		Assert.assertEquals(130D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(140D, draw.getTop(), 1E-3D);
		Assert.assertEquals(180D, draw.getRight(), 1E-3D);
		Assert.assertEquals(190D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void scalesTheNodeAroundItsPivot() {
		this.open(TransformNodeEffect.create(Transformation.create().scale(Scale.create(2D, 3D, 1D), Vector.create(100D, 100D))));
		final Draw draw = this.single();
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(100D, draw.getTop(), 1E-3D);
		Assert.assertEquals(200D, draw.getRight(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void turnsTheNodeAroundItsPivot() {
		this.open(TransformNodeEffect.create(Transformation.create().rotate(180D, Rotation.YAW, Vector.create(200D, 0D))));
		final Draw draw = this.single();
		Assert.assertEquals(250D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(300D, draw.getRight(), 1E-3D);
		Assert.assertEquals(100D, draw.getTop(), 1E-3D);
	}

	@Test
	public void chainsEveryOperationOfATransformation() {
		final Transformation transformation = Transformation.create().translate(Vector.create(10D, 0D)).scale(Scale.create(2D, 2D, 1D), Vector.create(100D, 100D));
		final TransformNodeEffect effect = TransformNodeEffect.create(transformation);
		this.open(effect);
		final Draw draw = this.single();
		Assert.assertSame(transformation, effect.getTransformation());
		Assert.assertEquals(110D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(210D, draw.getRight(), 1E-3D);
		Assert.assertEquals(200D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void replacesItsTransformation() {
		final Transformation transformation = Transformation.create().translate(Vector.create(0D, 25D));
		final TransformNodeEffect effect = TransformNodeEffect.create(Transformation.create());
		Assert.assertSame(effect, effect.transformation(transformation));
		this.open(effect);
		Assert.assertEquals(125D, this.single().getTop(), 1E-3D);
	}

	@Test
	public void readsTheSuppliersOfItsOperationsOnEveryFrame() {
		final double[] x = {10D};
		this.open(TransformNodeEffect.create(Transformation.create().translate(Vector.create(() -> x[0], () -> 0D))));
		Assert.assertEquals(110D, this.single().getLeft(), 1E-3D);
		x[0] = 20D;
		this.bridges.frame();
		Assert.assertEquals(120D, this.single().getLeft(), 1E-3D);
	}

	@Test
	public void scalesAroundTheCenterOfItsNodeByDefault() {
		this.open(TransformNodeEffect.create(Transformation.create().scale(Scale.create(2D, 2D, 1D))));
		final Draw draw = this.single();
		Assert.assertEquals(75D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(75D, draw.getTop(), 1E-3D);
		Assert.assertEquals(175D, draw.getRight(), 1E-3D);
		Assert.assertEquals(175D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void movesTheShadowOfItsNodeWhicheverEffectComesFirst() {
		final ShadowNodeEffect shadow = ShadowNodeEffect.create(new Color(0.1F, 0.3F, 0.5F, 1F), 8F);
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(shadow).effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(30D, 40D)))))).frame();
		final Draw draw = this.single();
		final Draw shadowDraw = this.bridges.getRender().getDraws(0.1F, 0.3F, 0.5F).get(0);
		Assert.assertEquals(130D, draw.getLeft(), 1E-3D);
		Assert.assertEquals((draw.getLeft() + draw.getRight()) / 2D, (shadowDraw.getLeft() + shadowDraw.getRight()) / 2D, 1E-3D);
		Assert.assertEquals((draw.getTop() + draw.getBottom()) / 2D, (shadowDraw.getTop() + shadowDraw.getBottom()) / 2D, 1E-3D);
	}

	@Test
	public void leavesTheNextNodesInPlace() {
		final RectNode moved = RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(30D, 40D))));
		final RectNode still = RectNode.create(300D, 100D, 50D, 50D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		this.bridges.open(new NodeUI(moved, still)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(300D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(100D, draws.get(0).getTop(), 1E-3D);
	}

	@Test
	public void leavesTheMatrixStackAsItWasWhenAnOperationFails() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(() -> {
			throw new IllegalStateException("broken");
		}, () -> 0D))));
		final MatrixStack modelView = this.bridges.getRender().getModelView();
		final int depth = modelView.getDepth();
		try {
			node.render(0D, 0D);
			Assert.fail();
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("broken", expected.getMessage());
		}
		Assert.assertEquals(depth, modelView.getDepth());
	}

	private void open(final TransformNodeEffect effect) {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(effect))).frame();
	}

	private Draw single() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		public NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}