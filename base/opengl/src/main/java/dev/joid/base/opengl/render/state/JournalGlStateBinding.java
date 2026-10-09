package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.IGlStateBinding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JournalGlStateBinding implements IGlStateBinding {

	private final IGlStateBinding binding;
	private final GlStateJournal  journal;

	public static @NonNull JournalGlStateBinding create(final @NonNull IGlStateBinding binding, final @NonNull GlStateJournal journal) {
		return new JournalGlStateBinding(binding, journal);
	}

	@Override
	public void clearStencil(final int stencil) {
		this.journal.touch(GlStateKey.CLEAR_STENCIL);
		this.binding.clearStencil(stencil);
	}

	@Override
	public void clearDepth(final double depth) {
		this.journal.touch(GlStateKey.CLEAR_DEPTH);
		this.binding.clearDepth(depth);
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		this.journal.touch(GlStateKey.VIEWPORT);
		this.binding.viewport(x, y, width, height);
	}

	@Override
	public void clearColor(final float red, final float green, final float blue, final float alpha) {
		this.journal.touch(GlStateKey.CLEAR_COLOR);
		this.binding.clearColor(red, green, blue, alpha);
	}

	@Override
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
		this.journal.touch(GlStateKey.COLOR_MASK);
		this.binding.colorMask(red, green, blue, alpha);
	}

	@Override
	public void blendEquationSeparate(final int color, final int alpha) {
		this.journal.touch(GlStateKey.BLEND_EQUATION);
		this.binding.blendEquationSeparate(color, alpha);
	}

	@Override
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		this.journal.touch(GlStateKey.BLEND_FUNCTION);
		this.binding.blendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	@Override
	public void depthFunc(final int function) {
		this.journal.touch(GlStateKey.DEPTH_FUNCTION);
		this.binding.depthFunc(function);
	}

	@Override
	public void depthMask(final boolean write) {
		this.journal.touch(GlStateKey.DEPTH_MASK);
		this.binding.depthMask(write);
	}

	@Override
	public void stencilMask(final int mask) {
		this.journal.touch(GlStateKey.STENCIL_MASK);
		this.binding.stencilMask(mask);
	}

	@Override
	public void stencilMaskSeparate(final int face, final int mask) {
		this.journal.touch(GlStateKey.STENCIL_MASK);
		this.binding.stencilMaskSeparate(face, mask);
	}

	@Override
	public void stencilOp(final int fail, final int depthFail, final int pass) {
		this.journal.touch(GlStateKey.STENCIL_OPERATION);
		this.binding.stencilOp(fail, depthFail, pass);
	}

	@Override
	public void stencilFunc(final int function, final int reference, final int mask) {
		this.journal.touch(GlStateKey.STENCIL_FUNCTION);
		this.binding.stencilFunc(function, reference, mask);
	}

	@Override
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass) {
		this.journal.touch(GlStateKey.STENCIL_OPERATION);
		this.binding.stencilOpSeparate(face, fail, depthFail, pass);
	}

	@Override
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask) {
		this.journal.touch(GlStateKey.STENCIL_FUNCTION);
		this.binding.stencilFuncSeparate(face, function, reference, mask);
	}

	@Override
	public void cullFace(final int mode) {
		this.journal.touch(GlStateKey.CULL_FACE);
		this.binding.cullFace(mode);
	}

	@Override
	public void frontFace(final int mode) {
		this.journal.touch(GlStateKey.FRONT_FACE);
		this.binding.frontFace(mode);
	}

	@Override
	public void lineWidth(final float width) {
		this.journal.touch(GlStateKey.LINE_WIDTH);
		this.binding.lineWidth(width);
	}

	@Override
	public void polygonMode(final int face, final int mode) {
		this.journal.touch(GlStateKey.POLYGON_MODE);
		this.binding.polygonMode(face, mode);
	}

	@Override
	public void pixelStorei(final int name, final int value) {
		this.journal.touch(GlStateKey.PIXEL_STORE, name);
		this.binding.pixelStorei(name, value);
	}

}