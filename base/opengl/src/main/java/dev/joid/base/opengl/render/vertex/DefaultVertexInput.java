package dev.joid.base.opengl.render.vertex;

import dev.joid.base.opengl.binding.IGlBinding;
import lombok.NonNull;

public final class DefaultVertexInput extends GlVertexInput {

	private DefaultVertexInput(final IGlBinding binding) {
		super(binding);
	}

	public static @NonNull DefaultVertexInput create(final @NonNull IGlBinding binding) {
		return new DefaultVertexInput(binding);
	}

	@Override
	public void bind() {
		super.pointAttributes();
	}

}