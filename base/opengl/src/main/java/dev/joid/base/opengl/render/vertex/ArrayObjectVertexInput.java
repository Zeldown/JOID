package dev.joid.base.opengl.render.vertex;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ArrayObjectVertexInput extends GlVertexInput {

	private final int array;

	private ArrayObjectVertexInput(final IGlBinding binding) {
		super(binding);
		this.array = binding.genVertexArray();
		binding.bindVertexArray(this.array);
		super.pointAttributes();
	}

	public static @NonNull ArrayObjectVertexInput create(final @NonNull IGlBinding binding) {
		return new ArrayObjectVertexInput(binding);
	}

	@Override
	public void bind() {
		super.getBinding().bindVertexArray(this.array);
		super.getBinding().bindBuffer(GlConstants.ARRAY_BUFFER, super.getBuffer());
	}

}