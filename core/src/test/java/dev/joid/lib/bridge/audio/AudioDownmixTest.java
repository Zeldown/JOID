package dev.joid.lib.bridge.audio;

import org.junit.Assert;
import org.junit.Test;

public class AudioDownmixTest {

	@Test
	public void keepsAMonoOrStereoTrackAsIs() {
		final short[] samples = {1, 2, 3, 4};
		Assert.assertSame(samples, AudioDownmix.stereo(samples, 1));
		Assert.assertSame(samples, AudioDownmix.stereo(samples, 2));
	}

	@Test
	public void mixesAFivePointOneTrackDownToStereo() {
		final short[] samples = {
				1000, 0, 0, 0, 0, 0,
				0, 0, 1000, 0, 0, 0,
				0, 0, 0, 1000, 0, 0,
				0, 0, 0, 0, 0, -1000
		};
		Assert.assertArrayEquals(new short[] {414, 0, 293, 293, 0, 0, 0, -293}, AudioDownmix.stereo(samples, 6));
	}

	@Test
	public void mixesAFourChannelTrackDownToStereo() {
		final short[] samples = {
				0, 1000, 0, 0,
				0, 0, 0, 1000
		};
		Assert.assertArrayEquals(new short[] {0, 453, 227, 227}, AudioDownmix.stereo(samples, 4));
	}

	@Test
	public void neverClipsAFullScaleTrack() {
		final short[] samples = {32767, 32767, 32767, 32767, 32767, 32767, -32768, -32768, -32768, -32768, -32768, -32768};
		Assert.assertArrayEquals(new short[] {32767, 32767, -32768, -32768}, AudioDownmix.stereo(samples, 6));
	}

	@Test
	public void silencesTheChannelsBeyondSevenPointOne() {
		final short[] samples = {0, 0, 0, 0, 0, 0, 1000, 0, 1000, 1000};
		Assert.assertArrayEquals(new short[] {227, 0}, AudioDownmix.stereo(samples, 10));
	}

	@Test
	public void dropsAnIncompleteFrame() {
		Assert.assertArrayEquals(new short[] {0, 453}, AudioDownmix.stereo(new short[] {0, 1000, 0, 0, 5, 5}, 4));
	}

}