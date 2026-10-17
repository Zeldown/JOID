package dev.joid.base.opengl.render.state;

import java.util.function.Supplier;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import lombok.Getter;
import lombok.NonNull;

public final class GlStateGuard {

	private final GlStateJournal journal;
	private final GlCapabilities capabilities;

	@Getter
	private final IGlBinding binding;

	private int depth;

	private GlStateGuard(final IGlBinding binding, final GlCapabilities capabilities) {
		this.journal      = GlStateJournal.create(binding, capabilities, capabilities.getFrameBufferFamily());
		this.capabilities = capabilities;
		this.binding      = JournalGlBinding.create(binding, this.journal);
	}

	public static @NonNull GlStateGuard create(final IGlBinding binding, final GlCapabilities capabilities) {
		return new GlStateGuard(binding, capabilities);
	}

	public void exit() {
		if (this.depth > 0 && --this.depth == 0) {
			this.journal.restore();
		}
	}

	public void enter() {
		if (this.depth++ == 0) {
			this.journal.start();
			this.resetPipeline();
		}
	}

	public void run(final Runnable action) {
		this.enter();
		try {
			action.run();
		} finally {
			this.exit();
		}
	}

	public <T> T call(final Supplier<T> action) {
		this.enter();
		try {
			return action.get();
		} finally {
			this.exit();
		}
	}

	public void suspend(final Runnable draw) {
		final int depth = this.depth;
		if (depth > 0) {
			this.journal.restore();
		}

		this.depth = 0;
		try {
			draw.run();
		} finally {
			this.depth = depth;
			if (depth > 0) {
				this.journal.start();
				this.resetPipeline();
			}
		}
	}

	private void resetPipeline() {
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

		this.binding.clearDepth(1D);
		this.binding.clearStencil(0);
		this.binding.depthFunc(GlConstants.LESS);
		this.binding.stencilMask(0xFF);
		this.binding.cullFace(GlConstants.BACK);
		this.binding.frontFace(GlConstants.CCW);
		this.binding.lineWidth(1F);
		this.binding.polygonMode(GlConstants.FRONT_AND_BACK, GlConstants.FILL);

		this.binding.pixelStorei(GlConstants.PACK_ALIGNMENT, 4);
		this.binding.pixelStorei(GlConstants.PACK_ROW_LENGTH, 0);
		this.binding.pixelStorei(GlConstants.PACK_SKIP_ROWS, 0);
		this.binding.pixelStorei(GlConstants.PACK_SKIP_PIXELS, 0);
		this.binding.pixelStorei(GlConstants.UNPACK_ALIGNMENT, 4);
		this.binding.pixelStorei(GlConstants.UNPACK_ROW_LENGTH, 0);
		this.binding.pixelStorei(GlConstants.UNPACK_SKIP_ROWS, 0);
		this.binding.pixelStorei(GlConstants.UNPACK_SKIP_PIXELS, 0);
		this.binding.bindBuffer(GlConstants.PIXEL_PACK_BUFFER, 0);
		this.binding.bindBuffer(GlConstants.PIXEL_UNPACK_BUFFER, 0);
	}

}