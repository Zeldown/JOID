package dev.joid.lib.ui.node.impl.structure.scrollbar;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.click.ClickType;
import lombok.AllArgsConstructor;

public class ScrollbarNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void measuresTheRoomLeftOnItsTrack() {
		final Bar bar = new Bar(0D, 0D, 40D, 10D, BoundingBox.create(0D, 0D, 400D, 30D));
		Assert.assertEquals(360D, bar.getScrollWidth(), 0D);
		Assert.assertEquals(20D, bar.getScrollHeight(), 0D);
	}

	@Test
	public void followsTheScrolledContent() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.bridges.open(new NodeUI(content)).frame();
		Assert.assertSame(content, bar.getScrollNode());
		content.scrollOffsetY(-100D).updateScroll();
		this.bridges.frame();
		final Draw thumb = this.thumb();
		Assert.assertEquals(140D, thumb.getTop(), 1E-3D);
		Assert.assertEquals(160D, thumb.getBottom(), 1E-3D);
		Assert.assertEquals(510D, thumb.getLeft(), 1E-3D);
	}

	@Test
	public void scrollsTheContentVerticallyWhenDragged() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		Assert.assertTrue(bar.isDragging());
		this.bridges.move(515D, 150D).frame();
		Assert.assertEquals(40D, bar.getY(), 1E-9D);
		Assert.assertEquals(-100D, content.getTargetScrollY(), 1E-6D);
		this.bridges.move(515D, 900D).frame();
		Assert.assertEquals(80D, bar.getY(), 1E-9D);
		Assert.assertEquals(-200D, content.getTargetScrollY(), 1E-6D);
	}

	@Test
	public void scrollsTheContentHorizontallyWhenDragged() {
		final Bar bar = new Bar(0D, 110D, 40D, 10D, BoundingBox.create(0D, 110D, 400D, 10D));
		final RectNode content = RectNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(bar);
		RectNode.create(0D, 0D, 400D, 100D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).attach(content);
		RectNode.create(400D, 0D, 400D, 100D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).attach(content);
		this.press(content, 120D, 215D);
		this.bridges.move(300D, 215D).frame();
		Assert.assertEquals(180D, bar.getX(), 1E-9D);
		Assert.assertEquals(-200D, content.getTargetScrollX(), 1E-6D);
		this.bridges.move(0D, 215D).frame();
		Assert.assertEquals(0D, bar.getX(), 1E-9D);
		Assert.assertEquals(0D, content.getTargetScrollX(), 1E-6D);
	}

	@Test
	public void letsGoOnRelease() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.move(515D, 150D).frame();
		Assert.assertFalse(bar.isDragging());
		Assert.assertEquals(0D, content.getTargetScrollY(), 0D);
	}

	@Test
	public void keepsDraggingUntilTheButtonThatStartedIt() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		this.bridges.getUi().mouseReleased(ClickType.RIGHT);
		Assert.assertTrue(bar.isDragging());
		Assert.assertSame(ClickType.LEFT, bar.getDragButton());
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertFalse(bar.isDragging());
		Assert.assertNull(bar.getDragButton());
	}

	@Test
	public void drivesTheAxisOfItsTrack() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final RectNode content = ScrollbarNodeTest.column(bar);
		RectNode.create(0D, 0D, 600D, 10D).attach(content);
		Assert.assertFalse(bar.isHorizontal());
		this.press(content, 515D, 110D);
		Assert.assertTrue(content.hasOverflowX());
		this.bridges.move(515D, 150D).frame();
		Assert.assertEquals(40D, bar.getY(), 1E-9D);
		Assert.assertEquals(410D, bar.getX(), 0D);
		Assert.assertEquals(-100D, content.getTargetScrollY(), 1E-6D);
		Assert.assertEquals(0D, content.getTargetScrollX(), 0D);
	}

	@Test
	public void ignoresAPressBesideIt() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		this.press(ScrollbarNodeTest.column(bar), 515D, 180D);
		Assert.assertFalse(bar.isDragging());
	}

	@Test
	public void leavesAContentThatFitsInPlace() {
		final RectNode content = RectNode.create(100D, 100D, 400D, 100D);
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D)).scrollNode(content);
		this.bridges.open(new NodeUI(bar)).frame();
		this.bridges.move(415D, 10D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(415D, 60D).frame();
		Assert.assertTrue(bar.isDragging());
		Assert.assertEquals(0D, bar.getY(), 0D);
		Assert.assertEquals(0D, content.getTargetScrollY(), 0D);
	}

	@Test
	public void drawsNothingWithoutScrolledNode() {
		this.bridges.open(new NodeUI(new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D)))).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).isEmpty());
	}

	@Test
	public void drawsItselfInsteadOfASkeleton() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		Assert.assertSame(bar, bar.scrollNode(RectNode.create(0D, 0D, 10D, 10D)).wait(node -> false));
		this.bridges.open(new NodeUI(bar)).frame();
		Assert.assertFalse(bar.isMounted());
		Assert.assertEquals(410D, this.thumb().getLeft(), 1E-3D);
	}

	private void press(final RectNode content, final double x, final double y) {
		this.bridges.open(new NodeUI(content)).frame();
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
	}

	private Draw thumb() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private static RectNode column(final Bar bar) {
		final RectNode content = RectNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(bar);
		RectNode.create(0D, 0D, 400D, 100D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).attach(content);
		RectNode.create(0D, 100D, 400D, 100D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).attach(content);
		RectNode.create(0D, 200D, 400D, 100D).color(new Color(0.5F, 0.7F, 0.9F, 1F)).attach(content);
		return content;
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class Bar extends ScrollbarNode {

		public Bar(final double x, final double y, final double width, final double height, final BoundingBox scroll) {
			super(x, y, width, height, scroll);
		}

		@Override
		public void drawScrollbar(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.6F, 0.4F, 0.2F, 1F));
		}

	}

}