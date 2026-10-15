package dev.joid.test.contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.function.Consumer;

import org.junit.Assert;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContractDraw {

	public static @NonNull SnapshotImage frame(final @NonNull ISnapshotBackend backend, final int width, final int height, final @NonNull Consumer<IRenderBridge> draw) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.beginFrame();
		try {
			render.clearColor(0F, 0F, 0F, 1F);
			draw.accept(render);
		} finally {
			render.endFrame();
		}

		final SnapshotImage image = backend.capture(width, height);
		backend.present();
		return image;
	}

	public static void assertPixel(final @NonNull SnapshotImage image, final int x, final int y, final int expected) {
		final int actual = image.getPixels()[x + y * image.getWidth()];
		Assert.assertEquals("Pixel " + x + "," + y, String.format(Locale.ROOT, "#%08X", expected), String.format(Locale.ROOT, "#%08X", actual));
	}

	public static @NonNull VertexBuffer quad(final float x, final float y, final float width, final float height, final float z, final boolean texture, final int color, final boolean normal, final float start, final float end) {
		final float[][] corners = {{x, y, start, start}, {x + width, y, end, start}, {x + width, y + height, end, end}, {x, y, start, start}, {x + width, y + height, end, end}, {x, y + height, start, end}};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(corners.length * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		for (int i = 0; i < corners.length; i++) {
			final int offset = i * VertexBuffer.STRIDE;
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset(), corners[i][0]);
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset() + 4, corners[i][1]);
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset() + 8, z);
			buffer.putFloat(offset + VertexAttribute.TEXTURE_COORDINATE.getOffset(), corners[i][2]);
			buffer.putFloat(offset + VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4, corners[i][3]);
			buffer.put(offset + VertexAttribute.COLOR.getOffset(), (byte) (color >> 16));
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 1, (byte) (color >> 8));
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 2, (byte) color);
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 3, (byte) (color >> 24));
			buffer.put(offset + VertexAttribute.NORMAL.getOffset() + 2, (byte) 127);
		}
		return VertexBuffer.create(buffer, corners.length, texture, color != 0, normal);
	}

}