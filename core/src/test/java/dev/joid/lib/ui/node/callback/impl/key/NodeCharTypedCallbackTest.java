package dev.joid.lib.ui.node.callback.impl.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.NonNull;

public class NodeCharTypedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeCharTypedCallback<RectNode> callback = (node, codepoint) -> received.addAll(Arrays.asList(node, codepoint));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, 'z');
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 'z');
		Assert.assertEquals(Arrays.asList(rect, (int) 'z'), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void hearsAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeCharTypedCallback<RectNode> callback = (node, codepoint) -> received.add(node);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		callback.post(rect, DispatchContext.create(true), 'z');
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void hearsACharacterConsumedByAnotherNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onCharTyped((node, codepoint) -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onCharTyped(new NodeCharTypedCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final int codepoint) {
				received.add("child");
			}

			@Override
			public void post(final @NonNull RectNode node, final @NonNull DispatchContext context, final int codepoint) {
				context.cancel(() -> this.apply(node, codepoint));
			}

		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).onCharTyped((node, codepoint) -> received.add("sibling"));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		Assert.assertTrue(this.bridges.getUi().charTyped('a'));
		Assert.assertEquals(Arrays.asList("child", "parent", "sibling"), received);
	}

	@Test
	public void receivesTheTypedCharacterWhereverTheMouseIs() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onCharTyped((node, codepoint) -> received.addAll(Arrays.asList(node, codepoint)));
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
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onCharTyped((node, codepoint) -> received.add(codepoint));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.getUi().charTyped(0x1F600);
		Assert.assertEquals(Collections.singletonList(0x1F600), received);
	}

	@Test
	public void reachesAChildNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onCharTyped((node, codepoint) -> received.addAll(Arrays.asList(node, codepoint)));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		this.bridges.getUi().charTyped('b');
		Assert.assertEquals(Arrays.asList(child, (int) 'b'), received);
	}

	@Test
	public void ignoresACharacterOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(rect -> false).onCharTyped((node, codepoint) -> received.add(node));
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(rect -> false).onCharTyped((node, codepoint) -> received.add(node));
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