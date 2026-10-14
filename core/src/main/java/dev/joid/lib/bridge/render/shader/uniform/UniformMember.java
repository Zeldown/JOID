package dev.joid.lib.bridge.render.shader.uniform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UniformMember {

	private final int         size;
	private final int         length;
	private final int         offset;
	private final String      name;
	private final int         arrayStride;
	private final int         matrixStride;
	private final UniformType type;
	private final ByteBuffer  values;

	private boolean dirty;

	private UniformMember(final String name, final UniformType type, final int length, final int start) {
		final int element = type.isMatrix() ? type.getColumns() * 16 : type.getComponents() * 4;
		final int alignment = length > 0 || type.isMatrix() || element > 8 ? 16 : element;

		this.name         = name;
		this.type         = type;
		this.length       = length;
		this.offset       = (start + alignment - 1) / alignment * alignment;
		this.arrayStride  = length > 0 ? (element + 15) / 16 * 16 : 0;
		this.matrixStride = type.isMatrix() ? 16 : 0;
		this.size         = length > 0 ? this.arrayStride * length : element;
		this.values       = ByteBuffer.allocateDirect(type.getCount() * Math.max(length, 1) * 4).order(ByteOrder.nativeOrder());
	}

	public static @NonNull UniformMember create(final @NonNull String name, final @NonNull UniformType type, final int length, final int start) {
		return new UniformMember(name, type, length, start);
	}

	public @NonNull UniformMember value(final boolean value) {
		if (this.type != UniformType.BOOL && this.type != UniformType.INT || this.length > 0) {
			throw this.refuse("a boolean");
		}

		this.put(0, value ? 1 : 0);
		return this;
	}

	public @NonNull UniformMember value(final @NonNull int... values) {
		if (!this.type.isInteger() || !this.fits(values.length)) {
			throw this.refuse(values.length + (values.length == 1 ? " int" : " ints"));
		}

		for (int i = 0; i < values.length; i++) {
			this.put(i, values[i]);
		}
		return this;
	}

	public @NonNull UniformMember value(final @NonNull float... values) {
		if (this.type.isInteger() || !this.fits(values.length)) {
			throw this.refuse(values.length + (values.length == 1 ? " float" : " floats"));
		}

		for (int i = 0; i < values.length; i++) {
			this.put(i, Float.floatToRawIntBits(values[i]));
		}
		return this;
	}

	public @NonNull UniformMember value(final @NonNull UniformMember source) {
		if (source.getType() != this.type || source.getLength() != this.length) {
			throw this.refuse("the values of " + source.getDeclaration());
		}

		for (int i = 0; i < this.values.capacity() / 4; i++) {
			this.put(i, source.getValues().getInt(i * 4));
		}
		return this;
	}

	public @NonNull UniformMember clean() {
		this.dirty = false;
		return this;
	}

	public @NonNull String getDeclaration() {
		return this.type.getIdentifier() + " " + this.name + (this.length > 0 ? "[" + this.length + "]" : "");
	}

	private boolean fits(final int count) {
		final int components = this.type.getCount();
		return this.length > 0 ? count > 0 && count % components == 0 && count <= components * this.length : count == components;
	}

	private void put(final int index, final int bits) {
		if (this.values.getInt(index * 4) != bits) {
			this.values.putInt(index * 4, bits);
			this.dirty = true;
		}
	}

	private IllegalArgumentException refuse(final String value) {
		return new IllegalArgumentException("The uniform " + this.getDeclaration() + " cannot take " + value);
	}

}