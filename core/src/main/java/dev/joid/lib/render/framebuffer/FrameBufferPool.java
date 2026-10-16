package dev.joid.lib.render.framebuffer;

import java.util.HashMap;
import java.util.Iterator;
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

	private static final Map<Object, PooledFrameBuffers> POOL = new HashMap<>();

	public static @NonNull IFrameBuffer[] allocate(final @NonNull Object owner, final int count, final int width, final int height) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final PooledFrameBuffers pooled = FrameBufferPool.POOL.get(owner);
		final boolean owned = pooled != null;
		if (owned && pooled.frameBuffers[0].getWidth() >= width && pooled.frameBuffers[0].getHeight() >= height) {
			pooled.lastUse = now;
			return pooled.frameBuffers;
		}

		final int targetWidth = FrameBufferPool.capacity(owned ? pooled.frameBuffers[0].getWidth() : 0, width);
		final int targetHeight = FrameBufferPool.capacity(owned ? pooled.frameBuffers[0].getHeight() : 0, height);
		if (owned) {
			pooled.delete();
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer[] frameBuffers = new IFrameBuffer[count];
		for (int i = 0; i < count; i++) {
			frameBuffers[i] = render.createFrameBuffer(targetWidth, targetHeight);
		}

		FrameBufferPool.POOL.put(owner, new PooledFrameBuffers(frameBuffers, now));
		return frameBuffers;
	}

	public static void cleanup() {
		for (final PooledFrameBuffers pooled : FrameBufferPool.POOL.values()) {
			pooled.delete();
		}
		FrameBufferPool.POOL.clear();
	}

	public static void releaseUnused() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final Iterator<PooledFrameBuffers> iterator = FrameBufferPool.POOL.values().iterator();
		while (iterator.hasNext()) {
			final PooledFrameBuffers pooled = iterator.next();
			if (now - pooled.lastUse >= 5000L) {
				pooled.delete();
				iterator.remove();
			}
		}
	}

	private static int capacity(final int current, final int required) {
		return current >= required ? current : Math.max(required, Math.min(4096, current * 2));
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class PooledFrameBuffers {

		private final IFrameBuffer[] frameBuffers;

		private long lastUse;

		private void delete() {
			for (final IFrameBuffer frameBuffer : this.frameBuffers) {
				frameBuffer.delete();
			}
		}

	}

}