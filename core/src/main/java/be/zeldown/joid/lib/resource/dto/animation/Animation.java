package be.zeldown.joid.lib.resource.dto.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Animation {

	private static final long MIN_DURATION = 10L;
	private static final long DEFAULT_DURATION = 100L;

	private final int                  width;
	private final int                  height;
	private final int                  plays;
	private final List<AnimationFrame> frames;
	private final long[]               ends;

	private Animation(final int width, final int height, final int plays, final @NonNull List<AnimationFrame> frames) {
		this.width = width;
		this.height = height;
		this.plays = plays;
		this.frames = Collections.unmodifiableList(new ArrayList<>(frames));
		this.ends = new long[frames.size()];
		long end = 0L;
		for (int i = 0; i < this.ends.length; i++) {
			end += frames.get(i).getDuration() < Animation.MIN_DURATION ? Animation.DEFAULT_DURATION : frames.get(i).getDuration();
			this.ends[i] = end;
		}
	}

	public static @NonNull Animation create(final int width, final int height, final int plays, final @NonNull List<AnimationFrame> frames) {
		if (frames.isEmpty()) {
			throw new IllegalArgumentException("An animation needs at least one frame");
		}
		return new Animation(width, height, plays, frames);
	}

	public long getDuration() {
		return this.ends[this.ends.length - 1];
	}

	public int indexAt(final long time) {
		final int index = Arrays.binarySearch(this.ends, Math.max(0L, time) + 1L);
		return Math.min(this.ends.length - 1, index >= 0 ? index : -index - 1);
	}

}