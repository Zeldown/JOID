package dev.joid.lib.bridge.render.texture;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MipmapChain {

	private final int width;
	private final int height;
	private final int levels;

	public static @NonNull MipmapChain of(final int width, final int height, final boolean mipmapped) {
		return new MipmapChain(width, height, mipmapped ? 32 - Integer.numberOfLeadingZeros(Math.max(1, Math.max(width, height))) : 1);
	}

	public @NonNull MipmapChain limit(final int levels) {
		return levels >= this.levels ? this : new MipmapChain(this.width, this.height, Math.max(1, levels));
	}

	public int getWidth(final int level) {
		return Math.max(1, this.width >> level);
	}

	public int getHeight(final int level) {
		return Math.max(1, this.height >> level);
	}

	public void forEachStep(final @NonNull IMipmapStep step) {
		for (int level = 1; level < this.levels; level++) {
			step.copy(level, this.getWidth(level - 1), this.getHeight(level - 1), this.getWidth(level), this.getHeight(level));
		}
	}

}