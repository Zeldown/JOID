package dev.joid.lib.ui.node.callback.impl.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.NonNull;

public class NodeKeyPressedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeKeyPressedCallback<RectNode> callback = (node, key) -> received.addAll(Arrays.asList(node, key));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, Key.Z);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, Key.Z);
		Assert.assertEquals(Arrays.asList(rect, Key.Z), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void hearsAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeKeyPressedCallback<RectNode> callback = (node, key) -> received.add(node);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		callback.post(rect, DispatchContext.create(true), Key.Z);
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void hearsAKeyConsumedByAnotherNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onKeyPressed((node, key) -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onKeyPressed(new NodeKeyPressedCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final @NonNull Key key) {
				received.add("child");
			}

			@Override
			public void post(final @NonNull RectNode node, final @NonNull DispatchContext context, final @NonNull Key key) {
				context.cancel(() -> this.apply(node, key));
			}

		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).onKeyPressed((node, key) -> received.add("sibling"));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertEquals(Arrays.asList("child", "parent", "sibling"), received);
	}

	@Test
	public void receivesThePressedKeyWhereverTheMouseIs() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onKeyPressed((node, key) -> received.addAll(Arrays.asList(node, key)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(1500D, 900D).frames(2);
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().keyPressed(Key.ENTER);
		Assert.assertEquals(Arrays.asList(rect, Key.A, rect, Key.ENTER), received);
	}

	@Test
	public void receivesTheKeyBeforeItsCharacter() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onKeyPressed((node, key) -> received.add(key)).onCharTyped((node, codepoint) -> received.add(codepoint));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().charTyped('a');
		Assert.assertEquals(Arrays.asList(Key.A, (int) 'a'), received);
	}

	@Test
	public void reachesAChildNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onKeyPressed((node, key) -> received.addAll(Arrays.asList(node, key)));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		this.bridges.getUi().keyPressed(Key.B);
		Assert.assertEquals(Arrays.asList(child, Key.B), received);
	}

	@Test
	public void ignoresAKeyOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(rect -> false).onKeyPressed((node, key) -> received.add(node));
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(rect -> false).onKeyPressed((node, key) -> received.add(node));
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