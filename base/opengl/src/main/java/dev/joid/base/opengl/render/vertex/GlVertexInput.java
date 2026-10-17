package dev.joid.base.opengl.render.vertex;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlEnums;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlVertexInput {

	private final int        array;
	private final int        buffer;
	private final IGlBinding binding;

	private GlVertexInput(final IGlBinding binding, final boolean vertexArray) {
		this.binding = binding;
		this.buffer  = binding.genBuffer();
		this.array   = vertexArray ? binding.genVertexArray() : 0;
		if (vertexArray) {
			binding.bindVertexArray(this.array);
			this.pointAttributes();
		}
	}

	public static @NonNull GlVertexInput create(final IGlBinding binding, final boolean vertexArray) {
		return new GlVertexInput(binding, vertexArray);
	}

	public void bind() {
		if (this.array == 0) {
			this.pointAttributes();
			return;
		}

		this.binding.bindVertexArray(this.array);
		this.binding.bindBuffer(GlConstants.ARRAY_BUFFER, this.buffer);
	}

	private void pointAttributes() {
		this.binding.bindBuffer(GlConstants.ARRAY_BUFFER, this.buffer);
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			this.binding.vertexAttribPointer(attribute.getLocation(), attribute.getComponents(), GlEnums.type(attribute.getComponent()), attribute.isNormalized(), VertexBuffer.STRIDE, attribute.getOffset());
		}
		this.binding.enableVertexAttribArray(VertexAttribute.POSITION.getLocation());
	}

}