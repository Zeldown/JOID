package dev.joid.lib.ui.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnceTheNodeIsLoadedInAUi() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onInit(event -> received.addAll(Arrays.asList(event.getNode(), event.getNode().hasUi())));
		Assert.assertTrue(received.isEmpty());
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertEquals(Arrays.asList(rect, true), received);
	}

	@Test
	public void firesForAChildAppendedToALoadedNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		this.bridges.open(new NodeUI(parent)).frames(30);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onInit(event -> received.add(event.getNode()));
		Assert.assertTrue(received.isEmpty());
		parent.append(child);
		Assert.assertEquals(Collections.singletonList(child), received);
	}

	@Test
	public void runsEveryInitCallbackOfANode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onInit(event -> received.add("first")).onInit(event -> received.add("second"));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void firesOnEveryUpdate() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onUpdate(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		received.clear();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(rect, rect, rect), received);
	}

	@Test
	public void firesOnceTheChildrenAreUpdated() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onUpdate(event -> received.add(event.getNode()));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onUpdate(event -> received.add(event.getNode()));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		received.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(child, parent), received);
	}

	@Test
	public void firesWhenTheParentClearsItsChildren() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onDetach(event -> received.add(event.getNode()));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertTrue(received.isEmpty());
		parent.clearChildren();
		Assert.assertEquals(Collections.singletonList(child), received);
	}

	@Test
	public void detachesTheChildrenFirst() {
		final List<Object> received = new ArrayList<>();
		final RectNode root = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode parent = RectNode.create(10D, 10D, 100D, 50D).onDetach(event -> received.add(event.getNode()));
		final RectNode child = RectNode.create(5D, 5D, 20D, 20D).onDetach(event -> received.add(event.getNode()));
		parent.append(child);
		root.append(parent);
		this.bridges.open(new NodeUI(root)).frames(30);
		root.clearChildren();
		Assert.assertEquals(Arrays.asList(child, parent), received);
	}

	@Test
	public void firesWhenTheUiCloses() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onDetach(event -> received.add(event.getNode()));
		final NodeUI ui = new NodeUI(rect);
		this.bridges.open(ui).frames(30);
		ui.dispose();
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void firesOnceOnTheFirstDrawnFrame() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMount(event -> received.add(event.getNode()));
		this.bridges.getUi().add(new NodeUI(rect));
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(30);
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void waitsForTheNodeToBeReady() {
		final List<Object> received = new ArrayList<>();
		final AtomicBoolean ready = new AtomicBoolean();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).wait(node -> ready.get()).onMount(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		ready.set(true);
		this.bridges.frames(3);
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void waitsForItsParentToBeReady() {
		final List<Object> received = new ArrayList<>();
		final AtomicBoolean ready = new AtomicBoolean();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).wait(node -> ready.get());
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onMount(event -> received.add(event.getNode()));
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertTrue(received.isEmpty());
		ready.set(true);
		this.bridges.frames(3);
		Assert.assertEquals(Collections.singletonList(child), received);
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