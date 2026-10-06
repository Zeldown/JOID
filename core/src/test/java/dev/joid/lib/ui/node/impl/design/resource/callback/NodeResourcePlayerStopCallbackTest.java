package dev.joid.lib.ui.node.impl.design.resource.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class NodeResourcePlayerStopCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeResourcePlayerStopCallback<ResourcePlayerNode> callback = received::add;
		final ResourcePlayerNode player = ResourcePlayerNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(player, context);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(player, context);
		Assert.assertEquals(Collections.singletonList(player), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeResourcePlayerStopCallback<ResourcePlayerNode> callback = received::add;
		callback.post(ResourcePlayerNode.create(0D, 0D, 10D, 10D), InternalContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnStop() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerStopCallbackTest.blink()).onStop(received::add);
		this.bridges.open(new NodeUI(player)).frames(5);
		Assert.assertTrue(received.isEmpty());
		player.stop();
		Assert.assertEquals(Collections.singletonList(player), received);
		this.bridges.frames(20);
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesForALoopingPlaybackStopped() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerStopCallbackTest.blink()).loop(true).onStop(received::add);
		this.bridges.open(new NodeUI(player)).frames(5);
		player.stop();
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesAfterOnEndOnceThePlaybackEnds() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerStopCallbackTest.blink()).onEnd(node -> received.add("end")).onStop(node -> received.add("stop"));
		this.bridges.open(new NodeUI(player)).frames(14);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(2);
		Assert.assertEquals(Arrays.asList("end", "stop"), received);
	}

	@Test
	public void staysQuietOnARestart() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerStopCallbackTest.blink()).onStop(received::add);
		this.bridges.open(new NodeUI(player)).frames(5);
		player.restart();
		this.bridges.frames(5);
		Assert.assertTrue(received.isEmpty());
	}

	private static Resource blink() {
		return ResourceBuilder.create().cache(null).of(NodeResourcePlayerStopCallbackTest.class.getResourceAsStream("/animation/blink.png"));
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