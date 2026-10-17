package dev.joid.lib.resource.decoder.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.FileAsset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.video.VideoAudioPlayer;

import lombok.NonNull;

public class VideoResourceDecoderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final RecordingAudioBridge       audio    = new RecordingAudioBridge();
	private final List<VideoResourceDecoder> decoders = new ArrayList<>();

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
			final VideoResourceDecoder decoder = VideoResourceDecoder.create(Asset.of(VideoResourceDecoderTest.class.getResourceAsStream("/video/" + name)));
			final ResourceData data = ResourceData.create(name, null);
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
		vp9.decode(ResourceData.create("alpha-vp9.webm", null));
		vp8.decode(ResourceData.create("alpha-vp8.webm", null));
		Assert.assertEquals("libvpx-vp9", ((String) VideoResourceDecoderTest.field(vp9, "codec")));
		Assert.assertEquals("libvpx", ((String) VideoResourceDecoderTest.field(vp8, "codec")));
	}

	@Test
	public void readsTheSizeTheDurationAndTheFrameRate() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = ResourceData.create("frames.mkv", null);
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
	public void copiesItsAssetIntoTheCacheUnderTheHashOfItsContent() throws IOException, NoSuchAlgorithmException {
		final VideoResourceDecoder decoder = this.frames();
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "file"));
		decoder.decode(ResourceData.create("frames.mkv", null));
		final File file = ((File) VideoResourceDecoderTest.field(decoder, "file"));
		final byte[] bytes = VideoResourceDecoderTest.bytes("frames.mkv");
		Assert.assertEquals(String.format(Locale.ROOT, "%040x", new BigInteger(1, MessageDigest.getInstance("SHA-1").digest(bytes))), file.getName());
		Assert.assertEquals("video", file.getParentFile().getName());
		Assert.assertArrayEquals(bytes, Files.readAllBytes(file.toPath()));
	}

	@Test
	public void readsAFileInPlace() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = VideoResourceDecoder.create(file);
		this.decoders.add(decoder);
		final ResourceData data = ResourceData.create("frames.mkv", null);
		decoder.decode(data);
		Assert.assertSame(file, VideoResourceDecoderTest.field(decoder, "file"));
		Assert.assertTrue(decoder.getAsset() instanceof FileAsset);
		Assert.assertEquals(16, data.getWidth());
	}

	@Test
	public void neverDeletesTheFileItReadsInPlace() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = this.decoder(file);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		decoder.clear(data);
		Assert.assertTrue(file.isFile());
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "file"));
	}

	@Test
	public void sharesItsCopyWithItsForksAndKeepsItInTheCache() {
		final File previous = JOIDEnv.getCacheDir();
		JOIDEnv.cacheDir(this.folder.getRoot());
		try {
			final VideoResourceDecoder decoder = this.frames();
			final VideoResourceDecoder fork = decoder.fork();
			this.decoders.add(fork);
			final ResourceData data = VideoResourceDecoderTest.load(decoder);
			final ResourceData forked = VideoResourceDecoderTest.load(fork);
			Assert.assertSame(decoder, VideoResourceDecoderTest.field(fork, "origin"));
			Assert.assertSame(VideoResourceDecoderTest.field(decoder, "file"), VideoResourceDecoderTest.field(fork, "file"));
			Assert.assertNotSame(VideoResourceDecoderTest.field(decoder, "grabber"), VideoResourceDecoderTest.field(fork, "grabber"));
			Assert.assertEquals(16, forked.getWidth());
			final File copy = ((File) VideoResourceDecoderTest.field(decoder, "file"));
			decoder.clear(data);
			Assert.assertTrue(copy.isFile());
			fork.clear(forked);
			Assert.assertTrue(copy.isFile());
		} finally {
			JOIDEnv.cacheDir(previous);
		}
	}

	@Test
	public void playsItsForkApart() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final VideoResourceDecoder fork = decoder.fork();
		this.decoders.add(fork);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		final ResourceData forked = VideoResourceDecoderTest.load(fork.autoplay(false));
		this.play(decoder, data, 7);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertFalse(fork.isPlaying());
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertEquals(20, VideoResourceDecoderTest.shade(forked), 3);
	}

	@Test
	public void queuesNoAudioBeforeZeroAfterASeek() throws Exception {
		for (final String path : new String[] {"/video/opus-vp9.webm", "/dev/joid/lib/resource/decoder/impl/voiced.mkv"}) {
			final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream(path));
			VideoResourceDecoderTest.load(decoder);
			Assert.assertTrue(path, ((FFmpegFrameGrabber) VideoResourceDecoderTest.field(decoder, "grabber")).getFormatContext().start_time() < 0L);
			decoder.seek(0D);
			final long deadline = System.currentTimeMillis() + 5000L;
			while (!((boolean) VideoResourceDecoderTest.field(decoder, "ended")) && ((ArrayBlockingQueue<?>) VideoResourceDecoderTest.field(decoder, "frameQueue")).remainingCapacity() > 0 && System.currentTimeMillis() < deadline) {
				Thread.sleep(1L);
			}

			final double rate = ((FFmpegFrameGrabber) VideoResourceDecoderTest.field(decoder, "grabber")).getSampleRate();
			final int queued = VideoResourceDecoderTest.queuedSamples(((VideoAudioPlayer) VideoResourceDecoderTest.field(decoder, "audioPlayer")));
			Assert.assertTrue(path + " " + queued, queued > 0);
			Assert.assertTrue(path + " " + queued, queued <= Math.ceil(VideoResourceDecoderTest.audioEnd(path) * rate));
			decoder.release();
		}
	}

	@Test
	public void keepsTheAudioOfAWebmOnceLooped() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/opus-vp9.webm")).loop(true);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		while (((double) VideoResourceDecoderTest.field(decoder, "loopOffset")) <= 0D) {
			this.play(decoder, data, 1);
		}
		this.play(decoder, data, 30);
		final int written = Collections.frequency(this.audio.calls, "write");
		this.play(decoder, data, 30);
		Assert.assertTrue(Collections.frequency(this.audio.calls, "write") > written);
	}

	@Test
	public void loopsAWebmWhoseAudioStartsBeforeZeroUnderAFrozenClock() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/opus-vp9.webm")).loop(true);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		Assert.assertTrue(((FFmpegFrameGrabber) VideoResourceDecoderTest.field(decoder, "grabber")).getFormatContext().start_time() < 0L);
		for (int frame = 0; frame < 60; frame++) {
			this.play(decoder, data, 1);
			Assert.assertTrue("frame " + frame, decoder.isSettled());
		}
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertTrue(((double) VideoResourceDecoderTest.field(decoder, "loopOffset")) > 0D);
	}

	@Test
	public void readsAFileAssetInPlace() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = this.decoder(file);
		final ResourceData data = ResourceData.create("frames.mkv", null);
		decoder.decode(data);
		Assert.assertSame(file, VideoResourceDecoderTest.field(decoder, "file"));
		Assert.assertEquals(16, data.getWidth());
	}

	@Test
	public void preparesTwoPlaceholders() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = ResourceData.create("frames.mkv", decoder);
		decoder.init(data);
		decoder.prepare(data);
		Assert.assertEquals(2, ((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures")).length);
		Assert.assertSame(((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures"))[0], data.getTextures()[0]);
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
		Assert.assertSame(((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures"))[0], data.getTextures()[0]);
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
	public void playsOnceDecodedWhenAskedBefore() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames().autoplay(false);
		Assert.assertSame(decoder, decoder.play());
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "grabber"));
		Assert.assertFalse(decoder.isPlaying());
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		Assert.assertTrue(decoder.isPlaying());
		this.play(decoder, data, 7);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
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
		Assert.assertSame(((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures"))[0], data.getTextures()[0]);
		this.play(decoder, data, 6);
		Assert.assertEquals(60, VideoResourceDecoderTest.shade(data), 3);
		Assert.assertSame(((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures"))[1], data.getTextures()[0]);
	}

	@Test
	public void stopsOnItsLastFrame() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 70);
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertTrue(((boolean) VideoResourceDecoderTest.field(decoder, "ended")));
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
		Assert.assertSame(decoder, decoder.play());
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
		decoder.pause().play();
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
		decoder.play();
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
		decoder.play();
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
	public void seeksWhileItsAssetIsCopied() throws InterruptedException {
		final CountDownLatch copying = new CountDownLatch(1);
		final CountDownLatch copied = new CountDownLatch(1);
		final VideoResourceDecoder decoder = this.decoder(new InputStream() {
			@Override
			public int read() throws IOException {
				copying.countDown();
				try {
					copied.await();
				} catch (final InterruptedException exception) {
					throw new IOException(exception);
				}
				return -1;
			}
		});
		final ResourceData data = ResourceData.create("slow.mkv", decoder);
		final Thread decoding = new Thread(() -> data.generate(false));
		final Thread seeking = new Thread(() -> decoder.seek(0.5D));
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		final boolean blocked;
		try {
			decoding.start();
			copying.await();
			seeking.start();
			seeking.join(1000L);
			blocked = seeking.isAlive();
			copied.countDown();
			decoding.join();
			seeking.join();
		} finally {
			JOIDEnv.devMode(devMode);
		}
		Assert.assertFalse(blocked);
	}

	@Test
	public void stopsItsDecodeThread() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 3);
		Assert.assertSame(decoder, decoder.stop());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertFalse(decoder.isPaused());
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "decodeThread"));
		Assert.assertTrue(decoder.isSettled());
	}

	@Test
	public void releasesItsGrabber() {
		final VideoResourceDecoder decoder = this.frames();
		VideoResourceDecoderTest.load(decoder);
		decoder.release();
		final ResourceData data = ResourceData.create("frames.mkv", null);
		decoder.decode(data);
		Assert.assertTrue(((boolean) VideoResourceDecoderTest.field(decoder, "released")));
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "grabber"));
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(0, data.getWidth());
	}

	@Test
	public void reusesTheCachedCopyOfTheSameContent() {
		final File previous = JOIDEnv.getCacheDir();
		JOIDEnv.cacheDir(this.folder.getRoot());
		try {
			final File directory = new File(this.folder.getRoot(), "video");
			final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/alpha-vp9.webm"));
			final VideoResourceDecoder other = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/alpha-vp9.webm"));
			Assert.assertFalse(directory.exists());
			final ResourceData data = ResourceData.create("alpha-vp9.webm", null);
			final ResourceData copy = ResourceData.create("alpha-vp9.webm", null);
			decoder.decode(data);
			other.decode(copy);
			Assert.assertEquals(((File) VideoResourceDecoderTest.field(decoder, "file")), ((File) VideoResourceDecoderTest.field(other, "file")));
			decoder.clear(data);
			other.clear(copy);
			Assert.assertEquals(1, directory.list((parent, name) -> !name.startsWith(".")).length);
		} finally {
			JOIDEnv.cacheDir(previous);
		}
	}

	@Test
	public void deletesItsTexturesOnClear() {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		final List<RecordingTexture> textures = VideoResourceDecoderTest.textures(decoder);
		decoder.clear(data);
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "textures"));
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "grabber"));
		for (final RecordingTexture texture : textures) {
			Assert.assertTrue(texture.isDeleted());
		}
		decoder.clear(data);
	}

	@Test
	public void decodesAndPlaysAgainOnceCleared() throws InterruptedException, IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = this.decoder(file);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 3);
		decoder.clear(data);
		final ResourceData reloaded = VideoResourceDecoderTest.load(decoder);
		Assert.assertNotNull(reloaded.getData(0));
		this.play(decoder, reloaded, 7);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(reloaded.getWidth(), reloaded.getTextures()[0].getWidth());
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(reloaded), 3);
	}

	@Test
	public void playsOnceDecodedAgainWhenAskedRightAfterAClear() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 3);
		decoder.clear(data);
		decoder.play();
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "grabber"));
		Assert.assertFalse(decoder.isPlaying());
		final ResourceData reloaded = VideoResourceDecoderTest.load(decoder);
		Assert.assertTrue(decoder.isPlaying());
		this.play(decoder, reloaded, 7);
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(reloaded), 3);
	}

	@Test
	public void reopensAReleasedVideoOnPlay() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 20);
		decoder.release();
		Assert.assertSame(decoder, decoder.play());
		Assert.assertFalse(((boolean) VideoResourceDecoderTest.field(decoder, "released")));
		Assert.assertNotNull(VideoResourceDecoderTest.field(decoder, "grabber"));
		this.play(decoder, data, 7);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(40, VideoResourceDecoderTest.shade(data), 3);
	}

	@Test
	public void reportsAVideoItCannotReopen() throws IOException {
		final File file = this.folder.newFile("frames.mkv");
		Files.write(file.toPath(), VideoResourceDecoderTest.bytes("frames.mkv"));
		final VideoResourceDecoder decoder = VideoResourceDecoder.create(file);
		this.decoders.add(decoder);
		VideoResourceDecoderTest.load(decoder);
		decoder.release();
		Files.delete(file.toPath());
		final String error = VideoResourceDecoderTest.capture(decoder::play);
		Assert.assertTrue(error, error.startsWith("[JOID] The video " + file.getPath() + " cannot be reopened ("));
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "grabber"));
		Assert.assertFalse(decoder.isPlaying());
	}

	@Test
	public void failsAnUnreadableVideo() {
		final VideoResourceDecoder decoder = this.decoder(new ByteArrayInputStream("not a video".getBytes(StandardCharsets.UTF_8)));
		final ResourceData data = ResourceData.create("notes.mkv", decoder);
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		final String error;
		try {
			error = VideoResourceDecoderTest.capture(() -> data.generate(false));
		} finally {
			JOIDEnv.devMode(devMode);
		}
		data.upload();
		Assert.assertEquals("", error);
		Assert.assertTrue(data.isFailed());
		Assert.assertNull(data.getData());
		Assert.assertFalse(decoder.isPlaying());
	}

	@Test
	public void leavesTheFailureOfItsDecodeToItsResourceData() {
		final VideoResourceDecoder decoder = this.decoder(new ByteArrayInputStream("not a video".getBytes(StandardCharsets.UTF_8)));
		final ResourceData data = ResourceData.create("notes.mkv", decoder);
		decoder.prepare(data);
		try {
			decoder.decode(data);
			Assert.fail("An unreadable video must throw from its decode");
		} catch (final IllegalStateException expected) {
			Assert.assertFalse(data.isFailed());
		}
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
		Assert.assertNotNull(VideoResourceDecoderTest.field(decoder, "audioPlayer"));
		Assert.assertEquals("create 8000x1", this.audio.calls.get(0));
		this.play(decoder, data, 60);
		Assert.assertTrue(this.audio.calls.contains("write"));
		this.audio.calls.clear();
		decoder.pause();
		decoder.play();
		decoder.seek(0.2D);
		decoder.stop();
		decoder.release();
		Assert.assertEquals(Arrays.asList("pause", "play", "stop", "stop", "stop", "delete"), this.audio.calls);
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "audioPlayer"));
	}

	@Test
	public void playsTheAudioThatPrecedesItsFirstImage() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/video/opus-vp9.webm"));
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 20);
		Assert.assertTrue(this.audio.calls.toString(), this.audio.calls.contains("write"));
	}

	@Test
	public void givesItsAudioGroupToItsSource() {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv"));
		Assert.assertNull(decoder.getAudioGroup());
		Assert.assertSame(decoder, decoder.audioGroup("music"));
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		Assert.assertEquals(Arrays.asList("create 8000x1", "group music"), this.audio.calls.subList(0, 2));
		decoder.audioGroup(null);
		decoder.update(data);
		Assert.assertTrue(this.audio.calls.toString(), this.audio.calls.contains("group null"));
		Assert.assertNull(decoder.getAudioGroup());
	}

	@Test
	public void placesItsAudioInSpace() {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv"));
		Assert.assertEquals(50F, decoder.getMaxDistance(), 0F);
		Assert.assertEquals(5F, decoder.getReferenceDistance(), 0F);
		Assert.assertSame(decoder, decoder.location(1F, 2F, 3F).referenceDistance(2F).maxDistance(9F));
		decoder.decode(ResourceData.create("voiced.mkv", null));
		Assert.assertNotNull(VideoResourceDecoderTest.field(decoder, "audioPlayer"));
		decoder.location(4F, 5F, 6F).referenceDistance(3F).maxDistance(12F);
		Assert.assertEquals(4D, decoder.getLocation().getX(), 0D);
		Assert.assertEquals(5D, decoder.getLocation().getY(), 0D);
		Assert.assertEquals(6D, decoder.getLocation().getZ(), 0D);
		Assert.assertEquals(3F, decoder.getReferenceDistance(), 0F);
		Assert.assertEquals(12F, decoder.getMaxDistance(), 0F);
	}

	@Test
	public void skipsTheAudioOfAMutedVideo() {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv")).volume(0F);
		decoder.decode(ResourceData.create("voiced.mkv", null));
		Assert.assertEquals(0F, decoder.getVolume(), 0F);
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "audioPlayer"));
		Assert.assertTrue(this.audio.calls.isEmpty());
	}

	@Test
	public void keepsTheInterruptionOfItsCallerWhenReleased() throws ReflectiveOperationException {
		final CountDownLatch done = new CountDownLatch(1);
		final Thread stubborn = new Thread(() -> {
			while (done.getCount() > 0L) {
				try {
					done.await();
				} catch (final InterruptedException ignored) {}
			}
		});
		stubborn.start();
		final VideoResourceDecoder decoder = this.frames();
		final Field field = VideoResourceDecoder.class.getDeclaredField("decodeThread");
		field.setAccessible(true);
		field.set(decoder, stubborn);
		Thread.currentThread().interrupt();
		try {
			decoder.release();
			Assert.assertTrue(Thread.interrupted());
		} finally {
			Thread.interrupted();
			done.countDown();
		}
	}

	@Test
	public void startsItsAudioOnceUnmuted() throws InterruptedException {
		final VideoResourceDecoder decoder = this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/voiced.mkv")).volume(0F);
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 10);
		Assert.assertNull(VideoResourceDecoderTest.field(decoder, "audioPlayer"));
		decoder.volume(1F);
		Assert.assertNotNull(VideoResourceDecoderTest.field(decoder, "audioPlayer"));
		Assert.assertEquals("create 8000x1", this.audio.calls.get(0));
		this.play(decoder, data, 60);
		Assert.assertTrue(this.audio.calls.contains("write"));
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
	public void playsFromTheTimeItSeeksToWhileStopped() throws InterruptedException {
		final VideoResourceDecoder decoder = this.frames();
		final ResourceData data = VideoResourceDecoderTest.load(decoder);
		this.play(decoder, data, 20);
		decoder.stop().seek(0.5D).play();
		this.play(decoder, data, 1);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(0.5D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(119, VideoResourceDecoderTest.shade(data), 3);
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
			final VideoResourceDecoder decoder = VideoResourceDecoder.create(file);
			final ResourceData data = ResourceData.create("padded.mkv", null);
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
		VideoResourceDecoderTest.buffer(decoder);
		decoder.seek(0.6D);
		this.play(decoder, data, 1);
		Assert.assertEquals(6, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(140, VideoResourceDecoderTest.shade(data), 3);
		this.play(decoder, data, 6);
		Assert.assertEquals(7, decoder.getDisplayedFrameIndex());
		Assert.assertEquals(160, VideoResourceDecoderTest.shade(data), 3);
	}

	private static int queuedSamples(final VideoAudioPlayer player) throws ReflectiveOperationException {
		final Field field = VideoAudioPlayer.class.getDeclaredField("sampleQueue");
		field.setAccessible(true);
		int samples = 0;
		for (final Object chunk : (Collection<?>) field.get(player)) {
			samples += ((short[]) chunk).length;
		}
		return samples;
	}

	private static double audioEnd(final String path) throws Exception {
		final File file = File.createTempFile("joid-audio-", ".bin");
		try {
			Files.write(file.toPath(), Asset.of(VideoResourceDecoderTest.class.getResourceAsStream(path)).read());
			final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
			grabber.start();
			try {
				double end = 0D;
				for (Frame frame = grabber.grabSamples(); frame != null; frame = grabber.grabSamples()) {
					end = Math.max(end, frame.timestamp / 1000000D + frame.samples[0].limit() / (double) grabber.getSampleRate());
				}
				return end;
			} finally {
				grabber.stop();
				grabber.release();
			}
		} finally {
			Files.delete(file.toPath());
		}
	}

	private VideoResourceDecoder frames() {
		return this.decoder(VideoResourceDecoderTest.class.getResourceAsStream("/dev/joid/lib/resource/decoder/impl/frames.mkv"));
	}

	private VideoResourceDecoder decoder(final Object handle) {
		final VideoResourceDecoder decoder = VideoResourceDecoder.create(Asset.of(handle));
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
		final ResourceData data = ResourceData.create("video", decoder);
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
		textures.add((RecordingTexture) ((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures"))[0]);
		textures.add((RecordingTexture) ((ITexture[]) VideoResourceDecoderTest.field(decoder, "textures"))[1]);
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

	private static void buffer(final VideoResourceDecoder decoder) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 5000L;
		while (((ArrayBlockingQueue<?>) VideoResourceDecoderTest.field(decoder, "frameQueue")).remainingCapacity() > 0 && System.currentTimeMillis() < deadline) {
			Thread.sleep(1L);
		}
	}

	private static Object field(final Object target, final String name) {
		try {
			final Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
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