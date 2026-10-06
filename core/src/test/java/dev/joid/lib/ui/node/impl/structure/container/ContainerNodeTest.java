package dev.joid.lib.ui.node.impl.structure.container;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;

public class ContainerNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void coversItsWholeParent() {
		final RectNode parent = RectNode.create(100D, 200D, 300D, 150D);
		final ContainerNode container = ContainerNode.create(parent);
		Assert.assertEquals(0D, container.getX(), 0D);
		Assert.assertEquals(0D, container.getY(), 0D);
		Assert.assertEquals(300D, container.getWidth(), 0D);
		Assert.assertEquals(150D, container.getHeight(), 0D);
		Assert.assertSame(parent, container.getParent());
		Assert.assertTrue(parent.getChildren().contains(container));
	}

	@Test
	public void keepsItsBounds() {
		final ContainerNode container = ContainerNode.create(10D, 20D, 30D, 40D);
		Assert.assertEquals(10D, container.getX(), 0D);
		Assert.assertEquals(20D, container.getY(), 0D);
		Assert.assertEquals(30D, container.getWidth(), 0D);
		Assert.assertEquals(40D, container.getHeight(), 0D);
		Assert.assertNull(container.getParent());
	}

	@Test
	public void drawsNothingItself() {
		this.bridges.open(new NodeUI(ContainerNode.create(10D, 20D, 300D, 150D))).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void drawsNoPlaceholderWhileLoading() {
		this.bridges.open(new NodeUI(ContainerNode.create(10D, 20D, 300D, 150D).wait(node -> false))).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void givesItsOriginToItsChildren() {
		final ContainerNode container = ContainerNode.create(100D, 200D, 300D, 150D);
		RectNode.create(5D, 10D, 50D, 20D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(container);
		this.bridges.open(new NodeUI(container)).frame();
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(105D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(210D, draw.getTop(), 1E-3D);
		Assert.assertEquals(155D, draw.getRight(), 1E-3D);
		Assert.assertEquals(230D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void clipsItsChildrenWithAHiddenOverflow() {
		final ContainerNode container = ContainerNode.create(100D, 200D, 300D, 150D).overflow(OverflowProperty.HIDDEN);
		RectNode.create(5D, 10D, 50D, 20D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(container);
		this.bridges.open(new NodeUI(container)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(100D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(200D, draws.get(0).getTop(), 1E-3D);
		Assert.assertEquals(400D, draws.get(0).getRight(), 1E-3D);
		Assert.assertEquals(350D, draws.get(0).getBottom(), 1E-3D);
		Assert.assertSame(draws.get(1), this.single(0.2F, 0.4F, 0.6F));
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