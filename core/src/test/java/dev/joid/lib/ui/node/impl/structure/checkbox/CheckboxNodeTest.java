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
import dev.joid.lib.utils.click.ClickType;

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