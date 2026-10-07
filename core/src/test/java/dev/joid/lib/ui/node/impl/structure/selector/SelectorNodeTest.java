package dev.joid.lib.ui.node.impl.structure.selector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		this.changes.clear();
	}

	@Test
	public void showsItsFirstOptionClosedByDefault() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		Assert.assertSame(this.first, this.selector.getSelected());
		Assert.assertTrue(this.selector.isSelected(this.first));
		Assert.assertEquals("first", this.selector.getValue());
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
	public void closesWhenDetached() {
		this.bridges.open(new NodeUI(this.selector.active(true)));
		this.selector.onDetach();
		Assert.assertFalse(this.selector.isActive());
	}

	@Test
	public void picksTheClickedOption() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(150D, 200D);
		Assert.assertSame(this.third, this.selector.getSelected());
		Assert.assertFalse(this.selector.isActive());
		Assert.assertEquals(Arrays.asList("third"), this.changes);
		Assert.assertEquals("third", this.selector.getValue());
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
	public void consumesAClickOnItsOpenList() {
		final List<String> clicks = new ArrayList<>();
		this.bridges.open(new BackgroundUI(this.selector.active(true), clicks)).frames(2);
		this.click(150D, 120D);
		Assert.assertFalse(this.selector.isActive());
		this.selector.active(true);
		this.click(150D, 160D);
		Assert.assertEquals(Arrays.asList("second"), this.changes);
		Assert.assertTrue(clicks.isEmpty());
	}

	@Test
	public void letsAClickBesideItThroughWhileClosing() {
		final List<String> clicks = new ArrayList<>();
		this.bridges.open(new BackgroundUI(this.selector.active(true), clicks)).frames(2);
		this.click(1000D, 1000D);
		Assert.assertFalse(this.selector.isActive());
		Assert.assertEquals(Arrays.asList("background"), clicks);
	}

	@Test
	public void refusesANodeAddedBesideItsValues() {
		final RectNode stranger = RectNode.create(0D, 0D, 10D, 10D).attach(this.selector);
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			this.bridges.open(new NodeUI(this.selector)).frame();
		} finally {
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains("IllegalStateException: The node RectNode is not an option of the selector, add the options with values(...)"));
		Assert.assertFalse(this.selector.getChildren().contains(stranger));
		this.bridges.frame();
		this.assertDrawn(0.1F, 0.3F, 0.5F, 100D, 140D);
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
		Assert.assertNull(empty.getValue());
		Assert.assertFalse(empty.isActive());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void reportsItsFirstSelection() {
		final List<String> selections = new ArrayList<>();
		new Selector().onChange((node, value) -> selections.add(value)).values("second", "first", "second");
		Assert.assertEquals(Arrays.asList("second"), selections);
	}

	@Test
	public void keepsItsValueWhenItsOptionsAreRebuilt() {
		this.selector.values("first", "first", "second");
		Assert.assertEquals("first", this.selector.getValue());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void dropsItsOldOptionsWhenItsOptionsAreRebuilt() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.selector.values("first", "first", "second");
		this.bridges.frame();
		Assert.assertNull(this.first.getParent());
		Assert.assertNull(this.third.getParent());
		Assert.assertSame(this.selector, this.selector.getSelected().getParent());
		Assert.assertNotSame(this.first, this.selector.getSelected());
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
		Assert.assertEquals("third", language.get());
	}

	@Test
	public void followsItsSignal() {
		final Signal<String> language = new Signal<>("second");
		this.bridges.open(new NodeUI(this.selector.signal(language))).frame();
		Assert.assertEquals("second", this.selector.getValue());
		language.set("third");
		this.bridges.frame();
		Assert.assertSame(this.third, this.selector.getSelected());
		this.assertDrawn(0.5F, 0.7F, 0.9F, 100D, 140D);
		Assert.assertEquals(Arrays.asList("second", "third"), this.changes);
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Signal<String> language = new Signal<>("second");
		final Signal<String> subtitles = new Signal<>("first");
		this.bridges.open(new NodeUI(this.selector.signal(language).signal(subtitles))).frame();
		this.selector.value("third");
		Assert.assertEquals("third", subtitles.get());
		Assert.assertEquals("second", language.get());
		language.set("first");
		this.bridges.frame();
		Assert.assertEquals("third", this.selector.getValue());
		Assert.assertSame(subtitles, this.selector.getSignal());
		Assert.assertTrue(language.getEventSet().isEmpty());
		Assert.assertEquals(1, subtitles.getEventSet().size());
	}


	@Test
	public void refusesAComputedSignal() {
		final Signal<String> language = new Signal<>("second");
		this.selector.signal(language);
		try {
			this.selector.signal(language.map(value -> value));
			Assert.fail("A ComputedSignal cannot be bound in both directions");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("Selector.signal(...) needs a writable signal: a ComputedSignal is read-only, pass it to a setter instead", expected.getMessage());
		}
		language.set("third");
		Assert.assertEquals("third", this.selector.getValue());
		Assert.assertSame(language, this.selector.getSignal());
	}
	@Test
	public void writesAChosenValueIntoItsSignal() {
		final Signal<String> language = new Signal<>("first");
		this.selector.signal(language).value("third").value("third");
		Assert.assertEquals("third", language.get());
		Assert.assertEquals(Arrays.asList("third"), this.changes);
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

	@AllArgsConstructor
	public static final class BackgroundUI extends UI {

		private final Node         node;
		private final List<String> clicks;

		@Override
		public void init() {
			RectNode.create(0D, 0D, 1920D, 1080D).onClick((node, mouseX, mouseY, clickType) -> this.clicks.add("background")).attach(this);
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