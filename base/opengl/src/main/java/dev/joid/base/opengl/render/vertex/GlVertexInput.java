package dev.joid.base.opengl.render.vertex;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlEnums;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class GlVertexInput {

	private final int        buffer;
	private final IGlBinding binding;

	protected GlVertexInput(final @NonNull IGlBinding binding) {
		this.binding = binding;
		this.buffer  = binding.genBuffer();
	}

	public abstract void bind();

	protected final void pointAttributes() {
		this.binding.bindBuffer(GlConstants.ARRAY_BUFFER, this.buffer);
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			this.binding.vertexAttribPointer(attribute.getLocation(), attribute.getComponents(), GlEnums.type(attribute.getComponent()), attribute.isNormalized(), VertexBuffer.STRIDE, attribute.getOffset());
		}
		this.binding.enableVertexAttribArray(VertexAttribute.POSITION.getLocation());
	}

}