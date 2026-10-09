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
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;

public class NodeResourcePlayerPlayCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeResourcePlayerPlayCallback<ResourcePlayerNode> callback = received::add;
		final ResourcePlayerNode player = ResourcePlayerNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
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
		final NodeResourcePlayerPlayCallback<ResourcePlayerNode> callback = received::add;
		callback.post(ResourcePlayerNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceWhenThePlaybackStarts() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerPlayCallbackTest.blink()).onPlay(received::add);
		this.bridges.open(new NodeUI(player)).frames(5);
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void waitsForPlayWithoutAutoplay() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerPlayCallbackTest.blink()).autoplay(false).onPlay(received::add);
		this.bridges.open(new NodeUI(player)).frames(5);
		Assert.assertTrue(received.isEmpty());
		player.play();
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesAgainWhenThePlaybackResumes() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(NodeResourcePlayerPlayCallbackTest.blink()).onPlay(received::add);
		this.bridges.open(new NodeUI(player)).frames(2);
		player.pause();
		this.bridges.frames(2);
		player.resume();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(player, player), received);
	}

	private static Resource blink() {
		return ResourceBuilder.create().cache(null).of(NodeResourcePlayerPlayCallbackTest.class.getResourceAsStream("/animation/blink.png"));
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