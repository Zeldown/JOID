package dev.joid.lib.ui.node.impl.design.resource;

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

public class ResourcePlayerNodeEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnceWhenThePlaybackStarts() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onPlay(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(5);
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void waitsForPlayWithoutAutoplay() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).autoplay(false).onPlay(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(5);
		Assert.assertTrue(received.isEmpty());
		player.play();
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesAgainWhenThePlaybackResumes() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onPlay(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(2);
		player.pause();
		this.bridges.frames(2);
		player.resume();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(player, player), received);
	}

	@Test
	public void firesAsSoonAsTheNodePauses() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onPause(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(2);
		Assert.assertTrue(received.isEmpty());
		player.pause();
		Assert.assertEquals(Collections.singletonList(player), received);
		this.bridges.frames(5);
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesNothingWhenNothingPlays() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).autoplay(false).onPause(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(2);
		Assert.assertFalse(player.isPlaying());
		player.pause();
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(player.isPaused());
		player.play();
		this.bridges.frames(2);
		player.pause();
		player.pause();
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesNothingWithoutPlayback() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).onPause(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player));
		player.pause();
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnStop() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onStop(event -> received.add(event.getNode()));
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
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).loop(true).onStop(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(5);
		player.stop();
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void firesAfterOnEndOnceThePlaybackEnds() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onEnd(event -> received.add("end")).onStop(event -> received.add("stop"));
		this.bridges.open(new NodeUI(player)).frames(14);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(2);
		Assert.assertEquals(Arrays.asList("end", "stop"), received);
	}

	@Test
	public void staysQuietOnARestart() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onStop(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(5);
		player.restart();
		this.bridges.frames(5);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceThePlaybackEnds() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(14);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(2);
		Assert.assertEquals(Collections.singletonList(player), received);
		this.bridges.frames(20);
		Assert.assertEquals(Collections.singletonList(player), received);
	}

	@Test
	public void neverFiresWhileLooping() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).loop(true).onEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(60);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void staysQuietWhilePaused() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(2);
		player.pause();
		this.bridges.frames(40);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void neverFiresOnStop() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(5);
		player.stop();
		this.bridges.frames(20);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void neverFiresForALoopingPlaybackStopped() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).loop(true).onEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(5);
		player.stop();
		this.bridges.frames(5);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void reportsTheProgressAndTheTimeOfEachFrame() {
		final List<Double> progresses = new ArrayList<>();
		final List<Double> times = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onProgress(event -> {
			progresses.add(event.getProgress());
			times.add(event.getCurrentTime());
		});
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertEquals(3, progresses.size());
		Assert.assertEquals(0.2D, progresses.get(2), 1E-9D);
		Assert.assertEquals(0.048D, times.get(2), 1E-9D);
		Assert.assertTrue(progresses.get(0) < progresses.get(1));
	}

	@Test
	public void staysQuietWhilePausedProgress() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onProgress(event -> received.add(event.getProgress()));
		this.bridges.open(new NodeUI(player)).frames(2);
		player.pause();
		this.bridges.frames(10);
		Assert.assertEquals(2, received.size());
	}

	@Test
	public void reportsASeekWhilePaused() {
		final List<Double> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeEventTest.blink()).onProgress(event -> received.add(event.getProgress()));
		this.bridges.open(new NodeUI(player)).frames(2);
		player.pause().seek(0.18D);
		this.bridges.frames(10);
		Assert.assertEquals(3, received.size());
		Assert.assertEquals(0.75D, received.get(2), 1E-9D);
	}

	private static Resource blink() {
		return ResourceBuilder.create().cache(null).of(ResourcePlayerNodeEventTest.class.getResourceAsStream("/animation/blink.png")).blocking();
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