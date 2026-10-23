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
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.hover.IHoverElement;
import dev.joid.lib.ui.node.hover.impl.PositionedHoverElement.HoverElementPosition;
import dev.joid.lib.ui.node.impl.shape.RectNode;

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
	public void takesANodeBuiltInTheCall() {
		Assert.assertEquals(40D, NodeHoverElement.follow(RectNode.create(10D, -5D, 40D, 20D).color(Color.WHITE)).getElement().getWidth(), 0D);
		Assert.assertEquals(20D, NodeHoverElement.fixed(RectNode.create(10D, -5D, 40D, 20D).color(Color.WHITE)).getElement().getHeight(), 0D);
		Assert.assertSame(HoverElementPosition.RELATIVE, NodeHoverElement.relative(RectNode.create(10D, -5D, 40D, 20D).color(Color.WHITE)).getPosition());
	}

	@Test
	public void drawsTheNodeNextToTheMouse() {
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.follow(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		Assert.assertEquals(160D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(155D, this.tooltip().getTop(), 1E-3D);
		Assert.assertEquals(200D, this.tooltip().getRight(), 1E-3D);
		Assert.assertEquals(175D, this.tooltip().getBottom(), 1E-3D);
	}

	@Test
	public void drawsTheNodeOnTheHoveredNodeWhenRelative() {
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.relative(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(250D, 180D).frames(2);
		Assert.assertEquals(110D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(95D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void drawsTheNodeAtItsOwnCoordinatesWhenFixed() {
		final RectNode tooltip = RectNode.create(30D, 40D, 40D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.fixed(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(250D, 180D).frames(2);
		Assert.assertEquals(30D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(40D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void loadsTheNodeIntoTheHoveredUiOnce() {
		final List<Object> received = new ArrayList<>();
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).onInit(event -> received.add(event.getNode()));
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.follow(tooltip));
		final NodeUI ui = new NodeUI(rect);
		this.bridges.open(ui).frames(30);
		Assert.assertFalse(tooltip.isAttached());
		this.bridges.move(150D, 160D).frames(5);
		Assert.assertSame(ui, tooltip.getUi());
		Assert.assertEquals(Collections.singletonList(tooltip), received);
	}

	@Test
	public void updatesTheNodeOnEveryFrameItIsShown() {
		final int[] updates = {0};
		final RectNode tooltip = RectNode.create(10D, -5D, 40D, 20D).onUpdate(event -> updates[0]++);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(NodeHoverElement.follow(tooltip));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(5);
		Assert.assertTrue(updates[0] >= 4);
	}

	@Test
	public void detachesTheNodeOnceItsElementIsRemoved() {
		final RectNode first = RectNode.create(10D, -5D, 40D, 20D);
		final RectNode second = RectNode.create(10D, 20D, 40D, 20D);
		final NodeHoverElement element = NodeHoverElement.follow(first);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(element).hover(NodeHoverElement.follow(second));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(5);
		Assert.assertTrue(first.isAttached());
		rect.removeHover(element);
		Assert.assertFalse(first.isAttached());
		Assert.assertTrue(second.isAttached());
		rect.clearHover();
		Assert.assertFalse(second.isAttached());
	}

	private Draw tooltip() {
		return this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).get(0);
	}

}