package dev.joid.lib.resource.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceAnimation {

	private final int                  width;
	private final int                  height;
	private final int                  plays;
	private final List<ResourceAnimationFrame> frames;
	private final long[]               ends;

	private ResourceAnimation(final int width, final int height, final int plays, final @NonNull List<ResourceAnimationFrame> frames) {
		this.width = width;
		this.height = height;
		this.plays = plays;
		this.frames = Collections.unmodifiableList(new ArrayList<>(frames));
		this.ends = new long[frames.size()];
		long end = 0L;
		for (int i = 0; i < this.ends.length; i++) {
			end += frames.get(i).getDuration() < 10L ? 100L : frames.get(i).getDuration();
			this.ends[i] = end;
		}
	}

	public static @NonNull ResourceAnimation create(final int width, final int height, final int plays, final @NonNull List<ResourceAnimationFrame> frames) {
		if (frames.isEmpty()) {
			throw new IllegalArgumentException("An animation needs at least one frame");
		}
		return new ResourceAnimation(width, height, plays, frames);
	}

	public long getDuration() {
		return this.ends[this.ends.length - 1];
	}

	public int indexAt(final long time) {
		final int index = Arrays.binarySearch(this.ends, Math.max(0L, time) + 1L);
		return Math.min(this.ends.length - 1, index >= 0 ? index : -index - 1);
	}

}