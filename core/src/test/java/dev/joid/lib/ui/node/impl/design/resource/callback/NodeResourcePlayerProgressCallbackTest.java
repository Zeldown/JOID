package dev.joid.lib.ui.node.impl.design.resource.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.utils.context.InternalContext;

public class NodeResourcePlayerProgressCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeResourcePlayerProgressCallback<ResourcePlayerNode> callback = (node, progress, currentTime) -> received.addAll(Arrays.asList(node, progress, currentTime));
		final ResourcePlayerNode player = ResourcePlayerNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(player, context, 0.5D, 0.12D);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(player, context, 0.5D, 0.12D);
		Assert.assertEquals(Arrays.asList(player, 0.5D, 0.12D), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeResourcePlayerProgressCallback<ResourcePlayerNode> callback = (node, progress, currentTime) -> received.add(node);
		callback.post(ResourcePlayerNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 0.5D, 0.12D);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void reportsTheProgressAndTheTimeOfEachFrame() {
		final List<Double> progresses = new ArrayList<>();
		final List<Double> times = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerProgressCallbackTest.blink()).onProgress((node, progress, currentTime) -> {
			progresses.add(progress);
			times.add(currentTime);
		});
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertEquals(3, progresses.size());
		Assert.assertEquals(0.2D, progresses.get(2), 1E-9D);
		Assert.assertEquals(0.048D, times.get(2), 1E-9D);
		Assert.assertTrue(progresses.get(0) < progresses.get(1));
	}

	@Test
	public void staysQuietWhilePaused() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerProgressCallbackTest.blink()).onProgress((node, progress, currentTime) -> received.add(progress));
		this.bridges.open(new NodeUI(player)).frames(2);
		player.pause();
		this.bridges.frames(10);
		Assert.assertEquals(2, received.size());
	}

	private static Resource blink() {
		return ResourceBuilder.create().cache(null).of(NodeResourcePlayerProgressCallbackTest.class.getResourceAsStream("/animation/blink.png"));
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