package dev.joid.impl.opengl.snapshot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import dev.joid.impl.opengl.binding.GlConstants;
import dev.joid.impl.opengl.binding.IGlBinding;
import dev.joid.lib.utils.image.PixelLayout;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlSnapshotCapture {

	public static @NonNull SnapshotImage capture(final @NonNull IGlBinding binding, final int width, final int height) {
		final ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		binding.getFrameBufferBinding().bindFramebuffer(GlConstants.FRAMEBUFFER, 0);
		binding.getFrameBufferBinding().readBuffer(GlConstants.BACK);
		binding.getFrameBufferBinding().readPixels(0, 0, width, height, GlConstants.RGBA, GlConstants.UNSIGNED_BYTE, pixels);
		return SnapshotImage.fromBytes(pixels, width, height, true, PixelLayout.RGBA8);
	}

	public static @NonNull String getRenderer(final @NonNull IGlBinding binding) {
		return binding.getString(GlConstants.RENDERER);
	}

}