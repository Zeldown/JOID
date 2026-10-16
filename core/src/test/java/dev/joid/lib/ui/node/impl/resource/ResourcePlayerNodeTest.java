package dev.joid.lib.ui.node.impl.resource;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.utils.geom.Point;

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
		Assert.assertSame(StretchType.STRETCH, player.getStretch());
		Assert.assertNull(player.getPlayback());
		Assert.assertNull(player.getVideo());
		Assert.assertEquals(0D, player.getWidth(), 0D);
		Assert.assertEquals(0D, player.getDuration(), 0D);
		Assert.assertEquals(0D, player.getProgress(), 0D);
	}

	@Test
	public void controlsNothingWithoutResource() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(10D, 20D, 30D, 40D);
		Assert.assertSame(player, player.play().pause().resume().seek(1D).restart().stop());
		Assert.assertSame(player, player.location(Point.create(1D, 2D, 3D)).referenceDistance(2F).maxDistance(9F));
		Assert.assertFalse(player.isPlaying());
		Assert.assertFalse(player.isPaused());
	}

	@Test
	public void refusesToBeAttachedWithoutResource() {
		try {
			this.bridges.open(new NodeUI(ResourcePlayerNode.create(300D, 300D, 50D, 40D)));
			Assert.fail("A player without resource cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The ResourcePlayerNode has no resource, call resource(...) before attaching it", expected.getMessage());
		}
	}

	@Test
	public void drawsASkeletonForAnEmptyResource() {
		final Resource empty = ResourceBuilder.create().cache(null).of(new RecordingTexture().allocate(0, 0));
		this.bridges.open(new NodeUI(ResourcePlayerNode.create(300D, 300D, 50D, 40D).resource(empty)));
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
		Assert.assertTrue(this.bridges.getRender().getDraws(1F, 1F, 1F).isEmpty());
	}

	@Test
	public void showsAFailedResourceWithoutSizeAtTheSizeOfItsMissingTexture() {
		final Resource missing = ResourceBuilder.create().cache(null).of("missing/absent.png").blocking();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D).resource(missing);
		this.bridges.open(new NodeUI(player)).frame();
		Assert.assertEquals(64D, player.getWidth(), 0D);
		Assert.assertEquals(64D, player.getHeight(), 0D);
	}

	@Test
	public void forksItsResourceOnlyOnceAttached() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D).resource(ResourceBuilder.create().cache(null).of(new RecordingTexture().allocate(4, 4)));
		Assert.assertNull(ResourcePlayerNodeTest.read(player, "fork"));
		this.bridges.open(new NodeUI(player));
		Assert.assertNotNull(ResourcePlayerNodeTest.read(player, "fork"));
	}

	@Test
	public void drawsASkeletonForAResourceWithoutDecoderYet() {
		final Resource pending = ResourceBuilder.create().cache(null).compute("pending", () -> new ResourceData("pending", null));
		this.bridges.open(new NodeUI(ResourcePlayerNode.create(300D, 300D, 50D, 40D).resource(pending)));
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
	}

	@Test
	public void takesTheSizeOfItsResource() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player));
		Assert.assertEquals(8D, player.getWidth(), 0D);
		Assert.assertEquals(8D, player.getHeight(), 0D);
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
		Assert.assertSame(StretchType.CONTAIN, player.getStretch());
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
		Assert.assertNull(player.getPlayback());
		this.bridges.open(new NodeUI(player));
		Assert.assertNotSame(resource, ResourcePlayerNodeTest.<Resource>read(player, "fork"));
		Assert.assertNotSame(resource.getDecoder(), player.getPlayback());
		Assert.assertSame(ResourcePlayerNodeTest.<Resource>read(player, "fork").getDecoder(), player.getPlayback());
		Assert.assertNull(player.getVideo());
		Assert.assertTrue(player.isPlaying());
		Assert.assertTrue(ResourcePlayerNodeTest.<Boolean>read(player, "resourceStarted"));
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
		this.bridges.open(new NodeUI(player)).frames(40);
		Assert.assertTrue(player.getPlayback().isLoop());
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
		Assert.assertSame(player, player.seek(0.06D));
		Assert.assertEquals(0.25D, player.getProgress(), 1E-9D);
	}

	@Test
	public void showsTheFrameOfASeekWhilePaused() throws InterruptedException {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		this.settle(player, 6);
		player.pause().seek(0.5D);
		this.settle(player, 10);
		Assert.assertTrue(player.isPaused());
		Assert.assertEquals(0.5D, player.getProgress(), 1E-9D);
		Assert.assertEquals(120, ((RecordingTexture) ResourcePlayerNodeTest.<Resource>read(player, "fork").getResourceData().getTextures()[0]).getPixels()[0] & 0xFF, 3);
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
		player.seek(0.5D);
		this.settle(player, 1);
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(0.5D, player.getProgress(), 1E-9D);
		Assert.assertEquals(120, ((RecordingTexture) ResourcePlayerNodeTest.<Resource>read(player, "fork").getResourceData().getTextures()[0]).getPixels()[0] & 0xFF, 3);
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
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).onPlay(event -> received.add(event.getNode()));
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
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).onPlay(event -> received.add(event.getNode()));
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
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).onPlay(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(player)).frames(20);
		final Resource next = ResourcePlayerNodeTest.blink();
		Assert.assertSame(player, player.resource(next));
		Assert.assertFalse(ResourcePlayerNodeTest.<Boolean>read(player, "resourceStarted"));
		this.bridges.frame();
		Assert.assertSame(next, player.getResource());
		Assert.assertTrue(player.isPlaying());
		Assert.assertEquals(2, received.size());
	}

	@Test
	public void appliesItsVolumeToItsVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).volume(0.25F).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertEquals(0.25F, player.getVolume(), 0F);
		Assert.assertEquals(0.25F, video.getVolume(), 0F);
		Assert.assertSame(player, player.volume(0.5F));
		Assert.assertEquals(0.5F, video.getVolume(), 0F);
	}

	@Test
	public void givesItsAudioGroupToItsVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).audioGroup("music").resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertEquals("music", player.getAudioGroup());
		Assert.assertEquals("music", video.getAudioGroup());
		final Signal<String> group = new Signal<>("voice");
		Assert.assertSame(player, player.audioGroup(group));
		Assert.assertEquals("voice", video.getAudioGroup());
		group.set("ambient");
		this.bridges.frame();
		Assert.assertEquals("ambient", video.getAudioGroup());
	}

	@Test
	public void placesTheAudioOfItsVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertSame(player, player.location(Point.create(1D, 2D, 3D)).referenceDistance(2F).maxDistance(9F));
		Assert.assertEquals(1F, ResourcePlayerNodeTest.<Float>read(video, "locationX"), 0F);
		Assert.assertEquals(2F, ResourcePlayerNodeTest.<Float>read(video, "locationY"), 0F);
		Assert.assertEquals(3F, ResourcePlayerNodeTest.<Float>read(video, "locationZ"), 0F);
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
		final VideoResourceDecoder video = player.getVideo();
		ui.dispose();
		Assert.assertNull(ResourcePlayerNodeTest.read(video, "grabber"));
		Assert.assertFalse(player.isPlaying());
	}

	@Test
	public void freesItsForkOnceDetached() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		this.bridges.open(new NodeUI(player)).frames(2);
		final Resource fork = ResourcePlayerNodeTest.<Resource>read(player, "fork");
		Assert.assertNotNull(fork.getResourceData().getTextures());
		player.fireDetach();
		Assert.assertNull(fork.getResourceData().getTextures());
	}

	@Test
	public void startsItsPlaybackAgainOnceAttachedAgain() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink());
		final NodeUI ui = new NodeUI(player);
		this.bridges.open(ui).frames(3);
		final double progress = player.getProgress();
		player.fireDetach();
		Assert.assertFalse(ResourcePlayerNodeTest.<Boolean>read(player, "resourceStarted"));
		Assert.assertFalse(ResourcePlayerNodeTest.<Boolean>read(player, "wasPlaying"));
		player.load(ui);
		this.bridges.frame();
		Assert.assertTrue(ResourcePlayerNodeTest.<Boolean>read(player, "resourceStarted"));
		Assert.assertTrue(player.isPlaying());
		Assert.assertTrue(player.getProgress() < progress);
	}

	@Test
	public void releasesItsPreviousVideo() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player)).frames(2);
		final VideoResourceDecoder previous = player.getVideo();
		player.resource(ResourcePlayerNodeTest.blink());
		Assert.assertNull(ResourcePlayerNodeTest.read(previous, "grabber"));
		Assert.assertNull(player.getVideo());
	}

	@Test
	public void playsAnAnimationThroughItsDecoder() {
		final Resource resource = ResourcePlayerNodeTest.blink();
		Assert.assertTrue(resource.getDecoder() instanceof AnimatedResourceDecoder);
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(resource);
		this.bridges.open(new NodeUI(player)).frames(2);
		Assert.assertEquals(0, (int) ResourcePlayerNodeTest.<Integer>read(ResourcePlayerNodeTest.<Resource>read(player, "fork").getDecoder(), "displayed"));
		this.bridges.frame();
		Assert.assertEquals(1, (int) ResourcePlayerNodeTest.<Integer>read(ResourcePlayerNodeTest.<Resource>read(player, "fork").getDecoder(), "displayed"));
		Assert.assertEquals(-1, (int) ResourcePlayerNodeTest.<Integer>read(resource.getDecoder(), "displayed"));
	}

	@Test
	public void playsASharedResourceApartFromAnotherPlayer() {
		final Resource resource = ResourcePlayerNodeTest.blink();
		final ResourcePlayerNode first = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(resource);
		final ResourcePlayerNode second = ResourcePlayerNode.create(200D, 100D, 80D, 80D).resource(resource).autoplay(false);
		this.bridges.open(new NodeUI(first, second)).frames(3);
		Assert.assertNotSame(first.getPlayback(), second.getPlayback());
		Assert.assertTrue(first.isPlaying());
		Assert.assertFalse(second.isPlaying());
		Assert.assertEquals(0.2D, first.getProgress(), 1E-9D);
		Assert.assertEquals(0D, second.getProgress(), 0D);
		second.play();
		first.pause();
		this.bridges.frames(3);
		Assert.assertTrue(first.isPaused());
		Assert.assertTrue(second.isPlaying());
		Assert.assertEquals(0.2D, first.getProgress(), 1E-9D);
		Assert.assertEquals(0.2D, second.getProgress(), 1E-9D);
	}

	@Test
	public void playsAStreamedVideoAgainOnceGivenBack() {
		final Resource video = ResourcePlayerNodeTest.video();
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(video);
		this.bridges.open(new NodeUI(player)).frames(2);
		Assert.assertTrue(player.isPlaying());
		player.resource(ResourcePlayerNodeTest.blink());
		this.bridges.frames(2);
		player.resource(video);
		this.bridges.frames(2);
		Assert.assertTrue(player.isPlaying());
		Assert.assertNotNull(ResourcePlayerNodeTest.read(player.getVideo(), "grabber"));
		Assert.assertTrue(ResourcePlayerNodeTest.<File>read(player.getVideo(), "file").isFile());
		Assert.assertFalse(ResourcePlayerNodeTest.<Resource>read(player, "fork").isFailed());
	}

	@Test
	public void keepsTheStateAskedBeforeItsResourceLoads() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).loop(true);
		Assert.assertSame(player, player.pause().seek(0.12D));
		Assert.assertFalse(ResourcePlayerNodeTest.<Boolean>read(player, "resourceStarted"));
		this.bridges.open(new NodeUI(player)).frames(3);
		Assert.assertFalse(player.isPlaying());
		Assert.assertEquals(0.5D, player.getProgress(), 1E-9D);
		final ResourcePlayerNode later = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourcePlayerNodeTest.blink()).autoplay(false).play();
		this.bridges.open(new NodeUI(later)).frames(3);
		Assert.assertTrue(later.isPlaying());
	}

	@Test
	public void refusesToPlayAStillImage() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).resource(ResourceBuilder.create().cache(null).of(new RecordingTexture().allocate(4, 4)));
		this.bridges.open(new NodeUI(player));
		Assert.assertTrue(ResourcePlayerNodeTest.<Boolean>read(player, "resourceStarted"));
		Assert.assertNull(player.getPlayback());
		try {
			player.play();
			Assert.fail("A still image must refuse to play");
		} catch (final IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().endsWith(" of this ResourcePlayerNode has no playback: give it a video or an animated image, or show a still image with a ResourceNode"));
		}
	}

	@Test
	public void placesTheAudioOfAVideoGivenAfterwards() {
		final ResourcePlayerNode player = ResourcePlayerNode.create(100D, 100D, 80D, 80D).location(Point.create(1D, 2D, 3D)).referenceDistance(2F).maxDistance(9F).resource(ResourcePlayerNodeTest.video());
		this.bridges.open(new NodeUI(player));
		final VideoResourceDecoder video = player.getVideo();
		Assert.assertTrue(ResourcePlayerNodeTest.<Boolean>read(video, "hasLocation"));
		Assert.assertEquals(1F, ResourcePlayerNodeTest.<Float>read(video, "locationX"), 0F);
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
		final List<Draw> draws = this.bridges.getRender().getDraws(loading.getRedF(), loading.getGreenF(), loading.getBlueF());
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

	private static <V> V read(final Object target, final String name) {
		try {
			final Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return (V) field.get(target);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static Resource blink() {
		return ResourceBuilder.create().cache(null).of(ResourcePlayerNodeTest.class.getResourceAsStream("/animation/blink.png")).blocking();
	}

	private static Resource video() {
		return ResourceBuilder.create().cache(null).of(ResourcePlayerNodeTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/frames.mkv")).blocking();
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