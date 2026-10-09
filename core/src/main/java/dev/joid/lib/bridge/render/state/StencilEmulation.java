package dev.joid.lib.bridge.render.state;

import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StencilEmulation {

	private final boolean test;
	private final boolean write;

	private final int             mask;
	private final int             reference;
	private final StencilFunction function;

	private final StencilOperation fail;
	private final StencilOperation pass;

	public static @NonNull StencilEmulation create(final @NonNull RenderState state, final boolean screen) {
		final StencilState stencil = state.getStencil();
		final boolean test = screen && stencil.isEnabled();
		final boolean write = test && (stencil.getFail() != StencilOperation.KEEP || stencil.getPass() != StencilOperation.KEEP);
		return new StencilEmulation(test, write, stencil.getMask() & 0xFF, stencil.getReference() & 0xFF, stencil.getFunction(), stencil.getFail(), stencil.getPass());
	}

	public void write(final @NonNull UniformBlock block) {
		block
		.value(GlslShaderTranslator.STENCIL_TEST, this.test)
		.value(GlslShaderTranslator.STENCIL_FUNCTION, this.function.ordinal())
		.value(GlslShaderTranslator.STENCIL_REFERENCE, this.reference)
		.value(GlslShaderTranslator.STENCIL_MASK, this.mask)
		.value(GlslShaderTranslator.STENCIL_FAIL, this.fail.ordinal())
		.value(GlslShaderTranslator.STENCIL_PASS, this.pass.ordinal());
	}

	public enum Pass {

		NONE,
		TEST,
		WRITE;

	}

}