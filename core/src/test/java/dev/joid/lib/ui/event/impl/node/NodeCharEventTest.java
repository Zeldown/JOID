package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeCharEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void skipsTheNextListenersOnceACharacterIsConsumed() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).handleCharTyped(event -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleCharTyped(event -> {
			received.add("child");
			event.consume();
		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).handleCharTyped(event -> received.add("sibling")).handleCharTyped(EventPriority.MONITOR, event -> received.add("sibling monitor"));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		Assert.assertTrue(this.bridges.getUi().charTyped('a'));
		Assert.assertEquals(Arrays.asList("child", "sibling monitor"), received);
	}

	@Test
	public void receivesTheTypedCharacterWhereverTheMouseIs() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleCharTyped(event -> received.addAll(Arrays.asList(event.getNode(), event.getCodepoint())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1500D, 900D).frames(2);
		this.bridges.getUi().charTyped('a');
		this.bridges.getUi().charTyped('é');
		Assert.assertEquals(Arrays.asList(rect, (int) 'a', rect, (int) 'é'), received);
		Assert.assertEquals('é', rect.getLastCodepoint());
	}

	@Test
	public void receivesACharacterOutsideTheBasicPlaneInOneCall() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleCharTyped(event -> received.add(event.getCodepoint()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.getUi().charTyped(0x1F600);
		Assert.assertEquals(Collections.singletonList(0x1F600), received);
	}

	@Test
	public void reachesAChildNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleCharTyped(event -> received.addAll(Arrays.asList(event.getNode(), event.getCodepoint())));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		this.bridges.getUi().charTyped('b');
		Assert.assertEquals(Arrays.asList(child, (int) 'b'), received);
	}

	@Test
	public void ignoresACharacterOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(rect -> false).handleCharTyped(event -> received.add(event.getNode()));
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(rect -> false).handleCharTyped(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(hidden, disabled)).frames(30);
		this.bridges.getUi().charTyped('a');
		Assert.assertTrue(received.isEmpty());
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}