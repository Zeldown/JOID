package dev.joid.lib.ui.node.impl.structure.toggle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.mouse.MouseButton;
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
		final Toggle toggle = new Toggle().state("on", 0).onChange((node, value) -> changes.add(value));
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
		final BooleanSignal music = new BooleanSignal(true);
		final Toggle toggle = new Toggle().state("on", 0).signal(music).onChange((node, value) -> changes.add(value));
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
		final BooleanSignal music = new BooleanSignal(false);
		final BooleanSignal sounds = new BooleanSignal(false);
		final Toggle toggle = new Toggle().state("on", 0).signal(music).signal(sounds);
		this.bridges.open(new NodeUI(toggle)).frame();
		toggle.toggle(true);
		Assert.assertTrue(sounds.get());
		Assert.assertFalse(music.get());
		music.set(true);
		music.set(false);
		Assert.assertTrue(toggle.isToggle());
		Assert.assertSame(sounds, toggle.getSignal());
		Assert.assertTrue(music.getEventSet().isEmpty());
		Assert.assertEquals(1, sounds.getEventSet().size());
	}

	@Test
	public void refusesAComputedSignal() {
		final BooleanSignal music = new BooleanSignal(false);
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
		final BooleanSignal music = new BooleanSignal(false);
		final Toggle toggle = new Toggle().state("on", 0).signal(music).onChange((node, value) -> changes.add(value));
		toggle.toggle(true);
		toggle.toggle(true);
		Assert.assertTrue(music.get());
		Assert.assertEquals(Arrays.asList(true), changes);
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

}