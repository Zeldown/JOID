package dev.joid.lib.ui.node.impl.design.shape;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;

public class CircleNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void takesItsDiameterAsItsSize() {
		final CircleNode circle = CircleNode.create(100D, 200D, 50D);
		Assert.assertEquals(50D, circle.getWidth(), 0D);
		Assert.assertEquals(50D, circle.getHeight(), 0D);
	}

	@Test
	public void startsWhiteWithoutHoveredColor() {
		final CircleNode circle = CircleNode.create(100D, 200D, 50D);
		Assert.assertSame(Color.WHITE, circle.getColor());
		Assert.assertNull(circle.getHoveredColor());
	}

	@Test
	public void drawsACircleInscribedInItsSquare() {
		final RecordingShader shader = (RecordingShader) CircleShader.inst().getShader();
		this.bridges.open(new NodeUI(CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)))).frame();
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(200D, draw.getTop(), 1E-3D);
		Assert.assertEquals(150D, draw.getRight(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
		final Map<String, Object> values = shader.getValues();
		Assert.assertEquals(25F, (Float) values.get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {125F, 225F}, (float[]) values.get("center"), 0F);
	}

	@Test
	public void fitsItsSmallestSide() {
		final RecordingShader shader = (RecordingShader) CircleShader.inst().getShader();
		this.bridges.open(new NodeUI(CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).width(80D))).frame();
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(115D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(200D, draw.getTop(), 1E-3D);
		Assert.assertEquals(165D, draw.getRight(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
		final Map<String, Object> values = shader.getValues();
		Assert.assertEquals(25F, (Float) values.get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {140F, 225F}, (float[]) values.get("center"), 0F);
	}

	@Test
	public void drawsInsideItsParent() {
		final CircleNode parent = CircleNode.create(100D, 200D, 300D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		CircleNode.create(10D, 20D, 40D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(parent);
		this.bridges.open(new NodeUI(parent)).frame();
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(110D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(220D, draw.getTop(), 1E-3D);
		Assert.assertEquals(150D, draw.getRight(), 1E-3D);
		Assert.assertEquals(260D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void turnsIntoItsHoveredColorUnderTheMouse() {
		final Color color = new Color(0.2F, 0.4F, 0.6F, 1F);
		final Color hovered = new Color(0.6F, 0.4F, 0.2F, 1F);
		final CircleNode circle = CircleNode.create(100D, 200D, 50D).color(color).hoveredColor(hovered);
		Assert.assertSame(color, circle.getColor());
		Assert.assertSame(hovered, circle.getHoveredColor());
		this.bridges.open(new NodeUI(circle)).frame();
		this.single(0.2F, 0.4F, 0.6F);
		this.bridges.move(125D, 225D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
		this.bridges.move(10D, 10D).frames(20);
		this.single(0.2F, 0.4F, 0.6F);
	}

	@Test
	public void fadesTowardsItsHoveredColor() {
		this.bridges.open(new NodeUI(CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(new Color(0.6F, 0.4F, 0.2F, 1F)))).frame();
		this.bridges.move(125D, 225D).frames(6);
		final Draw draw = this.bridges.getRender().getDraws().get(0);
		Assert.assertTrue(draw.getRed() > 0.2F && draw.getRed() < 0.6F);
		Assert.assertEquals(0.4F, draw.getGreen(), 1E-4F);
	}

	@Test
	public void takesAHoveredColorOfItsOwn() {
		final Color hovered = new Color(0.6F, 0.4F, 0.2F, 1F);
		final CircleNode circle = CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(hovered);
		Assert.assertSame(hovered, circle.getHoveredColor());
		this.bridges.open(new NodeUI(circle)).move(125D, 225D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
	}

	@Test
	public void readsASuppliedHoveredColor() {
		final Color[] hovered = {new Color(0.6F, 0.4F, 0.2F, 1F)};
		final CircleNode circle = CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(() -> hovered[0]);
		this.bridges.open(new NodeUI(circle)).move(125D, 225D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
		hovered[0] = new Color(0.4F, 0.6F, 0.2F, 1F);
		this.bridges.frame();
		this.single(0.4F, 0.6F, 0.2F);
	}

	@Test
	public void readsBothSuppliedColors() {
		final Color color = new Color(0.2F, 0.4F, 0.6F, 1F);
		final Color hovered = new Color(0.6F, 0.4F, 0.2F, 1F);
		final CircleNode circle = CircleNode.create(100D, 200D, 50D).color(() -> color).hoveredColor(() -> hovered);
		Assert.assertSame(color, circle.getColor());
		Assert.assertSame(hovered, circle.getHoveredColor());
		this.bridges.open(new NodeUI(circle)).move(125D, 225D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
	}

	@Test
	public void dropsItsHoveredColorSetToNull() {
		final CircleNode circle = CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(new Color(0.6F, 0.4F, 0.2F, 1F)).hoveredColor((Color) null);
		Assert.assertNull(circle.getHoveredColor());
		this.bridges.open(new NodeUI(circle)).move(125D, 225D).frames(20);
		this.single(0.2F, 0.4F, 0.6F);
	}

	@Test
	public void keepsItsColorWithoutHoveredColor() {
		this.bridges.open(new NodeUI(CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)))).move(125D, 225D).frames(20);
		this.single(0.2F, 0.4F, 0.6F);
	}

	@Test
	public void drawsAGreyCircleWhileLoading() {
		this.bridges.open(new NodeUI(CircleNode.create(100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).wait(node -> false))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		final Draw draw = draws.get(0);
		Assert.assertSame(CircleShader.inst().getShader(), draw.getShader());
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
		Assert.assertEquals(draw.getRed(), draw.getGreen(), 0F);
		Assert.assertEquals(draw.getRed(), draw.getBlue(), 0F);
		Assert.assertTrue(draw.getRed() >= 0.15F && draw.getRed() <= 0.19F);
	}

	@Test
	public void returnsItselfFromEachSetter() {
		final CircleNode circle = CircleNode.create(100D, 200D, 50D);
		final Color color = new Color(0.2F, 0.4F, 0.6F, 1F);
		Assert.assertSame(circle, circle.color(color));
		Assert.assertSame(circle, circle.color(() -> color));
		Assert.assertSame(circle, circle.color(color).hoveredColor(color));
		Assert.assertSame(circle, circle.color(() -> color).hoveredColor(() -> color));
		Assert.assertSame(circle, circle.hoveredColor(color));
		Assert.assertSame(circle, circle.hoveredColor(() -> color));
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	@UIData(background = false)
	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}