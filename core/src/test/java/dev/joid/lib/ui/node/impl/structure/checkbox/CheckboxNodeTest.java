package dev.joid.lib.ui.node.impl.structure.checkbox;

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
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;

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
		final Checkbox checkbox = new Checkbox().onChange(event -> changes.add(event.getValue()));
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
		final Checkbox checkbox = new Checkbox().checked(subtitles).onChange(event -> changes.add(event.getValue()));
		Assert.assertTrue(checkbox.isChecked());
		this.bridges.open(new NodeUI(checkbox)).frame();
		this.click(110D, 110D);
		Assert.assertFalse(subtitles.get());
		subtitles.set(true);
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertEquals(Arrays.asList(false, true), changes);
	}

	@Test
	public void catchesUpWithItsSignalOnceAttachedAgain() {
		final BooleanSignal subtitles = new BooleanSignal(true);
		final Checkbox checkbox = new Checkbox().checked(subtitles);
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
		final Checkbox checkbox = new Checkbox().checked(subtitles).checked(captions);
		final ContainerNode container = ContainerNode.create(0D, 0D, 200D, 200D).append(checkbox);
		this.bridges.open(new NodeUI(container)).frame();
		this.click(110D, 110D);
		Assert.assertTrue(captions.get());
		Assert.assertFalse(subtitles.get());
		subtitles.set(true);
		subtitles.set(false);
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertTrue(CheckboxNodeTest.subscribers(subtitles).isEmpty());
		Assert.assertEquals(1, CheckboxNodeTest.subscribers(captions).size());
		container.remove(checkbox);
		container.append(checkbox);
		Assert.assertTrue(CheckboxNodeTest.subscribers(subtitles).isEmpty());
		Assert.assertEquals(1, CheckboxNodeTest.subscribers(captions).size());
		captions.set(false);
		Assert.assertFalse(checkbox.isChecked());
	}

	@Test
	public void refusesASignalItCannotWrite() {
		final BooleanSignal subtitles = new BooleanSignal(true);
		try {
			new Checkbox().checked(subtitles.map(value -> !value));
			Assert.fail("A computed signal cannot drive a value the user changes");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The checked of Checkbox is changed by the user, so it cannot follow ComputedSignal{false}, which cannot be written: pass a writable Signal, or its value as a start value", expected.getMessage());
		}
	}

	@Test
	public void replacesItsSignalWithAnySetter() {
		final List<Boolean> changes = new ArrayList<>();
		final BooleanSignal subtitles = new BooleanSignal(false);
		final Checkbox checkbox = new Checkbox().checked(subtitles).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(checkbox));
		checkbox.checked(true);
		checkbox.checked(true);
		Assert.assertFalse(subtitles.get());
		subtitles.set(true);
		subtitles.set(false);
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertTrue(CheckboxNodeTest.subscribers(subtitles).isEmpty());
		checkbox.checked(subtitles).checked(true);
		Assert.assertFalse(subtitles.get());
		checkbox.checked(subtitles).checked(true);
		Assert.assertTrue(CheckboxNodeTest.subscribers(subtitles).isEmpty());
		Assert.assertEquals(Arrays.asList(true, false, true, false, true), changes);
	}

	@Test
	public void reportsNoChangeForItsStartingState() {
		final List<Boolean> changes = new ArrayList<>();
		final Checkbox checkbox = new Checkbox().onChange(event -> changes.add(event.getValue())).checked(true).beforeChange(event -> event.cancel());
		this.bridges.open(new NodeUI(checkbox));
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void letsItsListenersRefuseOnlyTheUser() {
		final List<Boolean> changes = new ArrayList<>();
		final BooleanSignal subtitles = new BooleanSignal(false);
		final Checkbox checkbox = new Checkbox().<Checkbox>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(checkbox)).frame();
		checkbox.checked(true);
		Assert.assertTrue(checkbox.isChecked());
		this.click(110D, 110D);
		Assert.assertTrue(checkbox.isChecked());
		checkbox.checked(subtitles);
		Assert.assertFalse(checkbox.isChecked());
		Assert.assertEquals(Arrays.asList(true, false), changes);
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
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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