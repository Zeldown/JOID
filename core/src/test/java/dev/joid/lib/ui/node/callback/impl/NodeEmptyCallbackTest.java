package dev.joid.lib.ui.node.callback.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.node.callback.NodeCallbackObject;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;

public class NodeEmptyCallbackTest {

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeEmptyCallback<RectNode> callback = received::add;
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context);
		Assert.assertEquals(Collections.singletonList(rect), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeEmptyCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void runsThroughItsCallbackObject() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		final NodeCallbackObject<NodeEmptyCallback<RectNode>> object = new NodeCallbackObject<>(received::add);
		object.pre(rect, context);
		Assert.assertTrue(received.isEmpty());
		object.post(rect, context);
		Assert.assertEquals(Collections.singletonList(rect), received);
		Assert.assertTrue(context.isCancelled());
	}

}