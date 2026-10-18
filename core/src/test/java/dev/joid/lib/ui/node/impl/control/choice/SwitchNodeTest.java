package dev.joid.lib.ui.node.impl.control.choice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class SwitchNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> changes = new ArrayList<>();

	@Test
	public void startsOnItsFirstState() {
		final Switch node = this.open();
		Assert.assertEquals("low", node.getValue());
		Assert.assertEquals(0, node.getValueIndex());
		Assert.assertEquals(Arrays.asList("low", "medium", "high"), node.getValueList());
	}

	@Test
	public void jumpsToANamedState() {
		final Switch node = this.open();
		Assert.assertSame(node, node.value("high"));
		Assert.assertEquals("high", node.getValue());
		Assert.assertEquals(2, node.getValueIndex());
		Assert.assertEquals(Arrays.asList("high"), this.changes);
	}

	@Test
	public void jumpsToAnIndex() {
		final Switch node = this.open();
		Assert.assertSame(node, node.valueIndex(1));
		Assert.assertEquals("medium", node.getValue());
		Assert.assertEquals(Arrays.asList("medium"), this.changes);
	}

	@Test
	public void staysInSyncWithItsSignal() {
		final Switch node = this.open();
		final Signal<String> quality = new Signal<>("high");
		Assert.assertSame(node, node.value(quality));
		Assert.assertEquals("high", node.getValue());
		quality.set("medium");
		Assert.assertEquals("medium", node.getValue());
		node.values("low", "high");
		Assert.assertEquals("low", quality.get());
		node.value("high");
		Assert.assertEquals("low", quality.get());
		quality.set("low");
		Assert.assertEquals("high", node.getValue());
		Assert.assertEquals(Arrays.asList("high", "medium", "low", "high"), this.changes);
	}

	@Test
	public void keepsItsSignalWhenItsSkinChoosesAState() {
		final Switch node = this.open();
		final Signal<String> quality = new Signal<>("high");
		node.<Switch>value(quality).pick("low");
		Assert.assertEquals("low", quality.get());
		quality.set("medium");
		Assert.assertEquals("medium", node.getValue());
	}

	@Test
	public void bindsItsIndexBothWays() {
		final Switch node = this.open();
		final IntegerSignal level = new IntegerSignal(2);
		final Signal<String> quality = new Signal<>("low");
		node.valueIndex(level);
		Assert.assertEquals("high", node.getValue());
		level.set(1);
		Assert.assertEquals("medium", node.getValue());
		Assert.assertEquals(1, node.getValueIndex());
		node.value(quality);
		Assert.assertEquals("low", node.getValue());
		level.set(2);
		Assert.assertEquals("low", node.getValue());
		Assert.assertTrue(SwitchNodeTest.subscribers(level).isEmpty());
		node.valueIndex(level);
		Assert.assertEquals("high", node.getValue());
		quality.set("medium");
		Assert.assertEquals("high", node.getValue());
		Assert.assertTrue(SwitchNodeTest.subscribers(quality).isEmpty());
		node.valueIndex(0);
		Assert.assertEquals(2, level.get().intValue());
		Assert.assertTrue(SwitchNodeTest.subscribers(level).isEmpty());
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Switch node = this.open();
		final Signal<String> quality = new Signal<>("high");
		final Signal<String> shadows = new Signal<>("medium");
		node.value(quality).value(shadows);
		shadows.set("low");
		Assert.assertEquals("high", quality.get());
		quality.set("medium");
		Assert.assertEquals("low", node.getValue());
		Assert.assertTrue(SwitchNodeTest.subscribers(quality).isEmpty());
		Assert.assertEquals(1, SwitchNodeTest.subscribers(shadows).size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesASignalItCannotWrite() {
		new Switch().values("low", "high").value(new Signal<>("high").map(value -> value));
	}

	@Test
	public void startsOnAChosenIndex() {
		final Switch node = this.open();
		Assert.assertSame(node, node.values("off", "on").valueIndex(1));
		Assert.assertEquals("on", node.getValue());
	}

	@Test
	public void startsOnAChosenState() {
		final Switch node = this.open();
		Assert.assertSame(node, node.values("off", "eco", "on").value("eco"));
		Assert.assertEquals("eco", node.getValue());
		Assert.assertEquals(1, node.getValueIndex());
	}

	@Test
	public void goesBackToTheFirstOfNewStates() {
		final Switch node = this.open();
		node.valueIndex(2).values("off", "on");
		Assert.assertEquals("off", node.getValue());
		Assert.assertEquals(Arrays.asList("high", "off"), this.changes);
	}

	@Test
	public void keepsItsStateWhenItRemainsAState() {
		final Switch node = this.open();
		node.value("medium");
		this.changes.clear();
		node.values("medium", "high");
		Assert.assertEquals("medium", node.getValue());
		Assert.assertEquals(0, node.getValueIndex());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void keepsItsChildrenWhenItsIndexChanges() {
		final Switch node = this.open();
		final int loads = node.loads;
		node.valueIndex(2).value("medium");
		node.values("low", "medium", "high");
		Assert.assertEquals(loads, node.loads);
		Assert.assertEquals(3, node.getChildren().size());
	}

	@Test
	public void rebuildsItsChildrenWhenItsStatesChange() {
		final Switch node = this.open();
		final int loads = node.loads;
		node.values("off", "on");
		Assert.assertEquals(loads + 2, node.loads);
		Assert.assertEquals(2, node.getChildren().size());
		node.values("off", "on", "eco");
		Assert.assertEquals(loads + 5, node.loads);
		Assert.assertEquals(3, node.getChildren().size());
	}

	@Test
	public void copiesTheStatesItIsGiven() {
		final Switch node = this.open();
		final List<String> states = new ArrayList<>(Arrays.asList("off", "on"));
		node.values(() -> states);
		states.add("eco");
		Assert.assertEquals(Arrays.asList("off", "on"), node.getValueList());
		Assert.assertNotSame(states, node.getValueList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesNoState() {
		new Switch().values();
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnIndexOutOfItsStates() {
		new Switch().values("off", "on").valueIndex(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAStateOutOfItsStates() {
		new Switch().values("off", "on").value("eco");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesToJumpOutOfItsStates() {
		this.open().valueIndex(3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesToJumpToAnUnknownState() {
		this.open().value("eco");
	}

	@Test
	public void takesAnIndexGivenBeforeItsStates() {
		Assert.assertEquals("on", new Switch().valueIndex(1).values("off", "on").getValue());
	}

	@Test
	public void takesAStateGivenBeforeItsStates() {
		Assert.assertEquals("eco", new Switch().value("eco").values("off", "on", "eco").getValue());
	}

	@Test
	public void takesTheStateOfItsSignalWhenItsStatesArrive() {
		final Signal<String> quality = new Signal<>("high");
		Assert.assertEquals("high", new Switch().value(quality).values("low", "medium", "high").getValue());
		Assert.assertEquals("high", quality.get());
	}

	@Test
	public void takesItsFirstStateWhenItsSignalArrivesOutOfItsStates() {
		Assert.assertEquals("off", new Switch().value(new Signal<>("eco")).values("off", "on").getValue());
	}

	@Test
	public void takesItsFirstStateWhenItsIndexSignalArrivesOutOfItsStates() {
		Assert.assertEquals("off", new Switch().valueIndex(new IntegerSignal(5)).values("off", "on").getValue());
	}

	@Test
	public void keepsAnIndexOutOfItsStatesUntilItsStatesReachIt() {
		final Switch node = this.open();
		final IntegerSignal level = new IntegerSignal(1);
		node.valueIndex(level);
		level.set(5);
		Assert.assertEquals("medium", node.getValue());
		Assert.assertEquals(1, level.get().intValue());
		node.values("a", "b", "c", "d", "e", "f");
		Assert.assertEquals("f", node.getValue());
		Assert.assertEquals(5, level.get().intValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnIndexGivenBeforeItsStatesThatIsOutOfThem() {
		new Switch().valueIndex(2).values("off", "on");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAStateGivenBeforeItsStatesThatIsOutOfThem() {
		new Switch().value("eco").values("off", "on");
	}

	@Test
	public void refusesToGiveItsStateBeforeItsStates() {
		try {
			new Switch().getValue();
			Assert.fail("A switch without states has no state to give");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Switch has no values, call values(...) before getValue()", expected.getMessage());
		}
	}

	@Test
	public void refusesToBeAttachedWithoutStates() {
		try {
			this.bridges.open(new SingleUI(new Switch()));
			Assert.fail("A switch without states cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Switch has no values, call values(...) before attaching it", expected.getMessage());
		}
	}

	@Test
	public void reportsNoChangeForItsStartingState() {
		final Switch node = new Switch().onChange(event -> this.changes.add(event.getValue())).values("off", "on").value("on");
		this.bridges.open(new SingleUI(node));
		Assert.assertEquals("on", node.getValue());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void letsItsListenersRefuseOnlyItsSkin() {
		final Switch node = this.open();
		node.beforeChange(event -> event.cancel());
		node.value("high");
		node.valueIndex(1);
		Assert.assertEquals("medium", node.getValue());
		node.pick("low");
		Assert.assertEquals("medium", node.getValue());
		Assert.assertEquals(Arrays.asList("high", "medium"), this.changes);
	}

	@Test
	public void buildsItsChildrenOnceWhenCreated() {
		final SwitchUI ui = new SwitchUI(new ArrayList<>());
		this.bridges.open(ui).frame();
		Assert.assertEquals(3, ui.node.loads);
		Assert.assertEquals(3, ui.node.getChildren().size());
	}

	@Test
	public void picksTheOptionUnderAClick() {
		final Switch node = this.open();
		this.click(205D, 120D);
		Assert.assertEquals("high", node.getValue());
		this.click(150D, 120D);
		Assert.assertEquals("medium", node.getValue());
		Assert.assertEquals(180D, node.getOptionList().get(1).getAbsoluteX() + node.getOptionList().get(1).getWidth(), 0D);
		Assert.assertEquals(Arrays.asList("high", "medium"), this.changes);
	}

	@Test
	public void picksTheOptionUnderAClickWhereverItsSkinPlacesIt() {
		final VerticalSwitch node = new VerticalSwitch();
		this.bridges.open(new SingleUI(node)).frame();
		this.click(110D, 150D);
		Assert.assertEquals("high", node.getValue());
		this.click(110D, 115D);
		Assert.assertEquals("medium", node.getValue());
		this.click(110D, 105D);
		Assert.assertEquals("low", node.getValue());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
	}

	private Switch open() {
		final SwitchUI ui = new SwitchUI(this.changes);
		this.bridges.open(ui).frame();
		return ui.node;
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

	@RequiredArgsConstructor
	public static final class SingleUI extends UI {

		private final SwitchNode<String> node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	@RequiredArgsConstructor
	public static final class SwitchUI extends UI {

		private final List<String> changes;

		private Switch node;

		@Override
		public void init() {
			this.node = new Switch().values("low", "medium", "high").onChange(event -> this.changes.add(event.getValue()));
			super.add(this.node);
		}

	}

	public static final class Switch extends SwitchNode<String> {

		private int loads;

		public Switch() {
			super(100D, 100D, 120D, 40D);
			super.option((state, index) -> {
				this.loads++;
				return RectNode.create(0D, 0D, 10D, 10D);
			});
		}

		public void pick(final @NonNull String state) {
			super.change(state);
		}

	}

	public static final class VerticalSwitch extends SwitchNode<String> {

		public VerticalSwitch() {
			super(100D, 100D, 40D, 120D);
			super.values("low", "medium", "high");
			super.option((state, index) -> RectNode.create(0D, 0D, 10D, 10D));
		}

		@Override
		public void layout() {
			final double[] heights = {10D, 20D, 90D};
			double offset = 0D;
			for (int i = 0; i < heights.length; i++) {
				super.getOptionList().get(i).layoutPosition(0D, offset).layoutSize(super.getWidth(), heights[i]);
				offset += heights[i];
			}
		}

	}

}