package dev.joid.base.opengl.render.vertex;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.render.GlEnums;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class GlVertexInput {

	private final IGlBinding binding;
	private final int        buffer;

	protected GlVertexInput(final @NonNull IGlBinding binding) {
		this.binding = binding;
		this.buffer  = binding.getBufferBinding().genBuffer();
	}

	public abstract void bind();

	protected final void pointAttributes() {
		final IGlBufferBinding vertices = this.binding.getBufferBinding();
		vertices.bindBuffer(GlConstants.ARRAY_BUFFER, this.buffer);
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			vertices.vertexAttribPointer(attribute.getLocation(), attribute.getComponents(), GlEnums.type(attribute.getComponent()), attribute.isNormalized(), VertexBuffer.STRIDE, attribute.getOffset());
		}
		vertices.enableVertexAttribArray(VertexAttribute.POSITION.getLocation());
	}

}