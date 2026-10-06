package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.click.ClickType;
import lombok.AllArgsConstructor;

public class SliderCursorNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private Slider slider;
	private Cursor cursor;

	@Before
	public void openASlider() {
		this.cursor = new Cursor();
		this.slider = new Slider().valueSet(new LinkedHashSet<>(Arrays.asList(1, 2, 3)), 1).cursor(this.cursor);
		this.bridges.open(new NodeUI(this.slider)).frame();
	}

	@Test
	public void knowsItsSlider() {
		Assert.assertSame(this.slider, this.cursor.getSlider());
		Assert.assertSame(this.cursor, this.cursor.slider(this.slider));
	}

	@Test
	public void followsTheMouseWhileDragged() {
		Assert.assertSame(this.cursor, this.cursor.dragging(true));
		this.bridges.move(300D, 125D).frame();
		Assert.assertEquals(175D, this.cursor.getX(), 1E-9D);
	}

	@Test
	public void staysOnItsTrack() {
		this.cursor.dragging(true);
		this.bridges.move(0D, 125D).frame();
		Assert.assertEquals(0D, this.cursor.getX(), 1E-9D);
		this.bridges.move(1900D, 125D).frame();
		Assert.assertEquals(350D, this.cursor.getX(), 1E-9D);
	}

	@Test
	public void grabsThePressedCursor() {
		this.bridges.move(110D, 125D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(this.cursor.isDragging());
		Assert.assertEquals(0D, this.cursor.getX(), 1E-9D);
	}

	@Test
	public void letsGoOnRelease() {
		this.bridges.move(110D, 125D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.move(400D, 125D).frame();
		Assert.assertFalse(this.cursor.isDragging());
		Assert.assertEquals(0D, this.cursor.getX(), 1E-9D);
	}

	@Test
	public void ignoresAPressBesideIt() {
		this.bridges.move(1000D, 1000D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertFalse(this.cursor.isDragging());
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

	public static final class Cursor extends SliderCursorNode {

		public Cursor() {
			super(50D, 50D);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.6F, 0.4F, 0.2F, 1F));
		}

	}

}