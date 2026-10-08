package dev.joid.base.openal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.base.openal.binding.IAlBinding;
import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public class AlAudioSourceTest {

	private final RecordingAlBinding binding = new RecordingAlBinding();

	@Test
	public void createsItsSourceOnTheCurrentContext() {
		final IAudioSource source = this.create(2);
		Assert.assertTrue(this.binding.calls.isEmpty());
		source.gain(0.5F);
		Assert.assertEquals(Arrays.asList("gen source 1", "gain 1 0.5", "gain 1 0.5"), this.binding.calls);
	}

	@Test
	public void writesEachArrayInABufferOfThePool() {
		final IAudioSource source = this.create(2);
		source.write(new short[] {1, 2, 3, 4});
		source.write(new short[] {5, 6});
		Assert.assertEquals(Arrays.asList(2, 3), new ArrayList<>(this.binding.queues.get(1)));
		Assert.assertEquals("data 2 2x4 8000", this.binding.calls.get(3));
		Assert.assertEquals("data 3 2x2 8000", this.binding.calls.get(5));
		Assert.assertEquals(6, source.getBufferedSamples());
	}

	@Test
	public void reusesThePlayedBuffers() {
		final IAudioSource source = this.create(1);
		source.write(new short[] {1, 2, 3});
		source.write(new short[] {4, 5});
		this.binding.finish(1, 1);
		Assert.assertEquals(2, source.getBufferedSamples());
		source.write(new short[] {6});
		Assert.assertEquals(Arrays.asList(3, 2), new ArrayList<>(this.binding.queues.get(1)));
		Assert.assertEquals(2, this.binding.calls.stream().filter(call -> call.startsWith("gen buffer")).count());
		Assert.assertEquals(3, source.getBufferedSamples());
	}

	@Test
	public void mixesManyChannelsDownToStereo() {
		this.create(6).write(new short[12]);
		Assert.assertEquals(4, this.binding.data.get(2).length);
		Assert.assertEquals("data 2 2x4 8000", this.binding.calls.get(3));
	}

	@Test
	public void playsAgainWhenItRanDry() {
		final IAudioSource source = this.create(1);
		source.write(new short[] {1});
		source.play();
		this.binding.runDry(1);
		Assert.assertTrue(source.isPlaying());
		Assert.assertEquals(0, source.getBufferedSamples());
		this.binding.calls.clear();
		source.write(new short[] {2});
		Assert.assertEquals(Arrays.asList("data 2 1x1 8000", "play 1"), this.binding.calls);
	}

	@Test
	public void waitsForPlayWhenSamplesArriveWhilePaused() {
		final IAudioSource source = this.create(1);
		source.play();
		source.pause();
		this.binding.calls.clear();
		source.write(new short[] {1});
		Assert.assertFalse(source.isPlaying());
		Assert.assertFalse(this.binding.calls.contains("play 1"));
		Assert.assertEquals(1, source.getBufferedSamples());
	}

	@Test
	public void dropsTheBufferedSamplesOnStop() {
		final IAudioSource source = this.create(1);
		source.write(new short[] {1, 2});
		source.write(new short[] {3});
		source.play();
		source.stop();
		Assert.assertFalse(source.isPlaying());
		Assert.assertEquals(0, source.getBufferedSamples());
		Assert.assertTrue(this.binding.queues.get(1).isEmpty());
		source.write(new short[] {4});
		Assert.assertEquals(2, this.binding.calls.stream().filter(call -> call.startsWith("gen buffer")).count());
	}

	@Test
	public void appliesTheGainOfTheHost() {
		final IAudioSource source = AlAudioSource.create(this.binding, gain -> gain * 0.5F, 8000, 1);
		source.gain(0.8F);
		Assert.assertEquals("gain 1 0.4", this.binding.calls.get(this.binding.calls.size() - 1));
	}

	@Test
	public void followsANewContextOfTheHost() {
		final IAudioSource source = this.create(1);
		source.gain(0.5F);
		source.write(new short[] {1, 2});
		this.binding.context = "second";
		Assert.assertEquals(0, source.getBufferedSamples());
		Assert.assertTrue(this.binding.calls.contains("gen source 3"));
		Assert.assertEquals("gain 3 0.5", this.binding.calls.get(this.binding.calls.size() - 1));
	}

	@Test
	public void staysSilentWithoutContext() {
		this.binding.context = null;
		final IAudioSource source = this.create(1);
		source.play();
		source.write(new short[] {1});
		source.gain(1F);
		source.pause();
		source.stop();
		Assert.assertFalse(source.isPlaying());
		Assert.assertEquals(0, source.getBufferedSamples());
		Assert.assertTrue(this.binding.calls.isEmpty());
	}

	@Test
	public void deletesItsSourceAndItsBuffers() {
		final IAudioSource source = this.create(1);
		source.write(new short[] {1});
		source.write(new short[] {2});
		this.binding.calls.clear();
		source.delete();
		Assert.assertEquals(Arrays.asList("stop 1", "delete source 1", "delete buffer 2", "delete buffer 3"), this.binding.calls);
	}

	@Test
	public void deletesNothingOnAnotherContext() {
		final IAudioSource source = this.create(1);
		source.write(new short[] {1});
		this.binding.context = "second";
		this.binding.calls.clear();
		source.delete();
		Assert.assertTrue(this.binding.calls.isEmpty());
	}

	@Test
	public void createsAContextOnlyWhenNoneIsCurrent() {
		AlAudioBridge.create(this.binding).createSource(8000, 1);
		Assert.assertTrue(this.binding.calls.isEmpty());
		this.binding.context = null;
		AlAudioBridge.create(this.binding).createSource(8000, 1);
		Assert.assertEquals(Collections.singletonList("create context"), this.binding.calls);
	}

	@Test
	public void givesTheGainOfTheHostToItsSources() {
		AlAudioBridge.create(this.binding).hostGain(gain -> 0F).createSource(8000, 1).gain(1F);
		Assert.assertEquals("gain 1 0.0", this.binding.calls.get(this.binding.calls.size() - 1));
	}

	private IAudioSource create(final int channels) {
		return AlAudioSource.create(this.binding, gain -> gain, 8000, channels);
	}

	private static final class RecordingAlBinding implements IAlBinding {

		private final List<String>                 calls   = new ArrayList<>();
		private final Map<Integer, short[]>        data    = new HashMap<>();
		private final Map<Integer, Integer>        played  = new HashMap<>();
		private final Map<Integer, Boolean>        playing = new HashMap<>();
		private final Map<Integer, Deque<Integer>> queues  = new HashMap<>();

		private int    names;
		private Object context = "first";

		private void finish(final int source, final int buffers) {
			this.played.merge(source, buffers, Integer::sum);
		}

		private void runDry(final int source) {
			this.played.put(source, this.queues.get(source).size());
			this.playing.put(source, false);
		}

		@Override
		public void createContext() {
			this.calls.add("create context");
			this.context = "created";
		}

		@Override
		public void destroyContext() {
			this.calls.add("destroy context");
		}

		@Override
		public Object getCurrentContext() {
			return this.context;
		}

		@Override
		public int genSource() {
			final int source = ++this.names;
			this.calls.add("gen source " + source);
			this.queues.put(source, new ArrayDeque<>());
			this.played.put(source, 0);
			this.playing.put(source, false);
			return source;
		}

		@Override
		public void deleteSource(final int source) {
			this.calls.add("delete source " + source);
		}

		@Override
		public int genBuffer() {
			final int buffer = ++this.names;
			this.calls.add("gen buffer " + buffer);
			return buffer;
		}

		@Override
		public void deleteBuffer(final int buffer) {
			this.calls.add("delete buffer " + buffer);
		}

		@Override
		public void bufferData(final int buffer, final int channels, final @NonNull short[] samples, final int sampleRate) {
			this.calls.add("data " + buffer + " " + channels + "x" + samples.length + " " + sampleRate);
			this.data.put(buffer, samples);
		}

		@Override
		public void play(final int source) {
			this.calls.add("play " + source);
			this.playing.put(source, true);
		}

		@Override
		public void stop(final int source) {
			this.calls.add("stop " + source);
			this.playing.put(source, false);
			this.played.put(source, this.queues.get(source).size());
		}

		@Override
		public void pause(final int source) {
			this.calls.add("pause " + source);
			this.playing.put(source, false);
		}

		@Override
		public void gain(final int source, final float gain) {
			this.calls.add("gain " + source + " " + gain);
		}

		@Override
		public int unqueueBuffer(final int source) {
			this.played.merge(source, -1, Integer::sum);
			return this.queues.get(source).poll();
		}

		@Override
		public void queueBuffer(final int source, final int buffer) {
			this.queues.get(source).add(buffer);
		}

		@Override
		public boolean isPlaying(final int source) {
			return this.playing.get(source);
		}

		@Override
		public int getQueuedBuffers(final int source) {
			return this.queues.get(source).size();
		}

		@Override
		public int getProcessedBuffers(final int source) {
			return this.played.get(source);
		}

	}

}