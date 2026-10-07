package dev.joid.lib.video;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3f;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public class VideoAudioPlayerTest {

	private final RecordingAudioBridge audio = new RecordingAudioBridge();

	@Before
	public void recordTheAudio() {
		BridgeHandler.AUDIO.register(this.audio);
	}

	@After
	public void forgetTheAudio() {
		BridgeHandler.AUDIO.unregister(this.audio);
		VideoAudioPlayer.setAudioListener(null);
	}

	@Test
	public void createsItsSourceOnceOnPlay() {
		final VideoAudioPlayer player = new VideoAudioPlayer(44100, 2);
		player.play();
		player.play();
		Assert.assertEquals(Collections.singletonList("44100x2"), this.audio.created);
	}

	@Test
	public void staysSilentWithoutAudioBridge() {
		BridgeHandler.AUDIO.unregister(this.audio);
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		final String error = VideoAudioPlayerTest.capture(player::play);
		VideoAudioPlayerTest.push(player, 16, 1024);
		player.update();
		BridgeHandler.AUDIO.register(this.audio);
		Assert.assertTrue(error, error.contains("No audio bridge registered"));
		Assert.assertEquals(16, player.getQueueSize());
		Assert.assertTrue(this.audio.created.isEmpty());
	}

	@Test
	public void waitsUntilItPlays() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		VideoAudioPlayerTest.push(player, 16, 1024);
		player.update();
		Assert.assertEquals(16, player.getQueueSize());
		Assert.assertTrue(this.audio.created.isEmpty());
	}

	@Test
	public void dropsTheSamplesPushedBeforeItPlays() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		VideoAudioPlayerTest.push(player, 20, 1024);
		player.play();
		player.update();
		Assert.assertEquals(0, player.getQueueSize());
		Assert.assertTrue(this.audio.source().queued.isEmpty());
	}

	@Test
	public void waitsForSixteenChunksBeforeQueueing() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		VideoAudioPlayerTest.push(player, 15, 1024);
		player.update();
		final RecordingAudioSource source = this.audio.source();
		Assert.assertTrue(source.queued.isEmpty());
		Assert.assertFalse(source.playing);
		VideoAudioPlayerTest.push(player, 1, 1024);
		player.update();
		Assert.assertEquals(4, source.queued.size());
		Assert.assertEquals(4096, source.queued.get(0).length);
		Assert.assertEquals(Collections.singletonList(0.3F), source.gains);
		Assert.assertTrue(source.playing);
		Assert.assertEquals(0, player.getQueueSize());
	}

	@Test
	public void splitsLargeSamplesAcrossChunksInOrder() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		VideoAudioPlayerTest.push(player, 16, 3000);
		player.update();
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals(8, source.queued.size());
		source.processed = 5;
		player.update();
		Assert.assertEquals(12, source.queued.size());
		Assert.assertEquals(2944, source.queued.get(11).length);
		int expected = 0;
		for (final short[] chunk : source.queued) {
			for (final short sample : chunk) {
				Assert.assertEquals((short) expected++, sample);
			}
		}
		Assert.assertEquals(16 * 3000, expected);
	}

	@Test
	public void cutsItsChunksBetweenTwoFramesOfAMultichannelTrack() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 6);
		player.play();
		player.update();
		VideoAudioPlayerTest.push(player, 16, 3000);
		player.update();
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals(8, source.queued.size());
		for (final short[] chunk : source.queued) {
			Assert.assertEquals(4092, chunk.length);
		}
	}

	@Test
	public void trimsItsLastChunk() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		VideoAudioPlayerTest.push(player, 16, 100);
		player.update();
		final RecordingAudioSource source = this.audio.source();
		Assert.assertEquals(1, source.queued.size());
		Assert.assertEquals(1600, source.queued.get(0).length);
	}

	@Test
	public void restartsASourceThatRanDry() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		final RecordingAudioSource source = this.audio.source();
		source.playing = false;
		source.calls.clear();
		player.update();
		Assert.assertEquals(Collections.singletonList("play"), source.calls);
		player.update();
		Assert.assertEquals(Collections.singletonList("play"), source.calls);
	}

	@Test
	public void appliesItsVolumeToTheGain() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		player.setVolume(0.5F);
		player.update();
		Assert.assertEquals(0.15F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void pausesAndResumesItsSource() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		final RecordingAudioSource source = this.audio.source();
		source.calls.clear();
		source.gains.clear();
		player.pause();
		player.update();
		Assert.assertEquals(Collections.singletonList("pause"), source.calls);
		Assert.assertTrue(source.gains.isEmpty());
		player.resume();
		player.update();
		Assert.assertEquals(Arrays.asList("pause", "play"), source.calls);
		Assert.assertEquals(Collections.singletonList(0.3F), source.gains);
	}

	@Test
	public void stopsItsSource() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		final RecordingAudioSource source = this.audio.source();
		source.calls.clear();
		source.gains.clear();
		player.stop();
		player.update();
		Assert.assertEquals(Collections.singletonList("stop"), source.calls);
		Assert.assertTrue(source.gains.isEmpty());
	}

	@Test
	public void controlsNothingBeforeItPlays() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		VideoAudioPlayerTest.push(player, 2, 10);
		player.pause();
		player.resume();
		player.stop();
		player.flush();
		player.cleanup();
		Assert.assertEquals(0, player.getQueueSize());
		Assert.assertTrue(this.audio.created.isEmpty());
	}

	@Test
	public void flushesItsSamplesAndItsSource() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		final RecordingAudioSource source = this.audio.source();
		VideoAudioPlayerTest.push(player, 15, 1024);
		player.flush();
		Assert.assertEquals(0, player.getQueueSize());
		Assert.assertTrue(source.calls.contains("clear"));
		source.queued.clear();
		VideoAudioPlayerTest.push(player, 15, 1024);
		player.update();
		Assert.assertTrue(source.queued.isEmpty());
		VideoAudioPlayerTest.push(player, 1, 1024);
		player.update();
		Assert.assertEquals(4, source.queued.size());
	}

	@Test
	public void convertsFloatSamplesToInterleavedShorts() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		player.pushSamples(new Buffer[] {FloatBuffer.wrap(new float[] {1F, -1F, 0.5F}), FloatBuffer.wrap(new float[] {2F, -2F, 0F})});
		VideoAudioPlayerTest.push(player, 15, 10);
		player.update();
		final short[] chunk = this.audio.source().queued.get(0);
		Assert.assertArrayEquals(new short[] {19660, 19660, -19660, -19660, 9830, 0}, Arrays.copyOf(chunk, 6));
	}

	@Test
	public void interleavesPlanarShortSamples() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		player.pushSamples(new Buffer[] {ShortBuffer.wrap(new short[] {1, 2, 3}), ShortBuffer.wrap(new short[] {4, 5, 6})});
		VideoAudioPlayerTest.push(player, 15, 10);
		player.update();
		Assert.assertArrayEquals(new short[] {1, 4, 2, 5, 3, 6}, Arrays.copyOf(this.audio.source().queued.get(0), 6));
	}

	@Test
	public void keepsPackedShortSamples() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		player.pushSamples(new Buffer[] {ShortBuffer.wrap(new short[] {7, 8, 9, 10})});
		VideoAudioPlayerTest.push(player, 15, 10);
		player.update();
		Assert.assertArrayEquals(new short[] {7, 8, 9, 10}, Arrays.copyOf(this.audio.source().queued.get(0), 4));
	}

	@Test
	public void ignoresEmptyOrUnknownSamples() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		player.pushSamples(new Buffer[0]);
		player.pushSamples(new Buffer[] {null});
		player.pushSamples(new Buffer[] {ByteBuffer.allocate(4)});
		Assert.assertEquals(0, player.getQueueSize());
	}

	@Test
	public void dropsSamplesBeyondItsQueue() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		VideoAudioPlayerTest.push(player, 200, 10);
		Assert.assertEquals(128, player.getQueueSize());
	}

	@Test
	public void sharesOneListener() {
		final AudioListener listener = () -> new Vector3f(1F, 2F, 3F);
		VideoAudioPlayer.setAudioListener(listener);
		Assert.assertSame(listener, VideoAudioPlayer.getAudioListener());
	}

	@Test
	public void ignoresTheDistanceWithoutListener() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		player.setLocation(100F, 0F, 0F);
		player.update();
		Assert.assertEquals(0.3F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void playsAtFullVolumeNearTheListener() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		VideoAudioPlayer.setAudioListener(() -> new Vector3f(3F, 4F, 0F));
		player.setLocation(0F, 0F, 0F);
		player.update();
		Assert.assertEquals(0.3F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void fadesWithTheDistanceToTheListener() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		VideoAudioPlayer.setAudioListener(() -> new Vector3f(0F, 0F, 27.5F));
		player.setLocation(0F, 0F, 0F);
		player.update();
		Assert.assertEquals(0.3F * 0.25F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void fadesBetweenItsOwnDistances() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		VideoAudioPlayer.setAudioListener(() -> new Vector3f(1F, 2F, 9F));
		player.setLocation(1F, 2F, 3F);
		player.setReferenceDistance(2F);
		player.setMaxDistance(10F);
		player.update();
		Assert.assertEquals(0.3F * 0.25F, this.audio.source().lastGain(), 1E-6F);
	}

	@Test
	public void pausesBeyondItsMaximumDistance() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		final RecordingAudioSource source = this.audio.source();
		VideoAudioPlayer.setAudioListener(() -> new Vector3f(0F, 60F, 0F));
		player.setLocation(0F, 0F, 0F);
		VideoAudioPlayerTest.push(player, 3, 10);
		source.calls.clear();
		player.update();
		Assert.assertEquals(Collections.singletonList("pause"), source.calls);
		Assert.assertEquals(0, player.getQueueSize());
		Assert.assertEquals(0.3F, source.lastGain(), 1E-6F);
	}

	@Test
	public void deletesItsSourceOnCleanup() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.queued();
		final RecordingAudioSource source = this.audio.source();
		VideoAudioPlayerTest.push(player, 3, 10);
		source.calls.clear();
		player.cleanup();
		player.update();
		Assert.assertEquals(Arrays.asList("stop", "delete"), source.calls);
		Assert.assertEquals(0, player.getQueueSize());
		player.play();
		Assert.assertEquals(Arrays.asList("8000x1", "8000x1"), this.audio.created);
	}

	private static VideoAudioPlayer started() {
		final VideoAudioPlayer player = new VideoAudioPlayer(8000, 1);
		player.play();
		player.update();
		return player;
	}

	private static VideoAudioPlayer queued() {
		final VideoAudioPlayer player = VideoAudioPlayerTest.started();
		VideoAudioPlayerTest.push(player, 16, 4096);
		player.update();
		return player;
	}

	private static void push(final VideoAudioPlayer player, final int count, final int length) {
		for (int i = 0; i < count; i++) {
			final short[] samples = new short[length];
			for (int j = 0; j < length; j++) {
				samples[j] = (short) (i * length + j);
			}
			player.pushSamples(new Buffer[] {ShortBuffer.wrap(samples)});
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
			final RecordingAudioSource source = new RecordingAudioSource();
			this.created.add(sampleRate + "x" + channels);
			this.sources.add(source);
			return source;
		}

		private RecordingAudioSource source() {
			return this.sources.get(this.sources.size() - 1);
		}

	}

	private static final class RecordingAudioSource implements IAudioSource {

		private final List<String>  calls  = new ArrayList<>();
		private final List<Float>   gains  = new ArrayList<>();
		private final List<short[]> queued = new ArrayList<>();

		private boolean playing;
		private int     processed;

		@Override
		public void play() {
			this.calls.add("play");
			this.playing = true;
		}

		@Override
		public void stop() {
			this.calls.add("stop");
			this.playing = false;
		}

		@Override
		public void clear() {
			this.calls.add("clear");
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
		public void queue(final @NonNull short[] samples) {
			this.queued.add(samples);
		}

		@Override
		public boolean isPlaying() {
			return this.playing;
		}

		@Override
		public int getQueuedBuffers() {
			return this.queued.size();
		}

		@Override
		public int getProcessedBuffers() {
			final int processed = this.processed;
			this.processed = 0;
			return processed;
		}

		@Override
		public void delete() {
			this.calls.add("delete");
		}

		private float lastGain() {
			return this.gains.get(this.gains.size() - 1);
		}

	}

}