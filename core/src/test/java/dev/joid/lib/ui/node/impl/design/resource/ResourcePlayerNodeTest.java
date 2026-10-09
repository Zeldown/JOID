package dev.joid.lib.ui.node.impl.design.resource;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.utils.signal.Signal;

public class ResourcePlayerNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsWithoutPlayback() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(10D, 20D);
		Assert.assertNull(player.getResource());
		Assert.assertFalse(player.isLoop());
		Assert.assertTrue(player.isAutoplay());
		Assert.assertEquals(1F, player.getVolume(), 0F);
		Assert.assertSame(StretchType.STRETCH, player.getStretchType());
		Assert.assertNull(player.getPlayback());
		Assert.assertNull(player.getVideo());
		Assert.assertEquals(0D, player.getWidth(), 0D);
		Assert.assertEquals(0D, player.getDuration(), 0D);
		Assert.assertEquals(0D, player.getProgress(), 0D);
	}

	@Test
	public void controlsNothingWithoutResource() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(10D, 20D, 30D, 40D);
		Assert.assertSame(player, player.play().pause().resume().seek(1D).seekTo(1D).restart().stop());
		Assert.assertSame(player, player.location(new Vector3f(1F, 2F, 3F)).referenceDistance(2F).maxDistance(9F));
		Assert.assertFalse(player.isPlaying());
		Assert.assertFalse(player.isPaused());
	}

	@Test
	public void drawsASkeletonWithoutResource() {
		this.bridges.open(new NodeUI(ResourcePlayerNode.create(300D, 300D, 50D, 40D)));
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
	}

	@Test
	public void drawsASkeletonForAnEmptyResource() {
		final Resource empty = ResourceBuilder.create().cache(null).of(new RecordingTexture().allocate(0, 0));
		this.bridges.open(new NodeUI(ResourcePlayerNode.create(300D, 300D, 50D, 40D).resource(empty)));
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
		Assert.assertTrue(this.bridges.getRender().getDraws(1F, 1F, 1F).isEmpty());
	}

	@Test
	public void takesTheSizeOfItsResource() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player));
		Assert.assertEquals(8D, player.getWidth(), 0D);
		Assert.assertEquals(8D, player.getHeight(), 0D);
		Assert.assertTrue(this.bridges.getRender().getDraws(1F, 1F, 1F).isEmpty());
		this.bridges.frame();
		this.assertBounds(this.white(), 100D, 100D, 108D, 108D);
	}

	@Test
	public void derivesItsHeightFromTheRatioOfItsResource() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 32D, 0D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player)).frame();
		Assert.assertEquals(32D, player.getWidth(), 0D);
		Assert.assertEquals(16D, player.getHeight(), 0D);
		this.assertBounds(this.white(), 100D, 100D, 132D, 116D);
	}

	@Test
	public void derivesItsWidthFromTheRatioOfItsResource() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 0D, 16D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player)).frame();
		Assert.assertEquals(32D, player.getWidth(), 0D);
		Assert.assertEquals(16D, player.getHeight(), 0D);
		this.assertBounds(this.white(), 100D, 100D, 132D, 116D);
	}

	@Test
	public void stretchesItsResourceOverItsBounds() {
		this.bridges.open(new NodeUI(ResourcePlayerNode.create(100D, 100D, 80D, 40D).resource(ResourcePlayerNodeTest.blink())));
		this.assertBounds(this.white(), 100D, 100D, 180D, 140D);
	}

	@Test
	public void containsItsResourceInItsBounds() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 40D, 80D).resource(ResourcePlayerNodeTest.blink());
		Assert.assertSame(player, player.stretch(StretchType.CONTAIN));
		this.bridges.open(new NodeUI(player));
		Assert.assertSame(StretchType.CONTAIN, player.getStretchType());
		this.assertBounds(this.white(), 100D, 120D, 140D, 160D);
	}

	@Test
	public void cropsItsResourceToItsBounds() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 40D, 80D).resource(ResourcePlayerNodeTest.blink()).stretch(StretchType.COVER);
		this.bridges.open(new NodeUI(player));
		this.assertBounds(this.white(), 100D, 100D, 140D, 180D);
	}

	@Test
	public void startsItsPlaybackOnceDrawn() {
		final Resource resource = ResourcePlayerNodeTest.blink();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(resource);
		Assert.assertSame(resource, player.getResource());
		Assert.assertSame(resource.getDecoder(), player.getPlayback());
		Assert.assertNull(player.getVideo());
		this.bridges.open(new NodeUI(player));
		Assert.assertTrue(player.isPlaying());
		Assert.assertTrue(player.isResourceStarted());
		Assert.assertEquals(0.24D, player.getDuration(), 1E-9D);
		this.bridges.frames(3);
		Assert.assertEquals(0.2D, player.getProgress(), 1E-9D);
	}

	@Test
	public void waitsForPlayWithoutAutoplay() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		Assert.assertSame(player, player.autoplay(false));
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertFalse(player.isAutoplay());
		Assert.assertFalse(player.isPlaying());
		Assert.assertEquals(0D, player.getProgress(), 0D);
		player.play();
		this.bridges.frames(3);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0.2D, player.getProgress(), 1E-9D);
	}

	@Test
	public void playsOnceWithoutLoop() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player)).frames(20);
		Assert.assertFalse(player.isPlaying());
		Assert.assertFalse(player.isPaused());
	}

	@Test
	public void loopsWhenAsked() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		Assert.assertSame(player, player.loop(true));
		Assert.assertTrue(player.getPlayback().isLoop());
		this.bridges.open(new NodeUI(player)).frames(40);
		Assert.assertTrue(player.isLoop());
		Assert.assertTrue(player.isPlaying());
	}

	@Test
	public void pausesAndResumesItsPlayback() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertSame(player, player.pause());
		this.bridges.frames(20);
		Assert.assertTrue(player.isPaused());
		Assert.assertFalse(player.isPlaying());
		Assert.assertEquals(0.2D, player.getProgress(), 1E-9D);
		Assert.assertSame(player, player.resume());
		this.bridges.frames(3);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0.4D, player.getProgress(), 1E-9D);
	}

	@Test
	public void seeksItsPlayback() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player));
		Assert.assertSame(player, player.seek(0.12D));
		Assert.assertEquals(0.5D, player.getProgress(), 1E-9D);
		Assert.assertSame(player, player.seekTo(0.06D));
		Assert.assertEquals(0.25D, player.getProgress(), 1E-9D);
	}

	@Test
	public void showsTheFrameOfASeekWhilePaused() throws InterruptedException {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		this.settle(player, 6);
		player.pause().seekTo(0.5D);
		this.settle(player, 10);
		Assert.assertTrue(player.isPaused());
		Assert.assertEquals(0.5D, player.getProgress(), 1E-9D);
		Assert.assertEquals(120, ((RecordingTexture) player.getResource().getResourceData().getTextures()[0]).getPixels()[0] & 0xFF, 3);
		player.resume();
		this.settle(player, 7);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0.6D, player.getProgress(), 1E-9D);
	}

	@Test
	public void showsTheFrameOfASeekWhilePlaying() throws InterruptedException {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		this.settle(player, 6);
		player.seekTo(0.5D);
		this.settle(player, 1);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0.5D, player.getProgress(), 1E-9D);
		Assert.assertEquals(120, ((RecordingTexture) player.getResource().getResourceData().getTextures()[0]).getPixels()[0] & 0xFF, 3);
	}

	@Test
	public void stopsItsPlayback() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertSame(player, player.stop());
		this.bridges.frames(3);
		Assert.assertFalse(player.isPlaying());
		Assert.assertEquals(0.2D, player.getProgress(), 1E-9D);
	}

	@Test
	public void restartsItsPlaybackFromTheBeginning() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).onPlay(received::add);
		this.bridges.open(new NodeUI(player)).frames(20);
		Assert.assertSame(player, player.restart());
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0D, player.getProgress(), 0D);
		this.bridges.frame();
		Assert.assertEquals(2, received.size());
	}

	@Test
	public void firesOnPlayOnARestartWhilePlaying() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).onPlay(received::add);
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertEquals(1, received.size());
		player.restart();
		Assert.assertEquals(2, received.size());
		this.bridges.frame();
		Assert.assertEquals(2, received.size());
	}

	@Test
	public void resumesAPausedPlaybackOnPlay() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player)).frames(3);
		player.pause();
		this.bridges.frames(20);
		player.play();
		this.bridges.frames(3);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0.4D, player.getProgress(), 1E-9D);
	}

	@Test
	public void startsANewResourceAgain() {
		final List<Object> received = new ArrayList<>();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).onPlay(received::add);
		this.bridges.open(new NodeUI(player)).frames(20);
		final Resource next = ResourcePlayerNodeTest.blink();
		Assert.assertSame(player, player.resource(next));
		Assert.assertFalse(player.isResourceStarted());
		this.bridges.frame();
		Assert.assertSame(next, player.getResource());
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(2, received.size());
	}

	@Test
	public void appliesItsVolumeToItsVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).volume(0.25F).resource(ResourcePlayerNodeTest.video());
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertEquals(1F, video.getVolume(), 0F);
		this.bridges.open(new NodeUI(player));
		Assert.assertEquals(0.25F, player.getVolume(), 0F);
		Assert.assertEquals(0.25F, video.getVolume(), 0F);
		Assert.assertSame(player, player.volume(0.5F));
		Assert.assertEquals(0.5F, video.getVolume(), 0F);
	}

	@Test
	public void givesItsAudioGroupToItsVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).audioGroup("music").resource(ResourcePlayerNodeTest.video());
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertNull(video.getAudioGroup());
		this.bridges.open(new NodeUI(player));
		Assert.assertEquals("music", player.getAudioGroup());
		Assert.assertEquals("music", video.getAudioGroup());
		final Signal<String> group = Signal.of("voice");
		Assert.assertSame(player, player.audioGroup(group));
		Assert.assertEquals("voice", video.getAudioGroup());
		group.set("ambient");
		this.bridges.frame();
		Assert.assertEquals("ambient", video.getAudioGroup());
	}

	@Test
	public void placesTheAudioOfItsVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertSame(player, player.location(new Vector3f(1F, 2F, 3F)).referenceDistance(2F).maxDistance(9F));
		Assert.assertEquals(1F, video.getLocationX(), 0F);
		Assert.assertEquals(2F, video.getLocationY(), 0F);
		Assert.assertEquals(3F, video.getLocationZ(), 0F);
		Assert.assertEquals(2F, video.getReferenceDistance(), 0F);
		Assert.assertEquals(9F, video.getMaxDistance(), 0F);
	}

	@Test
	public void playsAVideoOnceDrawn() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player)).frame();
		Assert.assertEquals(16D, player.getWidth(), 0D);
		Assert.assertEquals(8D, player.getHeight(), 0D);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(1D, player.getDuration(), 0D);
		this.assertBounds(this.white(), 100D, 100D, 116D, 108D);
	}

	@Test
	public void releasesItsVideoOnceDetached() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		final NodeUI ui = new NodeUI(player);
		this.bridges.open(ui).frames(2);
		ui.properlyClose();
		Assert.assertTrue(player.getVideo().isReleased());
		Assert.assertFalse(player.isPlaying());
	}

	@Test
	public void startsItsPlaybackAgainOnceAttachedAgain() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		final NodeUI ui = new NodeUI(player);
		this.bridges.open(ui).frames(3);
		final double progress = player.getProgress();
		player.onDetach();
		Assert.assertFalse(player.isResourceStarted());
		Assert.assertFalse(player.isWasPlaying());
		player.load(ui);
		this.bridges.frame();
		Assert.assertTrue(player.isResourceStarted());
		Assert.assertTrue(player.isPlaying());
		Assert.assertTrue(player.getProgress() < progress);
	}

	@Test
	public void releasesItsPreviousVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		final VideoResourceDecoder previous = player.getVideo();
		this.bridges.open(new NodeUI(player)).frames(2);
		player.resource(ResourcePlayerNodeTest.blink());
		Assert.assertTrue(previous.isReleased());
		Assert.assertNull(player.getVideo());
	}

	@Test
	public void playsAnAnimationThroughItsDecoder() {
		final Resource resource = ResourcePlayerNodeTest.blink();
		Assert.assertTrue(resource.getDecoder() instanceof AnimatedResourceDecoder);
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(resource);
		this.bridges.open(new NodeUI(player)).frames(2);
		Assert.assertEquals(0, ((AnimatedResourceDecoder) resource.getDecoder()).getDisplayed());
		this.bridges.frame();
		Assert.assertEquals(1, ((AnimatedResourceDecoder) resource.getDecoder()).getDisplayed());
	}

	@Test
	public void placesTheAudioOfAVideoGivenAfterwards() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).location(new Vector3f(1F, 2F, 3F)).referenceDistance(2F).maxDistance(9F).resource(ResourceBuilder.create().cache(null).of(ResourcePlayerNodeTest.class.getResourceAsStream("/dev/joid/lib/resource/dto/decoder/impl/frames.mkv")));
		this.bridges.open(new NodeUI(player));
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertTrue(video.isHasLocation());
		Assert.assertEquals(1F, video.getLocationX(), 0F);
		Assert.assertEquals(2F, video.getReferenceDistance(), 0F);
		Assert.assertEquals(9F, video.getMaxDistance(), 0F);
	}

	private void settle(final ResourcePlayerNode player, final int frames) throws InterruptedException {
		for (int i = 0; i < frames; i++) {
			final long deadline = System.currentTimeMillis() + 5000L;
			while (!player.getVideo().isSettled() && System.currentTimeMillis() < deadline) {
				Thread.sleep(1L);
			}
			this.bridges.frame();
		}
	}

	private Draw skeleton() {
		final Color loading = Color.LOADING();
		final List<Draw> draws = this.bridges.getRender().getDraws(loading.r, loading.g, loading.b);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private Draw white() {
		final List<Draw> draws = this.bridges.getRender().getDraws(1F, 1F, 1F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private void assertBounds(final Draw draw, final double left, final double top, final double right, final double bottom) {
		Assert.assertEquals(left, draw.getLeft(), 1E-3D);
		Assert.assertEquals(top, draw.getTop(), 1E-3D);
		Assert.assertEquals(right, draw.getRight(), 1E-3D);
		Assert.assertEquals(bottom, draw.getBottom(), 1E-3D);
	}

	private static Resource blink() {
		return ResourceBuilder.create().cache(null).of(ResourcePlayerNodeTest.class.getResourceAsStream("/animation/blink.png"));
	}

	private static Resource video() {
		return ResourceBuilder.create().cache(null).of(ResourcePlayerNodeTest.class.getResourceAsStream("/dev/joid/lib/resource/dto/decoder/impl/frames.mkv"));
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