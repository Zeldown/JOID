package dev.joid.lib.ui.node.property.watch;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.signal.Signal;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

public class WatchPropertyTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void leavesTheNodeAsItIs() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).append(RectNode.create(0D, 0D, 5D, 5D));
		WatchProperty.NONE.apply(node);
		Assert.assertEquals(1, node.getChildren().size());
	}

	@Test
	public void detachesEveryChild() {
		final int[] detached = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).append(RectNode.create(0D, 0D, 5D, 5D).onDetach(child -> detached[0]++), RectNode.create(5D, 5D, 5D, 5D));
		WatchProperty.CLEAR_CHILDREN.apply(node);
		Assert.assertTrue(node.getChildren().isEmpty());
		Assert.assertEquals(1, detached[0]);
	}

	@Test
	public void runsTheBodyAgain() {
		final int[] runs = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).body(() -> runs[0]++);
		WatchProperty.BODY.apply(node);
		Assert.assertEquals(2, runs[0]);
	}

	@Test
	public void runsNoBodyWithoutOne() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).append(RectNode.create(0D, 0D, 5D, 5D));
		WatchProperty.BODY.apply(node);
		Assert.assertEquals(1, node.getChildren().size());
	}

	@Test
	public void loadsTheNodeAgain() {
		final int[] loads = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onInit(target -> loads[0]++);
		this.bridges.open(new NodeUI(node)).frame();
		WatchProperty.RELOAD.apply(node);
		Assert.assertEquals(2, loads[0]);
	}

	@Test
	public void rebuildsAWatchingNodeOnEveryChange() {
		final Signal<Integer> count = new Signal<>(1);
		final WatchUI ui = new WatchUI(count);
		this.bridges.open(ui).frame();
		Assert.assertEquals(1, ui.node.getChildren().size());
		count.set(3);
		Assert.assertEquals(3, ui.node.getChildren().size());
	}

	@Test
	public void leavesTheOldChildrenWithoutParentOnARebuild() {
		final Signal<Integer> count = new Signal<>(1);
		final WatchUI ui = new WatchUI(count);
		this.bridges.open(ui).frame();
		final Node old = ui.node.getChildren().ordered().get(0);
		count.set(2);
		this.bridges.frame();
		Assert.assertNull(old.getParent());
		Assert.assertFalse(ui.node.getChildren().contains(old));
		for (final Node child : ui.node.getChildren()) {
			Assert.assertSame(ui.node, child.getParent());
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

	@RequiredArgsConstructor
	public static final class WatchUI extends UI {

		private final Signal<Integer> count;

		private RectNode node;

		@Override
		public void init() {
			this.node = RectNode.create(0D, 0D, 100D, 100D).watch(this.count, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY).body(node -> {
				for (int i = 0; i < this.count.getOrDefault(); i++) {
					RectNode.create(0D, i * 10D, 10D, 10D).attach(node);
				}
			});
			super.add(this.node);
		}

	}

}