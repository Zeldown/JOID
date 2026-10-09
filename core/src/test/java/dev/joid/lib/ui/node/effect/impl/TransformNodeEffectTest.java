package dev.joid.lib.ui.node.effect.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.render.transform.operation.RotateTransformOperation;
import dev.joid.lib.render.transform.operation.ScaleTransformOperation;
import dev.joid.lib.render.transform.operation.TranslateTransformOperation;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class TransformNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void movesTheNode() {
		this.open(TransformNodeEffect.create(new TranslateTransformOperation(Vector.create(30D, 40D))));
		final Draw draw = this.single();
		Assert.assertEquals(130D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(140D, draw.getTop(), 1E-3D);
		Assert.assertEquals(180D, draw.getRight(), 1E-3D);
		Assert.assertEquals(190D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void scalesTheNodeAroundItsPivot() {
		this.open(TransformNodeEffect.create(new ScaleTransformOperation(Scale.create(2D, 3D, 1D), Vector.create(100D, 100D))));
		final Draw draw = this.single();
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(100D, draw.getTop(), 1E-3D);
		Assert.assertEquals(200D, draw.getRight(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void turnsTheNodeAroundItsPivot() {
		this.open(TransformNodeEffect.create(new RotateTransformOperation(180D, Rotation.YAW, Vector.create(200D, 0D))));
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
		Assert.assertSame(transformation, effect.getTransformationSupplier().get());
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
	public void readsItsSuppliedTransformationOnEveryFrame() {
		final Transformation[] current = {Transformation.create().translate(Vector.create(10D, 0D))};
		final TransformNodeEffect effect = TransformNodeEffect.create(Transformation.create());
		Assert.assertSame(effect, effect.transformation(() -> current[0]));
		this.open(effect);
		Assert.assertEquals(110D, this.single().getLeft(), 1E-3D);
		current[0] = Transformation.create().translate(Vector.create(20D, 0D));
		this.bridges.frame();
		Assert.assertEquals(120D, this.single().getLeft(), 1E-3D);
	}

	@Test
	public void leavesTheNextNodesInPlace() {
		final RectNode moved = RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(TransformNodeEffect.create(new TranslateTransformOperation(Vector.create(30D, 40D))));
		final RectNode still = RectNode.create(300D, 100D, 50D, 50D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		this.bridges.open(new NodeUI(moved, still)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(300D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(100D, draws.get(0).getTop(), 1E-3D);
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