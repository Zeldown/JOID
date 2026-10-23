package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NodeChildEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesForEachAppendedChild() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onChildAppend(event -> received.addAll(Arrays.asList(event.getNode(), event.getChild())));
		final RectNode first = RectNode.create(10D, 10D, 20D, 20D);
		final RectNode second = RectNode.create(40D, 10D, 20D, 20D);
		parent.append(first, second);
		final RectNode third = RectNode.create(70D, 10D, 20D, 20D).attach(parent);
		Assert.assertEquals(Arrays.asList(parent, first, parent, second, parent, third), received);
	}

	@Test
	public void firesOnceTheChildIsAttachedAndLoaded() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onChildAppend(event -> received.addAll(Arrays.asList(event.getChild().getParent() == event.getNode(), event.getNode().getChildren().contains(event.getChild()), event.getChild().isAttached())));
		this.bridges.open(new NodeUI(parent)).frames(30);
		parent.append(RectNode.create(10D, 10D, 20D, 20D));
		Assert.assertEquals(Arrays.asList(true, true, true), received);
	}

}