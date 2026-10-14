package dev.joid.lib.ui.node.impl.structure.sw;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class SwitchNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> changes = new ArrayList<>();

	@Test
	public void startsOnItsFirstState() {
		final Switch node = this.open();
		Assert.assertEquals("low", node.getState());
		Assert.assertEquals(0, node.getStateIndex().get().intValue());
		Assert.assertEquals(Arrays.asList("low", "medium", "high"), node.getStateList().get());
	}

	@Test
	public void jumpsToANamedState() {
		final Switch node = this.open();
		Assert.assertSame(node, node.state("high"));
		Assert.assertEquals("high", node.getState());
		Assert.assertEquals(2, node.getStateIndex().get().intValue());
		Assert.assertEquals(Arrays.asList("high"), this.changes);
	}

	@Test
	public void jumpsToAnIndex() {
		final Switch node = this.open();
		Assert.assertSame(node, node.index(1));
		Assert.assertEquals("medium", node.getState());
		Assert.assertEquals(Arrays.asList("medium"), this.changes);
	}

	@Test
	public void staysInSyncWithItsSignal() {
		final Switch node = this.open();
		final Signal<String> quality = new Signal<>("high");
		Assert.assertSame(node, node.signal(quality));
		Assert.assertEquals("high", node.getState());
		node.state("low");
		Assert.assertEquals("low", quality.get());
		quality.set("medium");
		Assert.assertEquals("medium", node.getState());
		Assert.assertEquals(Arrays.asList("high", "low", "medium"), this.changes);
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Switch node = this.open();
		final Signal<String> quality = new Signal<>("high");
		final Signal<String> shadows = new Signal<>("medium");
		node.signal(quality).signal(shadows);
		node.state("low");
		Assert.assertEquals("low", shadows.get());
		Assert.assertEquals("high", quality.get());
		quality.set("medium");
		Assert.assertEquals("low", node.getState());
		Assert.assertSame(shadows, node.getSignal());
		Assert.assertTrue(SwitchNodeTest.subscribers(quality).isEmpty());
		Assert.assertEquals(1, SwitchNodeTest.subscribers(shadows).size());
	}

	@Test
	public void refusesAComputedSignal() {
		final Switch node = this.open();
		final Signal<String> quality = new Signal<>("high");
		node.signal(quality);
		try {
			node.signal(quality.map(value -> value));
			Assert.fail("A ComputedSignal cannot be bound in both directions");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("Switch.signal(...) needs a writable signal: a ComputedSignal is read-only, pass it to a setter instead", expected.getMessage());
		}
		quality.set("low");
		Assert.assertEquals("low", node.getState());
		Assert.assertSame(quality, node.getSignal());
	}

	@Test
	public void startsOnAChosenIndex() {
		final Switch node = this.open();
		Assert.assertSame(node, node.states("off", "on").index(1));
		Assert.assertEquals("on", node.getState());
	}

	@Test
	public void startsOnAChosenState() {
		final Switch node = this.open();
		Assert.assertSame(node, node.states("off", "eco", "on").state("eco"));
		Assert.assertEquals("eco", node.getState());
		Assert.assertEquals(1, node.getStateIndex().get().intValue());
	}

	@Test
	public void goesBackToTheFirstOfNewStates() {
		final Switch node = this.open();
		node.index(2).states("off", "on");
		Assert.assertEquals("off", node.getState());
		Assert.assertEquals(Arrays.asList("high", "off"), this.changes);
	}

	@Test
	public void keepsItsStateWhenItRemainsAState() {
		final Switch node = this.open();
		node.state("medium");
		this.changes.clear();
		node.states("medium", "high");
		Assert.assertEquals("medium", node.getState());
		Assert.assertEquals(0, node.getStateIndex().get().intValue());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void keepsItsChildrenWhenItsIndexChanges() {
		final Switch node = this.open();
		final int loads = node.loads;
		node.index(2).state("medium");
		node.states("low", "medium", "high");
		Assert.assertEquals(loads, node.loads);
		Assert.assertEquals(1, node.getChildren().size());
	}

	@Test
	public void rebuildsItsChildrenWhenItsStatesChange() {
		final Switch node = this.open();
		final int loads = node.loads;
		node.states("off", "on");
		Assert.assertEquals(loads + 1, node.loads);
		Assert.assertEquals(1, node.getChildren().size());
		node.getStateList().add("eco");
		Assert.assertEquals(loads + 2, node.loads);
		Assert.assertEquals(1, node.getChildren().size());
	}

	@Test
	public void copiesTheStatesItIsGiven() {
		final Switch node = this.open();
		final List<String> states = new ArrayList<>(Arrays.asList("off", "on"));
		node.states(() -> states);
		states.add("eco");
		Assert.assertEquals(Arrays.asList("off", "on"), node.getStateList().get());
		Assert.assertNotSame(states, node.getStateList().get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesNoState() {
		new Switch().states();
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnIndexOutOfItsStates() {
		new Switch().states("off", "on").index(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAStateOutOfItsStates() {
		new Switch().states("off", "on").state("eco");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesToJumpOutOfItsStates() {
		this.open().index(3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesToJumpToAnUnknownState() {
		this.open().state("eco");
	}

	@Test
	public void takesAnIndexGivenBeforeItsStates() {
		Assert.assertEquals("on", new Switch().index(1).states("off", "on").getState());
	}

	@Test
	public void takesAStateGivenBeforeItsStates() {
		Assert.assertEquals("eco", new Switch().state("eco").states("off", "on", "eco").getState());
	}

	@Test
	public void takesTheStateOfItsSignalWhenItsStatesArrive() {
		final Signal<String> quality = new Signal<>("high");
		Assert.assertEquals("high", new Switch().signal(quality).states("low", "medium", "high").getState());
		Assert.assertEquals("high", quality.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnIndexGivenBeforeItsStatesThatIsOutOfThem() {
		new Switch().index(2).states("off", "on");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAStateGivenBeforeItsStatesThatIsOutOfThem() {
		new Switch().state("eco").states("off", "on");
	}

	@Test
	public void refusesToBeAttachedWithoutStates() {
		try {
			this.bridges.open(new SingleUI(new Switch()));
			Assert.fail("A switch without states cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Switch has no states, call states(...) before attaching it", expected.getMessage());
		}
	}

	@Test
	public void reportsNoChangeForItsStartingState() {
		final Switch node = new Switch().onChange(event -> this.changes.add(event.getValue())).states("off", "on").state("on");
		this.bridges.open(new SingleUI(node));
		Assert.assertEquals("on", node.getState());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void letsItsListenersRefuseASetterButNotAForcedState() {
		final Switch node = this.open();
		node.beforeChange(event -> event.cancel());
		node.state("high");
		node.index(1);
		Assert.assertEquals("low", node.getState());
		node.forceState("high");
		Assert.assertEquals("high", node.getState());
		Assert.assertEquals(Arrays.asList("high"), this.changes);
	}

	@Test
	public void buildsItsChildrenOnceWhenCreated() {
		final SwitchUI ui = new SwitchUI(new ArrayList<>());
		this.bridges.open(ui).frame();
		Assert.assertEquals(1, ui.node.loads);
		Assert.assertEquals(1, ui.node.getChildren().size());
	}

	private Switch open() {
		final SwitchUI ui = new SwitchUI(this.changes);
		this.bridges.open(ui).frame();
		return ui.node;
	}

	private static Set<?> subscribers(final Signal<?> signal) {
		try {
			final Field field = Signal.class.getDeclaredField("eventSet");
			field.setAccessible(true);
			return (Set<?>) field.get(signal);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	@RequiredArgsConstructor
	public static final class SingleUI extends UI {

		private final Switch node;

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
			this.node = new Switch().states("low", "medium", "high").onChange(event -> this.changes.add(event.getValue()));
			super.add(this.node);
		}

	}

	public static final class Switch extends SwitchNode {

		private int loads;

		public Switch() {
			super(100D, 100D, 120D, 40D);
		}

		@Override
		public void init(final @NonNull UI ui) {
			this.loads++;
			RectNode.create(0D, 0D, 10D, 10D).attach(this);
		}

	}

}