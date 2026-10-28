package dev.joid.lib.render.framebuffer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrameBufferPool {

	private static final Map<List<Object>, PooledFrameBuffers> POOL = new HashMap<>();

	public static @NonNull IFrameBuffer[] allocate(final @NonNull Object owner, final int depth, final int count, final int width, final int height) {
		final long now = BridgeHandler.CLOCK.get().nanoTime();
		final List<Object> key = Arrays.asList(owner, depth, count);
		final PooledFrameBuffers pooled = FrameBufferPool.POOL.get(key);
		final boolean owned = pooled != null;
		if (owned && pooled.frameBuffers[0].getWidth() >= width && pooled.frameBuffers[0].getHeight() >= height) {
			pooled.lastUse = now;
			pooled.peakWidth = Math.max(pooled.peakWidth, width);
			pooled.peakHeight = Math.max(pooled.peakHeight, height);
			return pooled.frameBuffers;
		}

		final int targetWidth = FrameBufferPool.capacity(owned ? pooled.frameBuffers[0].getWidth() : 0, width);
		final int targetHeight = FrameBufferPool.capacity(owned ? pooled.frameBuffers[0].getHeight() : 0, height);
		if (owned) {
			pooled.delete();
			FrameBufferPool.POOL.remove(key);
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer[] frameBuffers = new IFrameBuffer[count];
		for (int i = 0; i < count; i++) {
			frameBuffers[i] = render.createFrameBuffer(targetWidth, targetHeight);
		}

		FrameBufferPool.POOL.put(key, new PooledFrameBuffers(frameBuffers, now, now, width, height));
		return frameBuffers;
	}

	public static void cleanup() {
		for (final PooledFrameBuffers pooled : FrameBufferPool.POOL.values()) {
			pooled.delete();
		}
		FrameBufferPool.POOL.clear();
	}

	public static void releaseUnused() {
		final long now = BridgeHandler.CLOCK.get().nanoTime();
		final Iterator<PooledFrameBuffers> iterator = FrameBufferPool.POOL.values().iterator();
		while (iterator.hasNext()) {
			final PooledFrameBuffers pooled = iterator.next();
			final boolean elapsed = now - pooled.peakStart >= 5_000_000_000L;
			if (now - pooled.lastUse >= 5_000_000_000L || elapsed && (FrameBufferPool.capacity(0, pooled.peakWidth) < pooled.frameBuffers[0].getWidth() || FrameBufferPool.capacity(0, pooled.peakHeight) < pooled.frameBuffers[0].getHeight())) {
				pooled.delete();
				iterator.remove();
			} else if (elapsed) {
				pooled.peakStart = now;
				pooled.peakWidth = 0;
				pooled.peakHeight = 0;
			}
		}
	}

	private static int capacity(final int current, final int required) {
		return current >= required ? current : Math.max(1, Integer.highestOneBit(required - 1) << 1);
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class PooledFrameBuffers {

		private final IFrameBuffer[] frameBuffers;

		private long lastUse;
		private long peakStart;

		private int peakWidth;
		private int peakHeight;

		private void delete() {
			for (final IFrameBuffer frameBuffer : this.frameBuffers) {
				frameBuffer.delete();
			}
		}

	}

}