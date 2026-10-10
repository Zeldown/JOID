package dev.joid.lib.bridge.render.vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VertexFill {

	public static @NonNull ByteBuffer complete(final @NonNull VertexBuffer buffer, final @NonNull ByteBuffer target) {
		final int size = buffer.getCount() * VertexBuffer.STRIDE;
		final ByteBuffer data = target.duplicate().order(ByteOrder.nativeOrder());
		final ByteBuffer source = buffer.getBuffer().duplicate();
		source.limit(source.position() + size);
		data.put(source);
		if (buffer.isTexture() && buffer.isNormal()) {
			return target;
		}

		final byte[] normal = {0, 0, 127};
		final int start = target.position();
		for (int vertex = start; vertex < start + size; vertex += VertexBuffer.STRIDE) {
			if (!buffer.isTexture()) {
				data.putFloat(vertex + VertexAttribute.TEXTURE_COORDINATE.getOffset(), 0F);
				data.putFloat(vertex + VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4, 0F);
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

}