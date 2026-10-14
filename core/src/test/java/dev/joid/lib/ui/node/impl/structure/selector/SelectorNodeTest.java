package dev.joid.lib.ui.node.impl.structure.selector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;

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
		this.selector = new Selector().onChange(event -> this.changes.add(event.getValue())).values("first", "second", "third");
		this.first = (RectNode) this.selector.getChildren().get(0);
		this.second = (RectNode) this.selector.getChildren().get(1);
		this.third = (RectNode) this.selector.getChildren().get(2);
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
		Assert.assertEquals(120D, this.selector.getHeight(), 0D);
		Assert.assertEquals(20D, this.selector.getY(), 0D);
		this.assertDrawn(0.1F, 0.3F, 0.5F, 100D, 140D);
		this.assertDrawn(0.3F, 0.5F, 0.7F, 60D, 100D);
		this.assertDrawn(0.5F, 0.7F, 0.9F, 20D, 60D);
	}

	@Test
	public void picksAnOptionAboveItWhenItOpensUpwards() {
		this.bridges.open(new NodeUI(this.selector.direction(SelectorDirection.UP))).frame();
		this.click(150D, 120D);
		Assert.assertTrue(this.selector.isActive());
		Assert.assertEquals(20D, this.selector.getY(), 0D);
		this.click(150D, 40D);
		Assert.assertEquals("third", this.selector.getValue());
		Assert.assertFalse(this.selector.isActive());
		Assert.assertEquals(100D, this.selector.getY(), 0D);
		Assert.assertEquals(40D, this.selector.getHeight(), 0D);
		Assert.assertEquals(Arrays.asList("third"), this.changes);
	}

	@Test
	public void closesWhenDetached() {
		this.bridges.open(new NodeUI(this.selector.active(true)));
		this.selector.fireDetach();
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
	public void refusesToBeAttachedWithoutValues() {
		try {
			this.bridges.open(new NodeUI(new Selector()));
			Assert.fail("A selector without values cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Selector has no values, call values(...) before attaching it", expected.getMessage());
		}
	}

	@Test
	public void refusesToGiveAValueWithoutValues() {
		try {
			new Selector().getValue();
			Assert.fail("A selector without values has no value");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Selector has no values, call values(...) before getValue()", expected.getMessage());
		}
	}

	@Test
	public void reportsNoChangeForItsStartingValue() {
		final List<String> selections = new ArrayList<>();
		final Selector selector = new Selector().onChange(event -> selections.add(event.getValue())).values("first", "second").value("second");
		this.bridges.open(new NodeUI(selector));
		Assert.assertEquals("second", selector.getValue());
		Assert.assertTrue(selections.isEmpty());
	}

	@Test
	public void takesAValueGivenBeforeItsOptions() {
		Assert.assertEquals("second", new Selector().value("second").values("first", "second", "third").getValue());
	}

	@Test
	public void takesTheValueOfItsSignalWhenItsOptionsArrive() {
		final Signal<String> language = new Signal<>("third");
		final Selector selector = new Selector().value(language).values("first", "second", "third");
		Assert.assertEquals("third", selector.getValue());
		Assert.assertEquals("third", language.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueGivenBeforeItsOptionsThatIsNoOption() {
		new Selector().value("fourth").values("first", "second");
	}

	@Test
	public void neverRefusesItsSettersNorItsSignal() {
		final Signal<String> language = new Signal<>("first");
		this.bridges.open(new NodeUI(this.selector.value(language).<Selector>beforeChange(event -> event.cancel())));
		this.selector.value("second");
		Assert.assertEquals("second", this.selector.getValue());
		language.set("third");
		Assert.assertEquals("second", this.selector.getValue());
		this.selector.value(language);
		Assert.assertEquals("third", this.selector.getValue());
		Assert.assertEquals(Arrays.asList("second", "third"), this.changes);
	}

	@Test
	public void keepsEveryValueAsAnOption() {
		final Selector selector = new Selector().values("easy", "normal", "hard");
		Assert.assertEquals(Arrays.asList("easy", "normal", "hard"), new ArrayList<>(selector.getOptionMap().values()));
		Assert.assertEquals("easy", selector.getValue());
	}

	@Test
	public void selectsTheGivenValue() {
		final List<String> selections = new ArrayList<>();
		final Selector selector = new Selector().onChange(event -> selections.add(event.getValue())).values("easy", "normal", "hard");
		this.bridges.open(new NodeUI(selector));
		selector.value("normal");
		Assert.assertEquals("normal", selector.getValue());
		Assert.assertEquals(Arrays.asList("normal"), selections);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyOptionList() {
		new Selector().values();
	}

	@Test
	public void selectsItsFirstOptionWhenItsValueIsGone() {
		this.bridges.open(new NodeUI(this.selector));
		this.selector.value("third");
		this.changes.clear();
		this.selector.values("first", "second");
		Assert.assertEquals("first", this.selector.getValue());
		Assert.assertEquals(Arrays.asList("first"), this.changes);
	}

	@Test
	public void keepsItsValueWhenItsOptionsAreRebuilt() {
		this.selector.values("first", "second");
		Assert.assertEquals("first", this.selector.getValue());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void dropsItsOldOptionsWhenItsOptionsAreRebuilt() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.selector.values("first", "second");
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
		this.bridges.open(new NodeUI(this.selector.value(language).active(true))).frames(2);
		this.click(150D, 200D);
		Assert.assertEquals("third", language.get());
	}

	@Test
	public void followsItsSignal() {
		final Signal<String> language = new Signal<>("second");
		this.bridges.open(new NodeUI(this.selector.value(language))).frame();
		Assert.assertEquals("second", this.selector.getValue());
		language.set("third");
		this.bridges.frame();
		Assert.assertSame(this.third, this.selector.getSelected());
		this.assertDrawn(0.5F, 0.7F, 0.9F, 100D, 140D);
		Assert.assertEquals(Arrays.asList("third"), this.changes);
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Signal<String> language = new Signal<>("second");
		final Signal<String> subtitles = new Signal<>("first");
		this.bridges.open(new NodeUI(this.selector.value(language).value(subtitles))).frame();
		subtitles.set("third");
		Assert.assertEquals("second", language.get());
		language.set("first");
		this.bridges.frame();
		Assert.assertEquals("third", this.selector.getValue());
		Assert.assertTrue(SelectorNodeTest.subscribers(language).isEmpty());
		Assert.assertEquals(1, SelectorNodeTest.subscribers(subtitles).size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesASignalItCannotWrite() {
		this.selector.value(new Signal<>("second").map(value -> value));
	}

	@Test
	public void replacesItsSignalWithAnySetter() {
		final Signal<String> language = new Signal<>("first");
		this.bridges.open(new NodeUI(this.selector.value(language)));
		this.selector.values("second", "third");
		Assert.assertEquals("second", language.get());
		this.selector.value("third").value("third");
		Assert.assertEquals("second", language.get());
		language.set("third");
		language.set("second");
		Assert.assertEquals("third", this.selector.getValue());
		Assert.assertTrue(SelectorNodeTest.subscribers(language).isEmpty());
		Assert.assertEquals(Arrays.asList("second", "third"), this.changes);
	}

	@Test
	public void writesItsOpeningIntoItsSignal() {
		final Signal<Boolean> open = new Signal<>(false);
		this.bridges.open(new NodeUI(this.selector.active(open))).frame();
		this.click(150D, 120D);
		Assert.assertTrue(open.get());
		this.click(150D, 200D);
		Assert.assertFalse(open.get());
		open.set(true);
		Assert.assertTrue(this.selector.isActive());
	}

	@Test
	public void forgetsItsOpeningSignalOnceGivenAValue() {
		final Signal<Boolean> open = new Signal<>(false);
		this.bridges.open(new NodeUI(this.selector.active(open).active(true))).frame();
		Assert.assertTrue(this.selector.isActive());
		this.click(150D, 120D);
		open.set(true);
		Assert.assertFalse(this.selector.isActive());
		Assert.assertTrue(SelectorNodeTest.subscribers(open).isEmpty());
	}

	@Test
	public void opensLikeAClickThroughItsGestures() {
		final List<String> events = new ArrayList<>();
		this.selector.beforeOpen(event -> events.add("before open")).onOpen(event -> events.add("open")).beforeClose(event -> events.add("before close")).onClose(event -> events.add("close"));
		this.bridges.open(new NodeUI(this.selector)).frame();
		Assert.assertSame(this.selector, this.selector.open());
		Assert.assertSame(this.selector, this.selector.close());
		this.selector.active(true);
		Assert.assertEquals(Arrays.asList("before open", "open", "before close", "close", "open"), events);
	}

	@Test
	public void staysClosedWhenItsOpeningIsRefused() {
		final Signal<Boolean> open = new Signal<>(false);
		this.bridges.open(new NodeUI(this.selector.active(open).beforeOpen(event -> event.cancel()))).frame();
		this.click(150D, 120D);
		this.selector.open();
		Assert.assertFalse(this.selector.isActive());
		Assert.assertFalse(open.get());
		open.set(true);
		Assert.assertTrue(this.selector.isActive());
	}

	@Test
	public void opensDownwardsOnlyInItsDownDirection() {
		Assert.assertTrue(SelectorDirection.DOWN.isDown());
		Assert.assertFalse(SelectorDirection.UP.isDown());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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

	private static Set<?> subscribers(final Signal<?> signal) {
		try {
			final Field field = Signal.class.getDeclaredField("listenerSet");
			field.setAccessible(true);
			return (Set<?>) field.get(signal);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
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
			RectNode.create(0D, 0D, 1920D, 1080D).onClick(event -> this.clicks.add("background")).attach(this);
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