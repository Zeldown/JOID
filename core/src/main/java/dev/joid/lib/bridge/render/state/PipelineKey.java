package dev.joid.lib.bridge.render.state;

import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.vertex.Primitive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineKey {

	private final IShader    shader;
	private final BlendState blend;
	private final boolean    colorMask;
	private final boolean    depthTest;
	private final boolean    depthWrite;
	private final boolean    cull;
	private final Primitive  primitive;
	private final boolean    stencil;

	public static @NonNull PipelineKey create(final @NonNull IShader shader, final @NonNull RenderState state, final @NonNull Primitive primitive) {
		return PipelineKey.create(shader, state.getBlend(), state.isColorMask(), state.isDepthTest(), state.isDepthWrite(), state.isCull(), primitive, false);
	}

	public static @NonNull PipelineKey stencil(final @NonNull IShader shader, final @NonNull RenderState state, final @NonNull Primitive primitive) {
		return PipelineKey.create(shader, BlendState.DISABLED, true, false, false, state.isCull(), primitive, true);
	}

	private static PipelineKey create(final IShader shader, final BlendState blend, final boolean colorMask, final boolean depthTest, final boolean depthWrite, final boolean cull, final Primitive primitive, final boolean stencil) {
		return new PipelineKey(shader, blend.isEnabled() ? blend : BlendState.DISABLED, colorMask, depthTest, depthTest && depthWrite, cull, primitive, stencil);
	}

}