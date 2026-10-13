package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeKeyEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void skipsTheNextListenersOnceAKeyIsConsumed() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).handleKeyPressed(event -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleKeyPressed(event -> {
			received.add("child");
			event.consume();
		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).handleKeyPressed(event -> received.add("sibling")).handleKeyPressed(EventPriority.MONITOR, event -> received.add("sibling monitor"));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertEquals(Arrays.asList("child", "sibling monitor"), received);
	}

	@Test
	public void receivesThePressedKeyWhereverTheMouseIs() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleKeyPressed(event -> received.addAll(Arrays.asList(event.getNode(), event.getKey())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1500D, 900D).frames(2);
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().keyPressed(Key.ENTER);
		Assert.assertEquals(Arrays.asList(rect, Key.A, rect, Key.ENTER), received);
	}

	@Test
	public void receivesTheKeyBeforeItsCharacter() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleKeyPressed(event -> received.add(event.getKey())).handleCharTyped(event -> received.add(event.getCodepoint()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().charTyped('a');
		Assert.assertEquals(Arrays.asList(Key.A, (int) 'a'), received);
	}

	@Test
	public void reachesAChildNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleKeyPressed(event -> received.addAll(Arrays.asList(event.getNode(), event.getKey())));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		this.bridges.getUi().keyPressed(Key.B);
		Assert.assertEquals(Arrays.asList(child, Key.B), received);
	}

	@Test
	public void ignoresAKeyOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(rect -> false).handleKeyPressed(event -> received.add(event.getNode()));
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(rect -> false).handleKeyPressed(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(hidden, disabled)).frames(30);
		this.bridges.getUi().keyPressed(Key.A);
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