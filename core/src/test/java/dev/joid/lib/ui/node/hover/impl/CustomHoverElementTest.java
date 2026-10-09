package dev.joid.lib.ui.node.hover.impl;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.IHoverElement;
import dev.joid.lib.ui.node.hover.impl.CustomHoverElement.HoverElementPosition;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class CustomHoverElementTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void keepsItsElementAndPosition() {
		final IHoverElement element = new BoxElement(0D, 0D, 40D, 20D);
		Assert.assertSame(element, CustomHoverElement.follow(element).getElement());
		Assert.assertSame(HoverElementPosition.FOLLOW, CustomHoverElement.follow(element).getPosition());
		Assert.assertSame(HoverElementPosition.FIXED, CustomHoverElement.fixed(element).getPosition());
		Assert.assertSame(HoverElementPosition.RELATIVE, CustomHoverElement.relative(element).getPosition());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingElement() {
		CustomHoverElement.follow(null);
	}

	@Test
	public void followsTheMouse() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(CustomHoverElement.follow(new BoxElement(10D, -5D, 40D, 20D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		Assert.assertEquals(160D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(135D, this.tooltip().getTop(), 1E-3D);
		this.bridges.move(250D, 180D).frames(1);
		Assert.assertEquals(260D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(155D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void sitsOnTheNodeWhenRelative() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(CustomHoverElement.relative(new BoxElement(10D, -5D, 40D, 20D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(250D, 180D).frames(2);
		Assert.assertEquals(110D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(75D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void sitsAtTheUiOriginWhenFixed() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(CustomHoverElement.fixed(new BoxElement(30D, 40D, 40D, 20D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(250D, 180D).frames(2);
		Assert.assertEquals(30D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(20D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void drawsNothingOutsideTheNode() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(CustomHoverElement.follow(new BoxElement(10D, -5D, 40D, 20D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(500D, 500D).frames(2);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).isEmpty());
	}

	@Test
	public void staysLeftOfTheRightEdge() {
		final RectNode rect = RectNode.create(1700D, 400D, 220D, 200D).hover(CustomHoverElement.follow(new BoxElement(10D, -5D, 40D, 20D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1900D, 500D).frames(2);
		Assert.assertEquals(1880D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(475D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void staysBelowTheTopEdge() {
		final RectNode rect = RectNode.create(100D, 0D, 200D, 100D).hover(CustomHoverElement.follow(new BoxElement(10D, -5D, 40D, 20D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 10D).frames(2);
		Assert.assertEquals(160D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(0D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void leavesAnElementWithoutSizeWhereItIs() {
		final RectNode rect = RectNode.create(1700D, 0D, 220D, 200D).hover(CustomHoverElement.follow(new BoxElement(10D, -5D, 0D, 0D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1900D, 2D).frames(2);
		Assert.assertEquals(1910D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(-3D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void leavesAnElementWithoutHeightWhereItIs() {
		final RectNode rect = RectNode.create(1700D, 0D, 220D, 200D).hover(CustomHoverElement.follow(new BoxElement(10D, -5D, 40D, 0D)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1900D, 2D).frames(2);
		Assert.assertEquals(1910D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(-3D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void leavesTheElementWhereItIsOutsideAUi() {
		CustomHoverElement.follow(new BoxElement(10D, -5D, 40D, 20D)).render(RectNode.create(0D, 0D, 10D, 10D), 1900D, 10D);
		Assert.assertEquals(1910D, this.tooltip().getLeft(), 1E-3D);
		Assert.assertEquals(-15D, this.tooltip().getTop(), 1E-3D);
	}

	@Test
	public void drawsNothingWithoutElementOrPosition() {
		new CustomHoverElement(null, HoverElementPosition.FOLLOW).render(RectNode.create(0D, 0D, 10D, 10D), 5D, 5D);
		new CustomHoverElement(new BoxElement(0D, 0D, 40D, 20D), null).render(RectNode.create(0D, 0D, 10D, 10D), 5D, 5D);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	private Draw tooltip() {
		return this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).get(0);
	}

	@Getter
	@AllArgsConstructor
	private static final class BoxElement implements IHoverElement {

		private final double x;
		private final double y;
		private final double width;
		private final double height;

		@Override
		public void render(final @NonNull Node node, final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(0D, 0D, 40D, 20D, new Color(0.6F, 0.4F, 0.2F, 1F));
		}

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