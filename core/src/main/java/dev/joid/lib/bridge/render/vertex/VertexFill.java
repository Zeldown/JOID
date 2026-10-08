package dev.joid.lib.bridge.render.vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import dev.joid.lib.bridge.render.state.RenderState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VertexFill {

	public static @NonNull ByteBuffer complete(final @NonNull VertexBuffer buffer, final @NonNull ByteBuffer target, final @NonNull RenderState state) {
		final int size = buffer.getCount() * VertexBuffer.STRIDE;
		final ByteBuffer data = target.duplicate().order(ByteOrder.nativeOrder());
		final ByteBuffer source = buffer.getBuffer().duplicate();
		source.limit(source.position() + size);
		data.put(source);
		if (buffer.isTexture() && buffer.isColor() && buffer.isNormal()) {
			return target;
		}

		final byte[] color = {VertexFill.toByte(state.getRed()), VertexFill.toByte(state.getGreen()), VertexFill.toByte(state.getBlue()), VertexFill.toByte(state.getAlpha())};
		final byte[] normal = {0, 0, 127};
		final int start = target.position();
		for (int vertex = start; vertex < start + size; vertex += VertexBuffer.STRIDE) {
			if (!buffer.isTexture()) {
				data.putFloat(vertex + VertexAttribute.TEXTURE_COORDINATE.getOffset(), 0F);
				data.putFloat(vertex + VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4, 0F);
			}

			if (!buffer.isColor()) {
				VertexFill.put(data, vertex + VertexAttribute.COLOR.getOffset(), color);
			}

			if (!buffer.isNormal()) {
				VertexFill.put(data, vertex + VertexAttribute.NORMAL.getOffset(), normal);
			}
		}
		return target;
	}

	private static void put(final ByteBuffer data, final int offset, final byte[] values) {
		for (int i = 0; i < values.length; i++) {
			data.put(offset + i, values[i]);
		}
	}

	private static byte toByte(final float value) {
		return (byte) Math.round(Math.max(0F, Math.min(1F, value)) * 255F);
	}

}