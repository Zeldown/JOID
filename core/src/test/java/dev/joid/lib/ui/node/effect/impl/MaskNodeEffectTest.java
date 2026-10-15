package dev.joid.lib.ui.node.effect.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.AllArgsConstructor;

public class MaskNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void followsTheSizeOfItsNode() {
		final MaskNodeEffect effect = MaskNodeEffect.create();
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(effect))).frame();
		Assert.assertEquals(200D, effect.getWidth(), 0D);
		Assert.assertEquals(50D, effect.getHeight(), 0D);
		final List<Draw> masks = this.bridges.getRender().getDraws(1F, 0F, 0F);
		Assert.assertEquals(1, masks.size());
		Assert.assertEquals(100D, masks.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(100D, masks.get(0).getTop(), 1E-3D);
		Assert.assertEquals(300D, masks.get(0).getRight(), 1E-3D);
		Assert.assertEquals(150D, masks.get(0).getBottom(), 1E-3D);
	}

	@Test
	public void startsAtTheCornerOfItsNode() {
		final MaskNodeEffect effect = MaskNodeEffect.create().width(40D).height(30D);
		Assert.assertEquals(0D, effect.getX(), 0D);
		Assert.assertEquals(0D, effect.getY(), 0D);
		Assert.assertEquals(40D, MaskNodeEffectTest.read(effect, "widthSupplier"), 0D);
		Assert.assertEquals(30D, MaskNodeEffectTest.read(effect, "heightSupplier"), 0D);
	}

	@Test
	public void keepsItsBounds() {
		final MaskNodeEffect effect = MaskNodeEffect.create().x(5D).y(6D).width(40D).height(30D);
		Assert.assertEquals(5D, effect.getX(), 0D);
		Assert.assertEquals(6D, effect.getY(), 0D);
		Assert.assertEquals(40D, MaskNodeEffectTest.read(effect, "widthSupplier"), 0D);
		Assert.assertEquals(30D, MaskNodeEffectTest.read(effect, "heightSupplier"), 0D);
	}

	@Test
	public void readsItsSuppliedBounds() {
		final double[] size = {10D};
		final MaskNodeEffect sized = MaskNodeEffect.create().width(() -> size[0]).height(() -> size[0] * 2D);
		final MaskNodeEffect placed = MaskNodeEffect.create().x(() -> size[0] / 2D).y(() -> size[0] / 5D).width(() -> size[0]).height(() -> size[0]);
		size[0] = 20D;
		Assert.assertEquals(0D, sized.getX(), 0D);
		Assert.assertEquals(40D, MaskNodeEffectTest.read(sized, "heightSupplier"), 0D);
		Assert.assertEquals(10D, placed.getX(), 0D);
		Assert.assertEquals(4D, placed.getY(), 0D);
		Assert.assertEquals(20D, MaskNodeEffectTest.read(placed, "widthSupplier"), 0D);
	}

	@Test
	public void keepsTheResourceOfItsShape() {
		final Resource resource = MaskNodeEffectTest.resource();
		final MaskNodeEffect fitted = MaskNodeEffect.create().resource(resource);
		final MaskNodeEffect sized = MaskNodeEffect.create().resource(resource).width(40D).height(30D);
		final MaskNodeEffect placed = MaskNodeEffect.create().resource(resource).x(5D).y(6D).width(40D).height(30D);
		Assert.assertSame(resource, fitted.getResource());
		Assert.assertNull(MaskNodeEffectTest.supplier(fitted, "widthSupplier"));
		Assert.assertSame(resource, sized.getResource());
		Assert.assertEquals(0D, sized.getX(), 0D);
		Assert.assertEquals(30D, MaskNodeEffectTest.read(sized, "heightSupplier"), 0D);
		Assert.assertSame(resource, placed.getResource());
		Assert.assertEquals(6D, placed.getY(), 0D);
	}

	@Test
	public void replacesEachBound() {
		final MaskNodeEffect effect = MaskNodeEffect.create().width(0D).height(0D);
		Assert.assertSame(effect, effect.x(1D).y(2D).width(3D).height(4D));
		Assert.assertArrayEquals(new double[] {1D, 2D, 3D, 4D}, MaskNodeEffectTest.bounds(effect), 0D);
		Assert.assertSame(effect, effect.x(5D).y(6D).width(7D).height(8D));
		Assert.assertArrayEquals(new double[] {5D, 6D, 7D, 8D}, MaskNodeEffectTest.bounds(effect), 0D);
		Assert.assertSame(effect, effect.x(9D).y(10D).width(11D).height(12D));
		Assert.assertArrayEquals(new double[] {9D, 10D, 11D, 12D}, MaskNodeEffectTest.bounds(effect), 0D);
	}

	@Test
	public void replacesEachBoundWithASupplier() {
		final double[] value = {1D};
		final MaskNodeEffect effect = MaskNodeEffect.create().width(0D).height(0D);
		Assert.assertSame(effect, effect.x(() -> value[0]).y(() -> value[0] + 1D).width(() -> value[0] + 2D).height(() -> value[0] + 3D));
		value[0] = 10D;
		Assert.assertArrayEquals(new double[] {10D, 11D, 12D, 13D}, MaskNodeEffectTest.bounds(effect), 0D);
		Assert.assertSame(effect, effect.x(() -> value[0] * 2D).y(() -> value[0] * 3D).width(() -> value[0] * 4D).height(() -> value[0] * 5D));
		Assert.assertArrayEquals(new double[] {20D, 30D, 40D, 50D}, MaskNodeEffectTest.bounds(effect), 0D);
		Assert.assertSame(effect, effect.x(() -> value[0]).y(() -> value[0]).width(() -> value[0]).height(() -> -value[0]));
		value[0] = 2D;
		Assert.assertArrayEquals(new double[] {2D, 2D, 2D, -2D}, MaskNodeEffectTest.bounds(effect), 0D);
	}

	@Test
	public void replacesItsResource() {
		final Resource resource = MaskNodeEffectTest.resource();
		final MaskNodeEffect effect = MaskNodeEffect.create().width(0D).height(0D);
		Assert.assertSame(effect, effect.resource(resource));
		Assert.assertSame(resource, effect.getResource());
		effect.resource((Resource) null);
		Assert.assertNull(effect.getResource());
	}

	@Test
	public void masksTheNodeToItsArea() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(MaskNodeEffect.create().x(10D).y(5D).width(50D).height(20D)))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final List<Draw> masks = this.bridges.getRender().getDraws(1F, 0F, 0F);
		Assert.assertEquals(1, masks.size());
		Assert.assertEquals(110D, masks.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(105D, masks.get(0).getTop(), 1E-3D);
		Assert.assertEquals(160D, masks.get(0).getRight(), 1E-3D);
		Assert.assertEquals(125D, masks.get(0).getBottom(), 1E-3D);
		Assert.assertTrue(draws.indexOf(masks.get(0)) < draws.indexOf(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0)));
		Assert.assertFalse(this.bridges.getRender().getState().getStencil().isEnabled());
	}

	@Test
	public void masksTheNodeToTheShapeOfAResource() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(MaskNodeEffect.create().resource(MaskNodeEffectTest.resource()).x(10D).y(5D).width(50D).height(20D)))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final int node = draws.indexOf(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0));
		final Draw mask = draws.get(node - 1);
		Assert.assertTrue(this.bridges.getRender().getDraws(1F, 0F, 0F).isEmpty());
		Assert.assertEquals(110D, mask.getLeft(), 1E-3D);
		Assert.assertEquals(105D, mask.getTop(), 1E-3D);
		Assert.assertEquals(160D, mask.getRight(), 1E-3D);
		Assert.assertEquals(125D, mask.getBottom(), 1E-3D);
		Assert.assertFalse(this.bridges.getRender().getState().getStencil().isEnabled());
	}

	private static double[] bounds(final MaskNodeEffect effect) {
		return new double[] {effect.getX(), effect.getY(), MaskNodeEffectTest.read(effect, "widthSupplier"), MaskNodeEffectTest.read(effect, "heightSupplier")};
	}

	private static double read(final MaskNodeEffect effect, final String name) {
		return (Double) MaskNodeEffectTest.supplier(effect, name).get();
	}

	private static Supplier<?> supplier(final MaskNodeEffect effect, final String name) {
		try {
			final Field field = MaskNodeEffect.class.getDeclaredField(name);
			field.setAccessible(true);
			return (Supplier<?>) field.get(effect);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static Resource resource() {
		return ResourceBuilder.create().cache(null).compute("mask", () -> new ResourceData("mask", null).textures(new ITexture[] {new RecordingTexture().allocate(4, 4)}));
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