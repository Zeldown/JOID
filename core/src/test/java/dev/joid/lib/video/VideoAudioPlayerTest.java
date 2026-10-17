package dev.joid.lib.video;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.utils.geom.Point;
import lombok.NonNull;

public class VideoAudioPlayerTest {

	private final RecordingAudioBridge audio = new RecordingAudioBridge();
	private final VideoResourceDecoder video = VideoAudioPlayerTest.video("clip.mp4");

	@Before
	public void recordTheAudio() {
		BridgeHandler.AUDIO.register(this.audio);
	}

	@After
	public void forgetTheAudio() {
		BridgeHandler.AUDIO.unregister(this.audio);
		VideoAudioPlayer.audioListenerPosition(null);
	}

	@Test
	public void createsItsSourceOnceOnPlay() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 44100, 2);
		player.play();
		player.play();
		Assert.assertEquals(Collections.singletonList("44100x2"), this.audio.created);
	}

	@Test
	public void playsWithoutSoundWithoutAudioBridge() {
		BridgeHandler.AUDIO.unregister(this.audio);
		final VideoAudioPlayer player = VideoAudioPlayer.create(VideoAudioPlayerTest.video("mute.mp4"), 8000, 1);
		final String error = VideoAudioPlayerTest.capture(player::play);
		VideoAudioPlayerTest.push(player, 16, 1024);
		player.update(0D);
		final int queued = VideoAudioPlayerTest.queueSize(player);
		BridgeHandler.AUDIO.register(this.audio);
		final String again = VideoAudioPlayerTest.capture(player::play);
		Assert.assertEquals("[JOID] The video mute.mp4 plays without sound (java.lang.IllegalStateException: No audio bridge registered, call BridgeHandler.AUDIO.register before using JOID), register an audio bridge and check the audio device" + System.lineSeparator(), error);
		Assert.assertEquals("", again);
		Assert.assertEquals(16, queued);
		Assert.assertTrue(this.audio.created.isEmpty());
	}

	@Test
	public void warnsOnceForEachVideoWithoutSound() {
		BridgeHandler.AUDIO.unregister(this.audio);
		final String first = VideoAudioPlayerTest.capture(() -> VideoAudioPlayer.create(VideoAudioPlayerTest.video("twice.mp4"), 8000, 1).play());
		final String second = VideoAudioPlayerTest.capture(() -> VideoAudioPlayer.create(VideoAudioPlayerTest.video("twice.mp4"), 8000, 1).play());
		BridgeHandler.AUDIO.register(this.audio);
		Assert.assertTrue(first, first.startsWith("[JOID] The video twice.mp4 plays without sound ("));
		Assert.assertEquals("", second);
	}

	@Test
	public void waitsUntilItPlays() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		VideoAudioPlayerTest.push(player, 16, 1024);
		player.update(0D);
		Assert.assertEquals(16, VideoAudioPlayerTest.queueSize(player));
		Assert.assertTrue(this.audio.created.isEmpty());
	}

	@Test
	public void dropsTheSamplesPushedBeforeItPlays() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		VideoAudioPlayerTest.push(player, 20, 1024);
		player.play();
		player.update(0D);
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
		Assert.assertTrue(this.audio.source().written.isEmpty());
	}

	@Test
	public void keepsTheSamplesPushedAfterItPlays() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		player.play();
		VideoAudioPlayerTest.push(player, 16, 1024);
		player.update(0D);
		Assert.assertEquals(4, this.audio.source().written.size());
	}

	@Test
	public void waitsForAFifthOfASecondBeforeQueueing() {
		final VideoAudioPlayer player = this.started();
		VideoAudioPlayerTest.push(player, 1, 1599);
		player.update(0D);
		final RecordingAudioSource source = this.audio.source();
		Assert.assertTrue(source.written.isEmpty());
		Assert.assertFalse(source.playing);
		VideoAudioPlayerTest.push(player, 1, 1);
		player.update(0D);
		Assert.assertEquals(1, source.written.size());
		Assert.assertEquals(1600, source.written.get(0).length);
		Assert.assertEquals(Collections.singletonList(0.3F), source.gains);
		Assert.assertTrue(source.playing);
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
	}

	@Test
	public void skipsTheSamplesItsVideoPlayedWhileItsQueueFilled() {
		final VideoAudioPlayer player = this.started();
		VideoAudioPlayerTest.push(player, 16, 1024);
		player.update(0.25D);
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals((short) 2000, source.written.get(0)[0]);
		Assert.assertEquals(16 * 1024 - 2000, source.getWrittenSamples());
	}

	@Test
	public void splitsLargeSamplesAcrossChunksInOrder() {
		final VideoAudioPlayer player = this.started();
		VideoAudioPlayerTest.push(player, 16, 3000);
		player.update(0D);
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals(8, source.written.size());
		source.played += 5 * 4096;
		player.update(0D);
		Assert.assertEquals(12, source.written.size());
		Assert.assertEquals(2944, source.written.get(11).length);
		int expected = 0;
		for (final short[] chunk : source.written) {
			for (final short sample : chunk) {
				Assert.assertEquals((short) expected++, sample);
			}
		}
		Assert.assertEquals(16 * 3000, expected);
	}

	@Test
	public void cutsItsChunksBetweenTwoFramesOfAMultichannelTrack() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 6);
		player.play();
		player.update(0D);
		VideoAudioPlayerTest.push(player, 16, 3000);
		player.update(0D);
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals(8, source.written.size());
		for (final short[] chunk : source.written) {
			Assert.assertEquals(4092, chunk.length);
		}
	}

	@Test
	public void trimsItsLastChunk() {
		final VideoAudioPlayer player = this.started();
		VideoAudioPlayerTest.push(player, 16, 100);
		player.update(0D);
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals(1, source.written.size());
		Assert.assertEquals(1600, source.written.get(0).length);
	}

	@Test
	public void playsASourceThatStopped() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		source.playing = false;
		source.calls.clear();
		player.update(0D);
		Assert.assertEquals(Collections.singletonList("play"), source.calls);
		player.update(0D);
		Assert.assertEquals(Collections.singletonList("play"), source.calls);
	}

	@Test
	public void appliesItsVolumeToTheGain() {
		final VideoAudioPlayer player = this.queued();
		this.video.volume(0.5F);
		player.update(0D);
		Assert.assertEquals(0.15F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void givesItsGroupToItsSource() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		this.video.audioGroup("music");
		player.play();
		Assert.assertEquals("music", this.audio.source().group);
		this.video.audioGroup(null);
		player.update(0D);
		Assert.assertNull(this.audio.source().group);
	}

	@Test
	public void pausesAndResumesItsSource() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		source.calls.clear();
		source.gains.clear();
		player.pause();
		player.update(0D);
		Assert.assertEquals(Collections.singletonList("pause"), source.calls);
		Assert.assertTrue(source.gains.isEmpty());
		player.resume();
		player.update(0D);
		Assert.assertEquals(Arrays.asList("pause", "play"), source.calls);
		Assert.assertEquals(Collections.singletonList(0.3F), source.gains);
	}

	@Test
	public void stopsItsSource() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		source.calls.clear();
		source.gains.clear();
		player.stop();
		player.update(0D);
		Assert.assertEquals(Collections.singletonList("stop"), source.calls);
		Assert.assertTrue(source.gains.isEmpty());
	}

	@Test
	public void controlsNothingBeforeItPlays() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		VideoAudioPlayerTest.push(player, 2, 10);
		player.pause();
		player.resume();
		player.stop();
		player.flush();
		player.cleanup();
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
		Assert.assertTrue(this.audio.created.isEmpty());
	}

	@Test
	public void flushesItsSamplesAndItsSource() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		VideoAudioPlayerTest.push(player, 15, 1024);
		player.flush();
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
		Assert.assertTrue(source.calls.contains("stop"));
		Assert.assertEquals(0, source.getBufferedSamples());
		final int written = source.written.size();
		VideoAudioPlayerTest.push(player, 1, 1599);
		player.update(0D);
		Assert.assertEquals(written, source.written.size());
		VideoAudioPlayerTest.push(player, 1, 1);
		player.update(0D);
		Assert.assertEquals(written + 1, source.written.size());
	}

	@Test
	public void keepsPackedShortSamples() {
		final VideoAudioPlayer player = this.started();
		player.pushSamples(new Buffer[] {ShortBuffer.wrap(new short[] {7, 8, 9, 10})}, 0);
		VideoAudioPlayerTest.push(player, 1, 1600);
		player.update(0D);
		Assert.assertArrayEquals(new short[] {7, 8, 9, 10}, Arrays.copyOf(this.audio.source().written.get(0), 4));
	}

	@Test
	public void skipsWholeFramesOfInterleavedSamples() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 2);
		player.play();
		player.update(0D);
		player.pushSamples(new Buffer[] {ShortBuffer.wrap(new short[] {1, 2, 3, 4})}, 1);
		VideoAudioPlayerTest.push(player, 1, 3200);
		player.update(0D);
		Assert.assertArrayEquals(new short[] {3, 4, 0, 1}, Arrays.copyOf(this.audio.source().written.get(0), 4));
	}

	@Test
	public void dropsAChunkThatEndsBeforeTheStart() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		player.pushSamples(new Buffer[] {ShortBuffer.wrap(new short[] {1, 2, 3})}, 3);
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
		player.pushSamples(new Buffer[] {ShortBuffer.wrap(new short[] {1, 0})}, 1);
		Assert.assertEquals(1, VideoAudioPlayerTest.queueSize(player));
	}

	@Test
	public void ignoresEmptyOrUnknownSamples() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		player.pushSamples(new Buffer[0], 0);
		player.pushSamples(new Buffer[] {null}, 0);
		player.pushSamples(new Buffer[] {ByteBuffer.allocate(4)}, 0);
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
	}

	@Test
	public void dropsSamplesBeyondItsQueue() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		VideoAudioPlayerTest.push(player, 200, 10);
		Assert.assertEquals(128, VideoAudioPlayerTest.queueSize(player));
	}

	@Test
	public void hearsFromTheOriginWithoutListener() {
		final VideoAudioPlayer player = this.queued();
		this.video.location(0F, 0F, 27.5F);
		player.update(0D);
		Assert.assertEquals(0.3F * 0.25F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void pausesFarFromTheOriginWithoutListener() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		this.video.location(100F, 0F, 0F);
		VideoAudioPlayerTest.push(player, 3, 10);
		source.calls.clear();
		player.update(0D);
		Assert.assertEquals(Collections.singletonList("pause"), source.calls);
	}

	@Test
	public void playsAtFullVolumeNearTheListener() {
		final VideoAudioPlayer player = this.queued();
		VideoAudioPlayer.audioListenerPosition(() -> Point.create(3D, 4D, 0D));
		this.video.location(0F, 0F, 0F);
		player.update(0D);
		Assert.assertEquals(0.3F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void fadesWithTheDistanceToTheListener() {
		final VideoAudioPlayer player = this.queued();
		VideoAudioPlayer.audioListenerPosition(() -> Point.create(0D, 0D, 27.5D));
		this.video.location(0F, 0F, 0F);
		player.update(0D);
		Assert.assertEquals(0.3F * 0.25F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void fadesBetweenItsOwnDistances() {
		final VideoAudioPlayer player = this.queued();
		VideoAudioPlayer.audioListenerPosition(() -> Point.create(1D, 2D, 9D));
		this.video.location(1F, 2F, 3F);
		this.video.referenceDistance(2F);
		this.video.maxDistance(10F);
		player.update(0D);
		Assert.assertEquals(0.3F * 0.25F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void pausesBeyondItsMaximumDistance() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		VideoAudioPlayer.audioListenerPosition(() -> Point.create(0D, 60D, 0D));
		this.video.location(0F, 0F, 0F);
		VideoAudioPlayerTest.push(player, 3, 10);
		source.calls.clear();
		player.update(0D);
		Assert.assertEquals(Collections.singletonList("pause"), source.calls);
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
		Assert.assertEquals(0.3F, source.lastGain(), 1E-6F);
	}

	@Test
	public void deletesItsSourceOnCleanup() {
		final VideoAudioPlayer player = this.queued();
		final RecordingAudioSource source = this.audio.source();
		VideoAudioPlayerTest.push(player, 3, 10);
		source.calls.clear();
		player.cleanup();
		player.update(0D);
		Assert.assertEquals(Arrays.asList("stop", "delete"), source.calls);
		Assert.assertEquals(0, VideoAudioPlayerTest.queueSize(player));
		player.play();
		Assert.assertEquals(Arrays.asList("8000x1", "8000x1"), this.audio.created);
	}

	public static int queueSize(final VideoAudioPlayer player) {
		try {
			final Field field = VideoAudioPlayer.class.getDeclaredField("sampleQueue");
			field.setAccessible(true);
			return ((ArrayBlockingQueue<?>) field.get(player)).size();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private VideoAudioPlayer started() {
		final VideoAudioPlayer player = VideoAudioPlayer.create(this.video, 8000, 1);
		player.play();
		player.update(0D);
		return player;
	}

	private VideoAudioPlayer queued() {
		final VideoAudioPlayer player = this.started();
		VideoAudioPlayerTest.push(player, 16, 4096);
		player.update(0D);
		return player;
	}

	private static VideoResourceDecoder video(final String name) {
		return VideoResourceDecoder.create(new Asset(name) {

			@Override
			public @NonNull InputStream open() {
				return new ByteArrayInputStream(new byte[0]);
			}

		});
	}

	private static void push(final VideoAudioPlayer player, final int count, final int length) {
		for (int i = 0; i < count; i++) {
			final short[] samples = new short[length];
			for (int j = 0; j < length; j++) {
				samples[j] = (short) (i * length + j);
			}
			player.pushSamples(new Buffer[] {ShortBuffer.wrap(samples)}, 0);
		}
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

	private static final class RecordingAudioBridge implements IAudioBridge {

		private final List<String>               created = new ArrayList<>();
		private final List<RecordingAudioSource> sources = new ArrayList<>();

		@Override
		public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
			final RecordingAudioSource source = new RecordingAudioSource(channels);
			this.created.add(sampleRate + "x" + channels);
			this.sources.add(source);
			return source;
		}

		private RecordingAudioSource source() {
			return this.sources.get(this.sources.size() - 1);
		}

	}

	private static final class RecordingAudioSource implements IAudioSource {

		private final List<Float>   gains   = new ArrayList<>();
		private final List<String>  calls   = new ArrayList<>();
		private final List<short[]> written = new ArrayList<>();

		private final int channels;

		private int     played;
		private Object  group;
		private boolean playing;

		private RecordingAudioSource(final int channels) {
			this.channels = channels;
		}

		@Override
		public void play() {
			this.calls.add("play");
			this.playing = true;
		}

		@Override
		public void stop() {
			this.calls.add("stop");
			this.playing = false;
			this.played = this.getWrittenSamples();
		}

		@Override
		public void pause() {
			this.calls.add("pause");
			this.playing = false;
		}

		@Override
		public void gain(final float gain) {
			this.gains.add(gain);
		}

		@Override
		public void group(final Object group) {
			this.group = group;
		}

		@Override
		public void write(final @NonNull short[] samples) {
			this.written.add(samples);
		}

		@Override
		public boolean isPlaying() {
			return this.playing;
		}

		@Override
		public int getBufferedSamples() {
			return this.getWrittenSamples() - this.played;
		}

		@Override
		public void delete() {
			this.calls.add("delete");
		}

		private float lastGain() {
			return this.gains.get(this.gains.size() - 1);
		}

		private int getWrittenSamples() {
			int samples = 0;
			for (final short[] chunk : this.written) {
				samples += chunk.length / this.channels * Math.min(this.channels, 2);
			}
			return samples;
		}

	}

}