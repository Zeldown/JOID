package dev.joid.lib.ui.node.impl.structure.slider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import lombok.AllArgsConstructor;

public class SliderThumbNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private Thumb thumb;
	private Slider slider;

	@Before
	public void openASlider() {
		this.thumb = new Thumb();
		this.slider = new Slider().values(1, 2, 3).thumb(this.thumb);
		this.bridges.open(new NodeUI(this.slider)).frame();
	}

	@Test
	public void knowsItsSlider() {
		Assert.assertSame(this.slider, this.thumb.getSlider());
		Assert.assertSame(this.thumb, this.thumb.slider(this.slider));
	}

	@Test
	public void followsTheMouseWhileDragged() {
		Assert.assertSame(this.thumb, this.thumb.dragging(true));
		this.bridges.move(300D, 125D).frame();
		Assert.assertEquals(175D, this.thumb.getX(), 1E-9D);
	}

	@Test
	public void staysOnItsTrack() {
		this.thumb.dragging(true);
		this.bridges.move(0D, 125D).frame();
		Assert.assertEquals(0D, this.thumb.getX(), 1E-9D);
		this.bridges.move(1900D, 125D).frame();
		Assert.assertEquals(350D, this.thumb.getX(), 1E-9D);
	}

	@Test
	public void grabsThePressedThumb() {
		this.bridges.move(110D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(this.thumb.isDragging());
		Assert.assertEquals(0D, this.thumb.getX(), 1E-9D);
	}

	@Test
	public void letsGoOnRelease() {
		this.bridges.move(110D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.move(400D, 125D).frame();
		Assert.assertFalse(this.thumb.isDragging());
		Assert.assertEquals(0D, this.thumb.getX(), 1E-9D);
	}

	@Test
	public void staysHoveredWhileDragged() {
		this.bridges.move(110D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(400D, 600D).frames(2);
		Assert.assertTrue(this.thumb.isHovered());
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(2);
		Assert.assertFalse(this.thumb.isHovered());
	}

	@Test
	public void ignoresAPressBesideIt() {
		this.bridges.move(1000D, 1000D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(this.thumb.isDragging());
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class Slider extends SliderNode<Integer> {

		public Slider() {
			super(100D, 100D, 400D, 50D);
		}

		@Override
		public void drawSlider(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.2F, 0.4F, 0.6F, 1F));
		}

	}

	public static final class Thumb extends SliderThumbNode {

		public Thumb() {
			super(50D, 50D);
		}

		@Override
		public void drawThumb(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.6F, 0.4F, 0.2F, 1F));
		}

	}

}