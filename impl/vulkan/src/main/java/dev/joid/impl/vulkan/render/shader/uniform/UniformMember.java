package dev.joid.impl.vulkan.render.shader.uniform;

import java.nio.ByteBuffer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class UniformMember {

	private final ByteBuffer data;
	private final int        offset;
	private final int        arrayStride;
	private final int        matrixStride;

	public void putInt(final int value) {
		this.data.putInt(this.offset, value);
	}

	public void putFloats(final float... values) {
		for (int i = 0; i < values.length; i++) {
			this.data.putFloat(this.offset + i * 4, values[i]);
		}
	}

	public void putArray(final float[] values, final int components) {
		for (int i = 0; i < values.length; i++) {
			this.data.putFloat(this.offset + i / components * this.arrayStride + i % components * 4, values[i]);
		}
	}

	public void putMatrix(final float[] values) {
		final int size = (int) Math.round(Math.sqrt(values.length));
		if (size * size != values.length || size < 2 || size > 4) {
			throw new IllegalArgumentException("Invalid matrix size");
		}

		for (int column = 0; column < size; column++) {
			for (int row = 0; row < size; row++) {
				this.data.putFloat(this.offset + column * this.matrixStride + row * 4, values[column * size + row]);
			}
		}
	}

}