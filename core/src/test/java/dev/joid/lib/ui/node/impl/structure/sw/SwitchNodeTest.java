package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
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
		Assert.assertEquals(0, node.getStateIndex().getOrDefault().intValue());
		Assert.assertEquals(Arrays.asList("low", "medium", "high"), node.getStateList().getOrDefault());
	}

	@Test
	public void jumpsToANamedState() {
		final Switch node = this.open();
		Assert.assertSame(node, node.index("high"));
		Assert.assertEquals("high", node.getState());
		Assert.assertEquals(2, node.getStateIndex().getOrDefault().intValue());
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
	public void startsOnAChosenIndex() {
		final Switch node = this.open();
		Assert.assertSame(node, node.state(Arrays.asList("off", "on"), 1));
		Assert.assertEquals("on", node.getState());
	}

	@Test
	public void startsOnAChosenState() {
		final Switch node = this.open();
		Assert.assertSame(node, node.state(Arrays.asList("off", "eco", "on"), "eco"));
		Assert.assertEquals("eco", node.getState());
		Assert.assertEquals(1, node.getStateIndex().getOrDefault().intValue());
	}

	@Test
	public void goesBackToTheFirstOfNewStates() {
		final Switch node = this.open();
		node.index(2).state("off", "on");
		Assert.assertEquals("off", node.getState());
	}

	@Test
	public void rebuildsItsChildrenOnEveryChange() {
		final Switch node = this.open();
		final int loads = node.loads;
		node.index(2);
		Assert.assertEquals(loads + 1, node.loads);
		Assert.assertEquals(1, node.getChildren().size());
		node.state("off", "on");
		Assert.assertTrue(node.loads > loads + 1);
		Assert.assertEquals(1, node.getChildren().size());
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

	@RequiredArgsConstructor
	public static final class SwitchUI extends UI {

		private final List<String> changes;

		private Switch node;

		@Override
		public void init() {
			this.node = new Switch().state("low", "medium", "high").onChange((node, value) -> this.changes.add(value));
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