package dev.joid.lib.ui.node.callback.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallbackInvoker;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeEventCallbackTest {

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeEventCallback<RectNode> callback = received::add;
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
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
		final NodeEventCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void runsThroughItsCallbackObject() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		final NodeCallbackInvoker<NodeEventCallback<RectNode>> object = new NodeCallbackInvoker<>(received::add);
		object.pre(rect, context);
		Assert.assertTrue(received.isEmpty());
		object.post(rect, context);
		Assert.assertEquals(Collections.singletonList(rect), received);
		Assert.assertTrue(context.isCancelled());
	}

}