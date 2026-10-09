package dev.joid.lib.resource.decoder.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.resource.ResourceData;

import lombok.NonNull;

public class VideoResourceDecoderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final List<VideoResourceDecoder> decoders = new ArrayList<>();
	private final RecordingAudioBridge       audio    = new RecordingAudioBridge();

	@Before
	public void recordTheAudio() {
		BridgeHandler.AUDIO.register(this.audio);
	}

	@After
	public void releaseEveryDecoder() {
		for (final VideoResourceDecoder decoder : this.decoders) {
			decoder.release();
		}
		BridgeHandler.AUDIO.unregister(this.audio);
	}

	@Test
	public void keepsTheTransparencyOfAWebm() {
		for (final String name : new String[] {"alpha-vp9.webm", "alpha-vp8.webm"}) {
			final VideoResourceDecoder decoder = new VideoResourceDecoder(Asset.of(VideoResourceDecoderTest.class.getResourceAsStream("/video/" + name)));
			final ResourceData data = new ResourceData(name, null);
			decoder.decode(data);
			try {
				final int[] pixels = data.getData()[0];
				Assert.assertEquals(64, data.getWidth());
				Assert.assertEquals(32, data.getHeight());
				Assert.assertEquals(name, 255, pixels[16 * 64 + 4] >>> 24);
				Assert.assertEquals(name, 0, pixels[16 * 64 + 50] >>> 24);
				Assert.assertEquals(name, 255, pixels[16 * 64 + 4] >> 16 & 0xFF, 8);
			} finally {
				decoder.release();
			}
		}
	}

	@Test
	public void decodesAnAlphaWebmWithLibvpx() {
		final VideoResourceDecoder vp9 = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/alpha-vp9.webm"));
		final VideoResourceDecoder vp8 = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/alpha-vp8.webm"));
		vp9.decode(new ResourceData("alpha-vp9.webm", null));
		vp8.decode(new ResourceData("alpha-vp8.webm", null));
		Assert.assertEquals("libvpx-vp9", vp9.getCodec());
		Assert.assertEquals("libvpx", vp8.getCodec());
	}

	@Test
	public void readsTheSizeTheDurationAndTheFrameRate() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = new ResourceData("frames.mkv", null);
		decoder.decode(data);
		Assert.assertEquals(16, data.getWidth());
		Assert.assertEquals(8, data.getHeight());
		Assert.assertEquals(1D, decoder.getDuration(), 0D);
		Assert.assertEquals(10D, decoder.getFrameRate(), 0D);
		Assert.assertEquals(10, decoder.getTotalFrames());
		Assert.assertEquals(16 * 8, data.getData()[0].length);
		Assert.assertEquals(20, data.getData()[0][0] & 0xFF, 3);
		Assert.assertEquals(20, data.getData()[0][16 * 8 - 1] & 0xFF, 3);
	}

	@Test
	public void copiesItsAssetIntoATemporaryFile() throws IOException {
		final VideoResourceDecoder decoder = this.frames();
		Assert.assertNull(decoder.getFile());
		decoder.decode(new ResourceData("frames.mkv", null));
		final File file = decoder.getFile();
		Assert.assertTrue(file.getName(), file.getName().startsWith("joid-video-"));
		Assert.assertTrue(file.getName(), file.getName().endsWith(".mp4"));
		Assert.assertArrayEquals(VideoResourceDecoderTest.bytes("frames.mkv"), Files.readAllBytes(file.toPath()));
	}

	@Test
	public void readsAFileInPlace() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = new VideoResourceDecoder(file);
		this.decoders.add(decoder);
		final ResourceData data = new ResourceData("frames.mkv", null);
		decoder.decode(data);
		Assert.assertSame(file, decoder.getFile());
		Assert.assertNull(decoder.getAsset());
		Assert.assertEquals(16, data.getWidth());
	}

	@Test
	public void readsAFileAssetInPlace() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = this.decoder(file);
		final ResourceData data = new ResourceData("frames.mkv", null);
		decoder.decode(data);
		Assert.assertSame(file, decoder.getFile());
		Assert.assertEquals(16, data.getWidth());
	}

	@Test
	public void preparesTwoPlaceholders() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = new ResourceData("frames.mkv", decoder);
		decoder.init(data);
		decoder.prepare(data);
		Assert.assertEquals(2, decoder.getTextures().length);
		Assert.assertSame(decoder.getTextures()[0], data.getTextures()[0]);
		for (final RecordingTexture texture : VideoResourceDecoderTest.textures(decoder)) {
			Assert.assertEquals(1, texture.getWidth());
			Assert.assertArrayEquals(new int[] {0}, texture.getPixels());
		}
	}

	@Test
	public void uploadsItsFirstFrameIntoBothTexturesAndPlays() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		for (final RecordingTexture texture : VideoResourceDecoderTest.textures(decoder)) {
			Assert.assertEquals(16, texture.getWidth());
			Assert.assertEquals(8, texture.getHeight());
			Assert.assertEquals(20, texture.getPixels()[0] & 0xFF, 3);
		}
		Assert.assertSame(decoder.getTextures()[0], data.getTextures()[0]);
		Assert.assertTrue(decoder.isAutoplay());
		Assert.assertTrue(decoder.isPlaying());
	}

	@Test
	public void waitsWithoutAutoplay() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames().autoplay(false);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 10);
		Assert.assertFalse(decoder.isAutoplay());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(20, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void ignoresAPauseBeforePlaying() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames().autoplay(false);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		Assert.assertSame(decoder, decoder.pause());
		Assert.assertFalse(decoder.isPaused());
		decoder.play();
		this.play(decoder, data, 3);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertFalse(decoder.isPaused());
	}

	@Test
	public void ignoresAPauseOnceStopped() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 3);
		decoder.stop().pause();
		Assert.assertFalse(decoder.isPaused());
		Assert.assertFalse(decoder.isPlaying());
	}

	@Test
	public void uploadsNothingWithoutPicture() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = new ResourceData("frames.mkv", decoder);
		decoder.prepare(data);
		decoder.upload(data);
		data.data(new int[1][]);
		decoder.upload(data);
		Assert.assertEquals(1, data.getTextures()[0].getWidth());
		Assert.assertFalse(decoder.isPlaying());
	}

	@Test
	public void showsEachFrameOnTheClock() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		Assert.assertEquals(20, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(0, decoder.getDisplayedFrameIndex());
		this.play(decoder, data, 1);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(1, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(0.1D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(0.1D, decoder.getProgress(), 1E-9D);
		Assert.assertSame(decoder.getTextures()[1], data.getTextures()[0]);
		this.play(decoder, data, 6);
		Assert.assertEquals(60, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertSame(decoder.getTextures()[0], data.getTextures()[0]);
	}

	@Test
	public void stopsOnItsLastFrame() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 70);
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertTrue(decoder.isEnded());
		Assert.assertTrue(decoder.isSettled());
		Assert.assertEquals(200, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(0.9D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(0.9D, decoder.getProgress(), 1E-9D);
	}

	@Test
	public void loopsBackToItsFirstFrame() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames().loop(true);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 72);
		Assert.assertTrue(decoder.isLoop());
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(1, decoder.getDisplayedFrameIndex());
	}

	@Test
	public void freezesWhilePaused() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 10);
		Assert.assertSame(decoder, decoder.pause());
		Assert.assertTrue(decoder.isPaused());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertTrue(decoder.isSettled());
		this.play(decoder, data, 20);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertSame(decoder, decoder.resume());
		this.play(decoder, data, 3);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(60, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void keepsTheFirstPause() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.pause();
		this.play(decoder, data, 30);
		decoder.pause().resume();
		this.play(decoder, data, 1);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void resumesOnlyAPausedPlayback() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.resume();
		this.play(decoder, data, 1);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void seeksToATime() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		Assert.assertSame(decoder, decoder.seek(0.5D));
		Assert.assertEquals(5, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(0.5D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(0.5D, decoder.getProgress(), 1E-9D);
		this.play(decoder, data, 24);
		Assert.assertEquals(180, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void seeksWhilePaused() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.pause().seek(0.7D);
		this.play(decoder, data, 10);
		Assert.assertEquals(0.7D, decoder.getCurrentTime(), 1E-9D);
		decoder.resume();
		this.play(decoder, data, 7);
		Assert.assertEquals(180, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void showsTheFrameItSeeksToWhilePaused() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.pause().seek(0.5D);
		this.play(decoder, data, 1);
		Assert.assertTrue(decoder.isPaused());
		Assert.assertEquals(120, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(5, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(0.5D, decoder.getProgress(), 1E-9D);
		this.play(decoder, data, 20);
		Assert.assertEquals(120, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(0.5D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void followsEverySeekWhilePaused() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.pause();
		for (final int frame : new int[] {3, 8, 2, 6}) {
			decoder.seek(frame / 10D);
			this.play(decoder, data, 1);
			Assert.assertEquals(20 * (frame + 1), VideoResourceDecoderTest.shade(data), 3);
			Assert.assertEquals(frame, decoder.getDisplayedFrameIndex());
		}
	}

	@Test
	public void resumesFromTheFrameItSeeksToWhilePaused() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.pause().seek(0.5D);
		this.play(decoder, data, 10);
		decoder.resume();
		this.play(decoder, data, 7);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(140, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(6, decoder.getDisplayedFrameIndex());
	}

	@Test
	public void seeksToItsFirstFrameWhilePaused() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 30);
		decoder.pause().seek(-1D);
		this.play(decoder, data, 1);
		Assert.assertEquals(20, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(0D, decoder.getProgress(), 0D);
	}

	@Test
	public void seeksToItsLastFrameWhilePaused() throws InterruptedException {
		for (final boolean loop : new boolean[] {false, true}) {
			final VideoResourceDecoder decoder = this.frames().loop(loop);
			final ResourceData data = VideoResourceDecoderTest.load(decoder);
			this.play(decoder, data, 6);
			decoder.pause().seek(5D);
			this.play(decoder, data, 1);
			Assert.assertEquals(200, VideoResourceDecoderTest.shade(data), 3);
			Assert.assertEquals(0.9D, decoder.getProgress(), 1E-9D);
			Assert.assertTrue(decoder.isPaused());
		}
	}

	@Test
	public void ignoresASeekOnceReleased() {
		final VideoResourceDecoder decoder = this.frames();
		VideoResourceDecoderTest.load(decoder);
		decoder.release();
		decoder.seek(0.5D);
		Assert.assertEquals(0D, decoder.getCurrentTime(), 0D);
	}

	@Test
	public void stopsItsDecodeThread() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 3);
		Assert.assertSame(decoder, decoder.stop());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertFalse(decoder.isPaused());
		Assert.assertNull(decoder.getDecodeThread());
		Assert.assertTrue(decoder.isSettled());
	}

	@Test
	public void releasesItsGrabber() {
		final VideoResourceDecoder decoder = this.frames();
		VideoResourceDecoderTest.load(decoder);
		decoder.release();
		final ResourceData data = new ResourceData("frames.mkv", null);
		decoder.decode(data);
		Assert.assertTrue(decoder.isReleased());
		Assert.assertNull(decoder.getGrabber());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(0, data.getWidth());
	}

	@Test
	public void deletesItsTexturesOnClear() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		final List<RecordingTexture> textures = VideoResourceDecoderTest.textures(decoder);
		decoder.clear(data);
		Assert.assertNull(decoder.getTextures());
		Assert.assertTrue(decoder.isReleased());
		for (final RecordingTexture texture : textures) {
			Assert.assertTrue(texture.isDeleted());
		}
		decoder.clear(data);
	}

	@Test
	public void reopensAReleasedVideoOnPlay() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 20);
		decoder.release();
		Assert.assertSame(decoder, decoder.play());
		Assert.assertFalse(decoder.isReleased());
		Assert.assertNotNull(decoder.getGrabber());
		this.play(decoder, data, 7);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void reportsAVideoItCannotReopen() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = new VideoResourceDecoder(file);
		this.decoders.add(decoder);
		VideoResourceDecoderTest.load(decoder);
		decoder.release();
		Files.delete(file.toPath());
		final String error = VideoResourceDecoderTest.capture(decoder::play);
		Assert.assertTrue(error, error.startsWith("Failed to reopen video grabber: "));
		Assert.assertNull(decoder.getGrabber());
	}

	@Test
	public void failsAnUnreadableVideo() {
		final VideoResourceDecoder decoder = this.decoder(new ByteArrayInputStream("not a video".getBytes(StandardCharsets.UTF_8)));
		final ResourceData data = new ResourceData("notes.mkv", decoder);
		decoder.prepare(data);
		final String error = VideoResourceDecoderTest.capture(() -> decoder.decode(data));
		decoder.upload(data);
		Assert.assertEquals("", error);
		Assert.assertTrue(data.isFailed());
		Assert.assertNull(data.getData());
		Assert.assertFalse(decoder.isPlaying());
	}

	@Test
	public void hasNoPositionBeforeItsDecode() {
		final VideoResourceDecoder decoder = this.frames();
		Assert.assertEquals(0D, decoder.getProgress(), 0D);
		Assert.assertEquals(0D, decoder.getCurrentTime(), 0D);
		Assert.assertTrue(decoder.isSettled());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertFalse(decoder.isLoop());
		Assert.assertEquals(1F, decoder.getVolume(), 0F);
	}

	@Test
	public void streamsItsAudioThroughTheAudioBridge() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv"));
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		Assert.assertNotNull(decoder.getAudioPlayer());
		Assert.assertEquals("create 8000x1", this.audio.calls.get(0));
		this.play(decoder, data, 60);
		Assert.assertTrue(decoder.getAudioPlayer().getQueueSize() > 0);
		this.audio.calls.clear();
		decoder.pause();
		decoder.resume();
		decoder.seek(0.2D);
		decoder.stop();
		decoder.release();
		Assert.assertEquals(Arrays.asList("pause", "play", "stop", "stop", "stop", "delete"), this.audio.calls);
		Assert.assertNull(decoder.getAudioPlayer());
	}

	@Test
	public void givesItsAudioGroupToItsSource() {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv"));
		Assert.assertNull(decoder.getAudioGroup());
		Assert.assertSame(decoder, decoder.audioGroup("music"));
		VideoResourceDecoderTest.load(decoder);
		Assert.assertEquals(Arrays.asList("create 8000x1", "group music"), this.audio.calls.subList(0, 2));
		decoder.audioGroup(null);
		Assert.assertEquals("group null", this.audio.calls.get(this.audio.calls.size() - 1));
		Assert.assertNull(decoder.getAudioGroup());
	}

	@Test
	public void placesItsAudioInSpace() {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv"));
		Assert.assertEquals(50F, decoder.getMaxDistance(), 0F);
		Assert.assertEquals(5F, decoder.getReferenceDistance(), 0F);
		Assert.assertSame(decoder, decoder.location(1F, 2F, 3F).referenceDistance(2F).maxDistance(9F));
		decoder.decode(new ResourceData("voiced.mkv", null));
		Assert.assertNotNull(decoder.getAudioPlayer());
		Assert.assertTrue(decoder.isHasLocation());
		decoder.location(4F, 5F, 6F).referenceDistance(3F).maxDistance(12F);
		Assert.assertEquals(4F, decoder.getLocationX(), 0F);
		Assert.assertEquals(5F, decoder.getLocationY(), 0F);
		Assert.assertEquals(6F, decoder.getLocationZ(), 0F);
		Assert.assertEquals(3F, decoder.getReferenceDistance(), 0F);
		Assert.assertEquals(12F, decoder.getMaxDistance(), 0F);
	}

	@Test
	public void skipsTheAudioOfAMutedVideo() {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv")).volume(0F);
		decoder.decode(new ResourceData("voiced.mkv", null));
		Assert.assertEquals(0F, decoder.getVolume(), 0F);
		Assert.assertNull(decoder.getAudioPlayer());
		Assert.assertTrue(this.audio.calls.isEmpty());
	}

	@Test
	public void stopsItsAudioWithItsLastFrame() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv"));
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 150);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertFalse(this.audio.calls.contains("stop"));
		this.play(decoder, data, 100);
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertTrue(this.audio.calls.contains("stop"));
	}

	@Test
	public void opensItsFileOnPlayWithoutDecoding() throws IOException {
		final File file = this.folder.newFile("voiced.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("voiced.mkv"));
		final VideoResourceDecoder decoder = new VideoResourceDecoder(file).location(1F, 2F, 3F);
		this.decoders.add(decoder);
		decoder.play();
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertNotNull(decoder.getGrabber());
		Assert.assertNotNull(decoder.getFrameQueue());
		Assert.assertNotNull(decoder.getAudioPlayer());
		Assert.assertEquals("create 8000x1", this.audio.calls.get(0));
		decoder.stop();
	}

	@Test
	public void playsAgainFromTheBeginningOnceEnded() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 70);
		Assert.assertFalse(decoder.isPlaying());
		decoder.play();
		this.play(decoder, data, 7);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void playsFromTheBeginningAfterAStop() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 20);
		decoder.stop().play();
		this.play(decoder, data, 7);
		Assert.assertEquals(0.1D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void showsTheFrameItSeeksTo() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 6);
		decoder.seek(0.5D);
		this.play(decoder, data, 1);
		Assert.assertEquals(119, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void decodesAVideoWhoseRowsArePaddedWithoutCorruptingTheHeap() throws IOException {
		final File file = this.folder.newFile("padded.mkv");
		Files.write(file.toPath(), Asset.of(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/padded.mkv")).read());
		for (int i = 0; i < 500; i++) {
			final VideoResourceDecoder decoder = new VideoResourceDecoder(file);
			final ResourceData data = new ResourceData("padded.mkv", null);
			decoder.decode(data);
			decoder.release();
			Assert.assertEquals(18 * 8, data.getData()[0].length);
		}
	}

	@Test
	public void keepsItsPositionWhenPlayedAgain() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 7);
		Assert.assertSame(decoder, decoder.play());
		this.play(decoder, data, 6);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(2, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(60, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void resumesAPausedVideoOnPlay() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 7);
		decoder.pause();
		Assert.assertSame(decoder, decoder.play());
		Assert.assertFalse(decoder.isPaused());
		this.play(decoder, data, 6);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(2, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(60, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void skipsTheFramesBetweenItsKeyFrameAndASeek() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/predicted.mkv"));
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		VideoResourceDecoderTest.buffer(decoder, 6);
		decoder.seek(0.6D);
		this.play(decoder, data, 1);
		Assert.assertEquals(6, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(140, VideoResourceDecoderTest.shade(data), 3);
		this.play(decoder, data, 6);
		Assert.assertEquals(7, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(160, VideoResourceDecoderTest.shade(data), 3);
	}

	private VideoResourceDecoder frames() {
		return this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/frames.mkv"));
	}

	private VideoResourceDecoder decoder(final Object handle) {
		final VideoResourceDecoder decoder = new VideoResourceDecoder(Asset.of(handle));
		this.decoders.add(decoder);
		return decoder;
	}

	private void play(final VideoResourceDecoder decoder, final ResourceData data, final int frames) throws InterruptedException {
		for (int i = 0; i < frames; i++) {
			this.bridges.getClock().advance(16L);
			final long deadline = System.currentTimeMillis() + 5000L;
			while (!decoder.isSettled() && System.currentTimeMillis() < deadline) {
				Thread.sleep(1L);
			}
			decoder.update(data);
		}
	}

	private static ResourceData load(final VideoResourceDecoder decoder) {
		final ResourceData data = new ResourceData("video", decoder);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		return data;
	}

	private static int shade(final ResourceData data) {
		return ((RecordingTexture) data.getTextures()[0]).getPixels()[0] & 0xFF;
	}

	private static List<RecordingTexture> textures(final VideoResourceDecoder decoder) {
		final List<RecordingTexture> textures = new ArrayList<>();
		textures.add((RecordingTexture) decoder.getTextures()[0]);
		textures.add((RecordingTexture) decoder.getTextures()[1]);
		return textures;
	}

	private static byte[] bytes(final String name) throws IOException {
		return Asset.of(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/" + name)).read();
	}

	private static String capture(final Runnable runnable) {
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private static void buffer(final VideoResourceDecoder decoder, final int frames) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 5000L;
		while (decoder.getDecodedFrameIndex().get() < frames && System.currentTimeMillis() < deadline) {
			Thread.sleep(1L);
		}
	}

	private static final class RecordingAudioBridge implements IAudioBridge {

		private final List<String> calls = new CopyOnWriteArrayList<>();

		@Override
		public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
			this.calls.add("create " + sampleRate + "x" + channels);
			return new RecordingAudioSource(this.calls);
		}

	}

	private static final class RecordingAudioSource implements IAudioSource {

		private final List<String> calls;

		private RecordingAudioSource(final List<String> calls) {
			this.calls = calls;
		}

		@Override
		public void play() {
			this.calls.add("play");
		}

		@Override
		public void stop() {
			this.calls.add("stop");
		}

		@Override
		public void pause() {
			this.calls.add("pause");
		}

		@Override
		public void gain(final float gain) {}

		@Override
		public void group(final Object group) {
			this.calls.add("group " + group);
		}

		@Override
		public void write(final @NonNull short[] samples) {
			this.calls.add("write");
		}

		@Override
		public boolean isPlaying() {
			return false;
		}

		@Override
		public int getBufferedSamples() {
			return 0;
		}

		@Override
		public void delete() {
			this.calls.add("delete");
		}

	}

}