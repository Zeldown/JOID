package dev.joid.lib.ui.node.impl.structure.toggle;

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
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;

public class ToggleNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsOnItsBackSide() {
		final Toggle toggle = new Toggle().state("on", 0);
		Assert.assertFalse(toggle.isToggle());
		Assert.assertEquals(0, toggle.<Integer>getValue().intValue());
		Assert.assertEquals("on", toggle.getState().getToggle());
		Assert.assertEquals(0, toggle.getState().getBack().intValue());
	}

	@Test
	public void switchesToAChosenSide() {
		final Toggle toggle = new Toggle().state("on", 0);
		Assert.assertSame(toggle, toggle.toggle(true));
		Assert.assertTrue(toggle.isToggle());
		Assert.assertEquals("on", toggle.getValue());
	}

	@Test
	public void flipsOnEachClick() {
		final List<Boolean> changes = new ArrayList<>();
		final Toggle toggle = new Toggle().state("on", 0).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(toggle)).frame();
		this.click(150D, 120D);
		Assert.assertTrue(toggle.isToggle());
		Assert.assertEquals("on", toggle.getValue());
		this.click(150D, 120D);
		Assert.assertFalse(toggle.isToggle());
		Assert.assertEquals(Arrays.asList(true, false), changes);
	}

	@Test
	public void staysInSyncWithItsSignal() {
		final List<Object> changes = new ArrayList<>();
		final BooleanSignal music = BooleanSignal.of(true);
		final Toggle toggle = new Toggle().state("on", 0).signal(music).onChange(event -> changes.add(event.getValue()));
		Assert.assertTrue(toggle.isToggle());
		Assert.assertSame(music, toggle.getSignal());
		this.bridges.open(new NodeUI(toggle)).frame();
		this.click(110D, 110D);
		Assert.assertFalse(music.get());
		music.set(true);
		Assert.assertTrue(toggle.isToggle());
		Assert.assertEquals(Arrays.asList(false, true), changes);
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final BooleanSignal music = BooleanSignal.of(false);
		final BooleanSignal sounds = BooleanSignal.of(false);
		final Toggle toggle = new Toggle().state("on", 0).signal(music).signal(sounds);
		this.bridges.open(new NodeUI(toggle)).frame();
		toggle.toggle(true);
		Assert.assertTrue(sounds.get());
		Assert.assertFalse(music.get());
		music.set(true);
		music.set(false);
		Assert.assertTrue(toggle.isToggle());
		Assert.assertSame(sounds, toggle.getSignal());
		Assert.assertTrue(ToggleNodeTest.subscribers(music).isEmpty());
		Assert.assertEquals(1, ToggleNodeTest.subscribers(sounds).size());
	}

	@Test
	public void refusesAComputedSignal() {
		final BooleanSignal music = BooleanSignal.of(false);
		final Toggle toggle = new Toggle().state("on", 0).signal(music);
		try {
			toggle.signal(music.map(value -> !value));
			Assert.fail("A ComputedSignal cannot be bound in both directions");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("Toggle.signal(...) needs a writable signal: a ComputedSignal is read-only, pass it to a setter instead", expected.getMessage());
		}
		this.bridges.open(new NodeUI(toggle));
		music.set(true);
		Assert.assertTrue(toggle.isToggle());
		Assert.assertSame(music, toggle.getSignal());
	}

	@Test
	public void writesAChosenSideIntoItsSignal() {
		final List<Object> changes = new ArrayList<>();
		final BooleanSignal music = BooleanSignal.of(false);
		final Toggle toggle = new Toggle().state("on", 0).signal(music).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(toggle));
		toggle.toggle(true);
		toggle.toggle(true);
		Assert.assertTrue(music.get());
		Assert.assertEquals(Arrays.asList(true), changes);
	}

	@Test
	public void letsItsListenersRefuseASetterButNotAForcedSide() {
		final Toggle toggle = new Toggle().state("on", 0).beforeChange(event -> event.cancel());
		this.bridges.open(new NodeUI(toggle));
		toggle.toggle(true);
		Assert.assertFalse(toggle.isToggle());
		toggle.forceToggle(true);
		Assert.assertTrue(toggle.isToggle());
	}

	@Test
	public void readsItsSideAsItsValueWithoutStateWhenItHoldsBooleans() {
		final BooleanToggle toggle = new BooleanToggle();
		Assert.assertFalse(toggle.<Boolean>getValue());
		toggle.toggle(true);
		Assert.assertTrue(toggle.<Boolean>getValue());
	}

	@Test
	public void refusesToReadAValueWithoutState() {
		try {
			new Toggle().getValue();
			Assert.fail("A toggle without state has no value");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Toggle has no state, call state(toggle, back) before getValue()", expected.getMessage());
		}
	}

	@Test
	public void ignoresAClickBesideIt() {
		final Toggle toggle = new Toggle().state("on", 0);
		this.bridges.open(new NodeUI(toggle)).frame();
		this.click(1000D, 120D);
		Assert.assertFalse(toggle.isToggle());
	}

	@Test
	public void leavesAClickToTheToggleAboveIt() {
		final Toggle below = new Toggle().state("on", 0);
		final Toggle above = new Toggle().state("on", 0);
		this.bridges.open(new NodeUI(below, above)).frame();
		this.click(150D, 120D);
		Assert.assertTrue(above.isToggle());
		Assert.assertFalse(below.isToggle());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		public NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

	public static final class Toggle extends ToggleNode<String, Integer> {

		public Toggle() {
			super(100D, 100D, 100D, 40D);
		}

	}

	public static final class BooleanToggle extends ToggleNode<Boolean, Boolean> {

		public BooleanToggle() {
			super(100D, 100D, 100D, 40D);
		}

	}

}