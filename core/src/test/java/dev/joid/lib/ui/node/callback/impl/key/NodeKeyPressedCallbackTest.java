package dev.joid.lib.ui.node.callback.impl.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

public class NodeKeyPressedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeKeyPressedCallback<RectNode> callback = (node, character, key) -> received.addAll(Arrays.asList(node, character, key));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, 'z', Key.Z);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 'z', Key.Z);
		Assert.assertEquals(Arrays.asList(rect, 'z', Key.Z), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeKeyPressedCallback<RectNode> callback = (node, character, key) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 'z', Key.Z);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesTheTypedKeyWhereverTheMouseIs() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onKeyPressed((node, character, key) -> received.addAll(Arrays.asList(node, character, key)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1500D, 900D).frames(2);
		this.bridges.getUi().keyTyped('a', Key.A);
		this.bridges.getUi().keyTyped('\r', Key.ENTER);
		Assert.assertEquals(Arrays.asList(rect, 'a', Key.A, rect, '\0', Key.ENTER), received);
	}

	@Test
	public void reachesAChildNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onKeyPressed((node, character, key) -> received.addAll(Arrays.asList(node, character)));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		this.bridges.getUi().keyTyped('b', Key.B);
		Assert.assertEquals(Arrays.asList(child, 'b'), received);
	}

	@Test
	public void ignoresAKeyOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(rect -> false).onKeyPressed((node, character, key) -> received.add(node));
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(rect -> false).onKeyPressed((node, character, key) -> received.add(node));
		this.bridges.open(new NodeUI(hidden, disabled)).frames(30);
		this.bridges.getUi().keyTyped('a', Key.A);
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