package dev.joid.lib.ui.node.impl.structure.scrollbar;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class ScrollbarNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void followsTheScrolledContent() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.bridges.open(new NodeUI(content)).frame();
		Assert.assertSame(content, bar.getScrollNode());
		content.scrollY(-100D).updateScroll();
		this.bridges.frame();
		final Draw thumb = this.thumb();
		Assert.assertEquals(133D, thumb.getTop(), 1E-3D);
		Assert.assertEquals(166D, thumb.getBottom(), 1E-3D);
		Assert.assertEquals(510D, thumb.getLeft(), 1E-3D);
	}

	@Test
	public void scrollsTheContentVerticallyWhenDragged() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		Assert.assertTrue(bar.isDragging());
		this.bridges.move(515D, 150D).frame();
		Assert.assertEquals(40D, bar.getThumb().getY(), 1E-9D);
		Assert.assertEquals(-120D, content.getTargetScrollY(), 1E-6D);
		this.bridges.move(515D, 900D).frame();
		Assert.assertEquals(200D / 3D, bar.getThumb().getY(), 1E-9D);
		Assert.assertEquals(-200D, content.getTargetScrollY(), 1E-6D);
	}

	@Test
	public void scrollsTheContentHorizontallyWhenDragged() {
		final Bar bar = new Bar(0D, 110D, 400D, 10D, 40D);
		final RectNode content = RectNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(bar);
		RectNode.create(0D, 0D, 400D, 100D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).attach(content);
		RectNode.create(400D, 0D, 400D, 100D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).attach(content);
		this.press(content, 120D, 215D);
		this.bridges.move(300D, 215D).frame();
		Assert.assertEquals(180D, bar.getThumb().getX(), 1E-9D);
		Assert.assertEquals(-360D, content.getTargetScrollX(), 1E-6D);
		this.bridges.move(0D, 215D).frame();
		Assert.assertEquals(0D, bar.getThumb().getX(), 1E-9D);
		Assert.assertEquals(0D, content.getTargetScrollX(), 1E-6D);
	}

	@Test
	public void writesItsDragIntoTheScrollSignalOfItsContent() {
		final DoubleSignal scroll = new DoubleSignal(0D);
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar).scrollY(scroll);
		this.press(content, 515D, 110D);
		this.bridges.move(515D, 150D).frame();
		Assert.assertEquals(-120D, scroll.peek(), 1E-6D);
	}

	@Test
	public void leavesItsContentWhenItsScrollIsRefused() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar).beforeScroll(event -> event.cancel());
		this.press(content, 515D, 110D);
		this.bridges.move(515D, 150D).frame();
		Assert.assertEquals(0D, content.getTargetScrollY(), 0D);
	}

	@Test
	public void letsGoOnRelease() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.move(515D, 150D).frame();
		Assert.assertFalse(bar.isDragging());
		Assert.assertEquals(0D, content.getTargetScrollY(), 0D);
	}

	@Test
	public void letsGoWhenDetached() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		Assert.assertTrue(bar.isDragging());
		content.fireDetach();
		Assert.assertFalse(bar.isDragging());
	}

	@Test
	public void keepsDraggingUntilTheButtonThatStartedIt() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 110D);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		Assert.assertTrue(bar.isDragging());
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertFalse(bar.isDragging());
	}

	@Test
	public void drivesTheAxisOfItsTrack() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		RectNode.create(0D, 0D, 600D, 10D).attach(content);
		Assert.assertFalse(bar.isHorizontal());
		this.press(content, 515D, 110D);
		Assert.assertTrue(content.hasOverflowX());
		this.bridges.move(515D, 150D).frame();
		Assert.assertEquals(40D, bar.getThumb().getY(), 1E-9D);
		Assert.assertEquals(0D, bar.getThumb().getX(), 0D);
		Assert.assertEquals(-120D, content.getTargetScrollY(), 1E-6D);
		Assert.assertEquals(0D, content.getTargetScrollX(), 0D);
	}

	@Test
	public void jumpsToAPressOnItsTrack() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.press(content, 515D, 180D);
		Assert.assertTrue(bar.isDragging());
		this.bridges.frame();
		Assert.assertEquals(-190D, content.getTargetScrollY(), 1E-6D);
	}

	@Test
	public void readsItsProgressAndTheShareOfItsContentInView() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final RectNode content = ScrollbarNodeTest.column(bar);
		this.bridges.open(new NodeUI(content)).frame();
		Assert.assertEquals(0D, bar.getProgress(), 0D);
		Assert.assertEquals(1D / 3D, bar.getRatio(), 1E-9D);
		content.scrollY(-100D).updateScroll();
		Assert.assertEquals(0.5D, bar.getProgress(), 1E-9D);
	}

	@Test
	public void leavesAContentThatFitsInPlace() {
		final RectNode content = RectNode.create(100D, 100D, 400D, 100D);
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D).scrollNode(content);
		this.bridges.open(new NodeUI(bar)).frame();
		this.bridges.move(415D, 10D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(415D, 60D).frame();
		Assert.assertTrue(bar.isDragging());
		Assert.assertEquals(0D, bar.getThumb().getY(), 0D);
		Assert.assertEquals(0D, content.getTargetScrollY(), 0D);
	}

	@Test
	public void refusesToBeAttachedWithoutScrolledNode() {
		try {
			this.bridges.open(new NodeUI(new Bar(410D, 0D, 10D, 100D, 20D)));
			Assert.fail("A scrollbar without scrolled node cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Bar has no scrolled node, pass it to node.scrollbar(...) instead of attaching it", expected.getMessage());
		}
	}

	private void press(final RectNode content, final double x, final double y) {
		this.bridges.open(new NodeUI(content)).frame();
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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

		private final double length;

		public Bar(final double x, final double y, final double width, final double height, final double length) {
			super(x, y, width, height);
			this.length = length;
			super.thumb(RectNode.create(0D, 0D, width > height ? length : 0D, width > height ? 0D : length).color(new Color(0.6F, 0.4F, 0.2F, 1F)));
		}

		@Override
		protected @NonNull Node instantiate() {
			return new Bar(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.length);
		}

	}

}