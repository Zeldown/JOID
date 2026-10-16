package dev.joid.base.opengl.render.vertex;

import dev.joid.base.opengl.binding.IGlBinding;
import lombok.NonNull;

public final class DefaultGlVertexInput extends GlVertexInput {

	private DefaultGlVertexInput(final IGlBinding binding) {
		super(binding);
	}

	public static @NonNull DefaultGlVertexInput create(final IGlBinding binding) {
		return new DefaultGlVertexInput(binding);
	}

	@Override
	public void bind() {
		super.pointAttributes();
	}

}