package dev.joid.lib.ui.node.hover.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.IHoverElement;
import dev.joid.lib.ui.node.hover.impl.CustomHoverElement.HoverElementPosition;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeHoverElementTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void takesItsBoundsFromTheNode() {
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D);
		final IHoverElement element = NodeHoverElement.fixed(tooltip).getElement();
		Assert.assertEquals(10D, element.getX(), 0D);
		Assert.assertEquals(-5D, element.getY(), 0D);
		Assert.assertEquals(40D, element.getWidth(), 0D);
		Assert.assertEquals(20D, element.getHeight(), 0D);
		tooltip.x(30D).y(15D);
		Assert.assertEquals(30D, element.getX(), 0D);
		Assert.assertEquals(15D, element.getY(), 0D);
	}

	@Test
	public void keepsItsPosition() {
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D);
		Assert.assertSame(HoverElementPosition.FOLLOW, NodeHoverElement.follow(tooltip).getPosition());
		Assert.assertSame(HoverElementPosition.FIXED, NodeHoverElement.fixed(tooltip).getPosition());
		Assert.assertSame(HoverElementPosition.RELATIVE, NodeHoverElement.relative(tooltip).getPosition());
	}

	@Test
	public void drawsTheNodeNextToTheMouse() {
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.follow(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		Assert.assertEquals(160D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(135D, this.tooltip().getTop(), 1E-3D);
		Assert.assertEquals(200D, this.tooltip().getRight(), 1E-3D);
		Assert.assertEquals(155D, this.tooltip().getBottom(), 1E-3D);
	}

	@Test
	public void drawsTheNodeOnTheHoveredNodeWhenRelative() {
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.relative(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(250D, 180D).frames(2);
		Assert.assertEquals(110D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(75D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void drawsTheNodeFromTheUiOriginWhenFixed() {
		final RectNode tooltip = RectNode.create(30D, 40D, 40D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.fixed(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(250D, 180D).frames(2);
		Assert.assertEquals(30D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(20D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void loadsTheNodeIntoTheHoveredUiOnce() {
		final List<Object> received = new ArrayList<>();
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).onInit(received::add);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.follow(tooltip));
		final NodeUI ui = new NodeUI(rect);
		this.bridges.open(ui).frames(30);
		Assert.assertFalse(tooltip.hasUi());
		this.bridges.move(150D, 160D).frames(5);
		Assert.assertSame(ui, tooltip.getUi());
		Assert.assertEquals(Collections.singletonList(tooltip), received);
	}

	private Draw tooltip() {
		return this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).get(0);
	}

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