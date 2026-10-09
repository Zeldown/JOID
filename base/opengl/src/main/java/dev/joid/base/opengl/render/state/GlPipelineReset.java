package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlPipelineReset {

	private final IGlBinding     binding;
	private final GlCapabilities capabilities;

	public static @NonNull GlPipelineReset create(final @NonNull IGlBinding binding, final @NonNull GlCapabilities capabilities) {
		return new GlPipelineReset(binding, capabilities);
	}

	public void apply() {
		this.binding.disable(GlConstants.LINE_SMOOTH);
		this.binding.disable(GlConstants.SCISSOR_TEST);
		this.binding.disable(GlConstants.COLOR_LOGIC_OP);
		this.binding.disable(GlConstants.POLYGON_OFFSET_FILL);
		if (this.capabilities.hasFrameBufferSrgb()) {
			this.binding.disable(GlConstants.FRAMEBUFFER_SRGB);
		}
		if (this.capabilities.hasRasterizerDiscard()) {
			this.binding.disable(GlConstants.RASTERIZER_DISCARD);
		}
		if (this.capabilities.hasPrimitiveRestart()) {
			this.binding.disable(GlConstants.PRIMITIVE_RESTART);
		}
		if (this.capabilities.hasDepthClamp()) {
			this.binding.disable(GlConstants.DEPTH_CLAMP);
		}
		if (this.capabilities.getProfile() == GlProfile.COMPATIBILITY) {
			this.binding.disable(GlConstants.FOG);
			this.binding.disable(GlConstants.LIGHTING);
			this.binding.disable(GlConstants.ALPHA_TEST);
			this.binding.disable(GlConstants.COLOR_MATERIAL);
		}

		final IGlStateBinding state = this.binding.getStateBinding();
		state.clearDepth(1D);
		state.clearStencil(0);
		state.depthFunc(GlConstants.LESS);
		state.stencilMask(0xFF);
		state.cullFace(GlConstants.BACK);
		state.frontFace(GlConstants.CCW);
		state.lineWidth(1F);
		state.polygonMode(GlConstants.FRONT_AND_BACK, GlConstants.FILL);

		state.pixelStorei(GlConstants.PACK_ALIGNMENT, 4);
		state.pixelStorei(GlConstants.PACK_ROW_LENGTH, 0);
		state.pixelStorei(GlConstants.PACK_SKIP_ROWS, 0);
		state.pixelStorei(GlConstants.PACK_SKIP_PIXELS, 0);
		state.pixelStorei(GlConstants.UNPACK_ALIGNMENT, 4);
		state.pixelStorei(GlConstants.UNPACK_ROW_LENGTH, 0);
		state.pixelStorei(GlConstants.UNPACK_SKIP_ROWS, 0);
		state.pixelStorei(GlConstants.UNPACK_SKIP_PIXELS, 0);
		if (this.capabilities.hasPixelBuffers()) {
			this.binding.getBufferBinding().bindBuffer(GlConstants.PIXEL_PACK_BUFFER, 0);
			this.binding.getBufferBinding().bindBuffer(GlConstants.PIXEL_UNPACK_BUFFER, 0);
		}
	}

}