package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.signal.Signal;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class SelectorNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> changes = new ArrayList<>();

	private Selector selector;
	private RectNode first;
	private RectNode second;
	private RectNode third;

	@Before
	public void createAnOptionList() {
		this.selector = new Selector().onChange((node, value) -> this.changes.add(value)).values("first", "first", "second", "third");
		this.first = (RectNode) this.selector.getChildren().ordered().get(0);
		this.second = (RectNode) this.selector.getChildren().ordered().get(1);
		this.third = (RectNode) this.selector.getChildren().ordered().get(2);
	}

	@Test
	public void showsItsFirstOptionClosedByDefault() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		Assert.assertSame(this.first, this.selector.getSelected());
		Assert.assertTrue(this.selector.isSelected(this.first));
		Assert.assertEquals(Optional.of("first"), this.selector.getValue());
		Assert.assertFalse(this.selector.isActive());
		Assert.assertSame(SelectorDirection.DOWN, this.selector.getDirection());
		Assert.assertEquals(40D, this.selector.getHeight(), 0D);
		this.assertDrawn(0.1F, 0.3F, 0.5F, 100D, 140D);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).isEmpty());
		Assert.assertEquals(200D, this.first.getWidth(), 0D);
	}

	@Test
	public void opensDownwardsOnAClickOnItsSelection() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.click(150D, 120D);
		Assert.assertTrue(this.selector.isActive());
		Assert.assertEquals(120D, this.selector.getHeight(), 0D);
		this.assertDrawn(0.3F, 0.5F, 0.7F, 140D, 180D);
		this.assertDrawn(0.5F, 0.7F, 0.9F, 180D, 220D);
	}

	@Test
	public void opensUpwards() {
		Assert.assertSame(this.selector, this.selector.direction(SelectorDirection.UP).active(true));
		this.bridges.open(new NodeUI(this.selector)).frames(2);
		Assert.assertEquals(40D, this.selector.getHeight(), 0D);
		this.assertDrawn(0.3F, 0.5F, 0.7F, 60D, 100D);
		this.assertDrawn(0.5F, 0.7F, 0.9F, 20D, 60D);
	}

	@Test
	public void picksTheClickedOption() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(150D, 200D);
		Assert.assertSame(this.third, this.selector.getSelected());
		Assert.assertFalse(this.selector.isActive());
		Assert.assertEquals(Arrays.asList("third"), this.changes);
		Assert.assertEquals(Optional.of("third"), this.selector.getValue());
		this.assertDrawn(0.5F, 0.7F, 0.9F, 100D, 140D);
	}

	@Test
	public void closesOnAClickOnItsSelection() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(150D, 120D);
		Assert.assertFalse(this.selector.isActive());
		Assert.assertSame(this.first, this.selector.getSelected());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void closesOnAClickBesideIt() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(1000D, 1000D);
		Assert.assertFalse(this.selector.isActive());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void staysClosedOnAClickBesideIt() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.click(1000D, 1000D);
		Assert.assertFalse(this.selector.isActive());
	}

	@Test
	public void showsAChosenOption() {
		Assert.assertSame(this.selector, this.selector.value("second"));
		Assert.assertSame(this.second, this.selector.getSelected());
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.assertDrawn(0.3F, 0.5F, 0.7F, 100D, 140D);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.1F, 0.3F, 0.5F).isEmpty());
	}

	@Test
	public void drawsNothingWithoutOptions() {
		final Selector empty = new Selector();
		this.bridges.open(new NodeUI(empty)).frame();
		this.click(150D, 120D);
		Assert.assertNull(empty.getSelected());
		Assert.assertFalse(empty.getValue().isPresent());
		Assert.assertFalse(empty.isActive());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueThatIsNoOption() {
		this.selector.value("fourth");
	}

	@Test
	public void writesThePickedValueIntoItsSignal() {
		final Signal<String> language = new Signal<>();
		this.bridges.open(new NodeUI(this.selector.signal(language).active(true))).frames(2);
		this.click(150D, 200D);
		Assert.assertEquals("third", language.getOrDefault());
	}

	@Test
	public void followsItsSignal() {
		final Signal<String> language = new Signal<>("second");
		this.bridges.open(new NodeUI(this.selector.signal(language))).frame();
		Assert.assertEquals(Optional.of("second"), this.selector.getValue());
		language.set("third");
		this.bridges.frame();
		Assert.assertSame(this.third, this.selector.getSelected());
		this.assertDrawn(0.5F, 0.7F, 0.9F, 100D, 140D);
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void writesAChosenValueIntoItsSignal() {
		final Signal<String> language = new Signal<>("first");
		this.selector.signal(language).value("third");
		Assert.assertEquals("third", language.getOrDefault());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void opensDownwardsOnlyInItsDownDirection() {
		Assert.assertTrue(SelectorDirection.DOWN.isDown());
		Assert.assertFalse(SelectorDirection.UP.isDown());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frame();
	}

	private void assertDrawn(final float red, final float green, final float blue, final double top, final double bottom) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(100D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(300D, draws.get(0).getRight(), 1E-3D);
		Assert.assertEquals(top, draws.get(0).getTop(), 1E-3D);
		Assert.assertEquals(bottom, draws.get(0).getBottom(), 1E-3D);
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class Selector extends SelectorNode<String> {

		public Selector() {
			super(100D, 100D, 200D, 40D);
		}

		@Override
		protected @NonNull Node option(final @NonNull String value) {
			if ("first".equals(value)) {
				return RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.1F, 0.3F, 0.5F, 1F));
			}
			if ("second".equals(value)) {
				return RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.3F, 0.5F, 0.7F, 1F));
			}
			return RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.5F, 0.7F, 0.9F, 1F));
		}

		@Override
		public void drawBackground(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.2F, 0.4F, 0.6F, 1F));
		}

	}

}