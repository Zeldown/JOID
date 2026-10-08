package dev.joid.base.opengl.snapshot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.utils.image.PixelLayout;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlSnapshotCapture {

	public static @NonNull SnapshotImage capture(final @NonNull GlRenderBridge bridge, final int width, final int height) {
		final ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		bridge.getFrameBufferBinding().bindFramebuffer(GlConstants.FRAMEBUFFER, 0);
		bridge.getBinding().readBuffer(GlConstants.BACK);
		bridge.getBinding().readPixels(0, 0, width, height, GlConstants.RGBA, GlConstants.UNSIGNED_BYTE, pixels);
		return SnapshotImage.fromBytes(pixels, width, height, true, PixelLayout.RGBA8);
	}

	public static @NonNull String getRenderer(final @NonNull IGlBinding binding) {
		return binding.getString(GlConstants.RENDERER);
	}

}