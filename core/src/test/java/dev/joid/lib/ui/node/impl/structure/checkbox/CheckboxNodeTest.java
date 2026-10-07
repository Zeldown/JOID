package dev.joid.lib.ui.node.impl.structure.checkbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;

public class CheckboxNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsUnchecked() {
		Assert.assertFalse(new Checkbox().isChecked());
	}

	@Test
	public void takesAChosenState() {
		final Checkbox checkbox = new Checkbox();
		Assert.assertSame(checkbox, checkbox.checked(true));
		Assert.assertTrue(checkbox.isChecked());
	}

	@Test
	public void flipsOnEachClick() {
		final List<Boolean> changes = new ArrayList<>();
		final Checkbox checkbox = new Checkbox().onChange((node, value) -> changes.add(value));
		this.bridges.open(new NodeUI(checkbox)).frame();
		this.click(110D, 110D);
		Assert.assertTrue(checkbox.isChecked());
		this.click(110D, 110D);
		Assert.assertFalse(checkbox.isChecked());
		Assert.assertEquals(Arrays.asList(true, false), changes);
	}

	@Test
	public void staysInSyncWithItsSignal() {
		final List<Boolean> changes = new ArrayList<>();
		final BooleanSignal subtitles = new BooleanSignal(true);
		final Checkbox checkbox = new Checkbox().signal(subtitles).onChange((node, value) -> changes.add(value));
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertSame(subtitles, checkbox.getSignal());
		this.bridges.open(new NodeUI(checkbox)).frame();
		this.click(110D, 110D);
		Assert.assertFalse(subtitles.getOrDefault());
		subtitles.set(true);
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertEquals(Arrays.asList(false, true), changes);
	}

	@Test
	public void catchesUpWithItsSignalOnceAttachedAgain() {
		final BooleanSignal subtitles = new BooleanSignal(true);
		final Checkbox checkbox = new Checkbox().signal(subtitles);
		final ContainerNode container = ContainerNode.create(0D, 0D, 200D, 200D).append(checkbox);
		this.bridges.open(new NodeUI(container)).frame();
		container.remove(checkbox);
		subtitles.set(false);
		Assert.assertTrue(checkbox.isChecked());
		container.append(checkbox);
		Assert.assertFalse(checkbox.isChecked());
		subtitles.set(true);
		Assert.assertTrue(checkbox.isChecked());
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final BooleanSignal subtitles = new BooleanSignal(false);
		final BooleanSignal captions = new BooleanSignal(false);
		final Checkbox checkbox = new Checkbox().signal(subtitles).signal(captions);
		final ContainerNode container = ContainerNode.create(0D, 0D, 200D, 200D).append(checkbox);
		this.bridges.open(new NodeUI(container)).frame();
		checkbox.checked(true);
		Assert.assertTrue(captions.getOrDefault());
		Assert.assertFalse(subtitles.getOrDefault());
		subtitles.set(true);
		subtitles.set(false);
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertSame(captions, checkbox.getSignal());
		Assert.assertTrue(subtitles.getEventSet().isEmpty());
		Assert.assertEquals(1, captions.getEventSet().size());
		container.remove(checkbox);
		container.append(checkbox);
		Assert.assertTrue(subtitles.getEventSet().isEmpty());
		Assert.assertEquals(1, captions.getEventSet().size());
		captions.set(false);
		Assert.assertFalse(checkbox.isChecked());
	}


	@Test
	public void refusesAComputedSignal() {
		final BooleanSignal subtitles = new BooleanSignal(true);
		final Checkbox checkbox = new Checkbox().signal(subtitles);
		try {
			checkbox.signal(subtitles.map(value -> !value));
			Assert.fail("A ComputedSignal cannot be bound in both directions");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("Checkbox.signal(...) needs a writable signal: a ComputedSignal is read-only, pass it to a setter instead", expected.getMessage());
		}
		subtitles.set(false);
		Assert.assertFalse(checkbox.isChecked());
		Assert.assertSame(subtitles, checkbox.getSignal());
	}
	@Test
	public void writesAChosenStateIntoItsSignal() {
		final List<Boolean> changes = new ArrayList<>();
		final BooleanSignal subtitles = new BooleanSignal(false);
		final Checkbox checkbox = new Checkbox().signal(subtitles).onChange((node, value) -> changes.add(value));
		checkbox.checked(true);
		checkbox.checked(true);
		Assert.assertTrue(subtitles.getOrDefault());
		Assert.assertEquals(Arrays.asList(true), changes);
	}

	@Test
	public void ignoresAClickBesideIt() {
		final Checkbox checkbox = new Checkbox();
		this.bridges.open(new NodeUI(checkbox)).frame();
		this.click(200D, 110D);
		Assert.assertFalse(checkbox.isChecked());
	}

	@Test
	public void leavesAClickToTheCheckboxAboveIt() {
		final Checkbox below = new Checkbox();
		final Checkbox above = new Checkbox();
		this.bridges.open(new NodeUI(below, above)).frame();
		this.click(110D, 110D);
		Assert.assertTrue(above.isChecked());
		Assert.assertFalse(below.isChecked());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
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

	public static final class Checkbox extends CheckboxNode {

		public Checkbox() {
			super(100D, 100D, 24D, 24D);
		}

	}

}