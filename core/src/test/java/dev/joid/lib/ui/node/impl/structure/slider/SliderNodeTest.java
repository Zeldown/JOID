package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.signal.Signal;

import lombok.AllArgsConstructor;

public class SliderNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueSetWithoutItsValue() {
		new Slider().valueSet(SliderNodeTest.digits(), 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsSet() {
		new Slider().valueSet(SliderNodeTest.digits(), 1).value(10);
	}

	@Test
	public void placesItsCursorOnItsValue() {
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor());
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertEquals(175D, slider.getCursor().getX(), 1E-9D);
		Assert.assertEquals(275D, this.cursor().getLeft(), 1E-3D);
		Assert.assertEquals(5, slider.getValue().intValue());
	}

	@Test
	public void movesItsCursorToANewValue() {
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor());
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertSame(slider, slider.value(9));
		this.bridges.frame();
		Assert.assertEquals(450D, this.cursor().getLeft(), 1E-3D);
		Assert.assertEquals(9, slider.getValue().intValue());
	}

	@Test
	public void changesItsValueBeforeBeingShown() {
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor()).value(7);
		Assert.assertEquals(7, slider.getValue().intValue());
		Assert.assertEquals(0D, slider.getCursor().getX(), 0D);
	}

	@Test
	public void drawsNothingWithoutValues() {
		this.bridges.open(new NodeUI(new Slider().cursor(new Cursor()))).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void drawsNothingWithoutCursor() {
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5);
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
		Assert.assertEquals(5, slider.getValue().intValue());
	}

	@Test
	public void jumpsToTheClickedPosition() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(5);
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor()).signal(signal).onChange((node, value) -> changes.add(value));
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frame();
		Assert.assertTrue(slider.getCursor().isDragging());
		Assert.assertEquals(7, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(7), changes);
		Assert.assertEquals(7, signal.getOrDefault().intValue());
		Assert.assertSame(signal, slider.getSignal());
	}

	@Test
	public void followsItsSignal() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(5);
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 1).cursor(new Cursor()).signal(signal).onChange((node, value) -> changes.add(value));
		Assert.assertEquals(5, slider.getValue().intValue());
		this.bridges.open(new NodeUI(slider)).frame();
		signal.set(2);
		this.bridges.frame();
		Assert.assertEquals(2, slider.getValue().intValue());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void writesAChosenValueIntoItsSignal() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(5);
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor()).signal(signal).onChange((node, value) -> changes.add(value));
		this.bridges.open(new NodeUI(slider)).frame();
		slider.value(3);
		this.bridges.frame();
		Assert.assertEquals(3, signal.getOrDefault().intValue());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void followsItsDraggedCursor() {
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor());
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(150D, 125D).frames(2);
		Assert.assertEquals(25D, slider.getCursor().getX(), 1E-9D);
		Assert.assertEquals(2, slider.getValue().intValue());
	}

	@Test
	public void ignoresAPressBesideIt() {
		final Slider slider = new Slider().valueSet(SliderNodeTest.digits(), 5).cursor(new Cursor());
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(1000D, 125D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frame();
		Assert.assertFalse(slider.getCursor().isDragging());
		Assert.assertEquals(5, slider.getValue().intValue());
	}

	@Test
	public void replacesItsCursor() {
		final Cursor first = new Cursor();
		final Cursor second = new Cursor();
		final Slider slider = new Slider().cursor(first).cursor(second);
		Assert.assertSame(second, slider.getCursor());
		Assert.assertSame(slider, second.getSlider());
		Assert.assertEquals(1, slider.getChildren().size());
		Assert.assertTrue(slider.getChildren().contains(second));
	}

	@Test
	public void keepsTheValueItIsGivenOnItsFirstFrames() {
		final IntegerSlider slider = new IntegerSlider().values(1, 4, 2);
		this.bridges.open(new NodeUI(slider)).frames(2);
		Assert.assertEquals(2, slider.getValue().intValue());
	}

	private Draw cursor() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private static Set<Integer> digits() {
		return new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
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

	public static final class IntegerSlider extends IntegerSliderNode {

		public IntegerSlider() {
			super(100D, 100D, 400D, 50D);
			super.cursor(new Cursor());
		}

		@Override
		public void drawSlider(final double mouseX, final double mouseY) {}

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