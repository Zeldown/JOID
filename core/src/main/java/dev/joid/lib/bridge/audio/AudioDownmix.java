package dev.joid.lib.bridge.audio;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AudioDownmix {

	public static @NonNull short[] stereo(final @NonNull short[] samples, final int channels) {
		if (channels <= 2) {
			return samples;
		}

		final float[][] weights = AudioDownmix.weights(channels);
		float leftSum = 0F;
		float rightSum = 0F;
		for (final float[] weight : weights) {
			leftSum += weight[0];
			rightSum += weight[1];
		}

		final float scale = 1F / Math.max(leftSum, rightSum);
		final int frames = samples.length / channels;
		final short[] stereo = new short[frames * 2];
		for (int frame = 0; frame < frames; frame++) {
			float left = 0F;
			float right = 0F;
			for (int channel = 0; channel < channels; channel++) {
				final float sample = samples[frame * channels + channel];
				left += sample * weights[channel][0];
				right += sample * weights[channel][1];
			}

			stereo[frame * 2] = (short) Math.max(-32768, Math.min(32767, Math.round(left * scale)));
			stereo[frame * 2 + 1] = (short) Math.max(-32768, Math.min(32767, Math.round(right * scale)));
		}
		return stereo;
	}

	private static @NonNull float[][] weights(final int channels) {
		final float half = 0.70710677F;
		final float[] left = {1F, 0F};
		final float[] right = {0F, 1F};
		final float[] center = {half, half};
		final float[] silent = {0F, 0F};
		final float[] back = {0.5F, 0.5F};
		final float[] surroundLeft = {half, 0F};
		final float[] surroundRight = {0F, half};

		switch (channels) {
		case 3:
			return new float[][] {left, right, center};
		case 4:
			return new float[][] {left, right, center, back};
		case 5:
			return new float[][] {left, right, center, surroundLeft, surroundRight};
		case 6:
			return new float[][] {left, right, center, silent, surroundLeft, surroundRight};
		case 7:
			return new float[][] {left, right, center, silent, back, surroundLeft, surroundRight};
		default:
			final float[][] weights = new float[channels][];
			Arrays.fill(weights, silent);
			System.arraycopy(new float[][] {left, right, center, silent, surroundLeft, surroundRight, surroundLeft, surroundRight}, 0, weights, 0, 8);
			return weights;
		}
	}

}