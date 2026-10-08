package dev.joid.backend.lwjgl2.binding;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import dev.joid.base.opengl.binding.IGlStateBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlStateBinding implements IGlStateBinding {

	public static @NonNull Lwjgl2GlStateBinding create() {
		return new Lwjgl2GlStateBinding();
	}

	@Override
	public void clearStencil(final int stencil) {
		GL11.glClearStencil(stencil);
	}

	@Override
	public void clearDepth(final double depth) {
		GL11.glClearDepth(depth);
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		GL11.glViewport(x, y, width, height);
	}

	@Override
	public void clearColor(final float red, final float green, final float blue, final float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
		GL11.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void blendEquationSeparate(final int color, final int alpha) {
		GL20.glBlendEquationSeparate(color, alpha);
	}

	@Override
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		GL14.glBlendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	@Override
	public void depthFunc(final int function) {
		GL11.glDepthFunc(function);
	}

	@Override
	public void depthMask(final boolean write) {
		GL11.glDepthMask(write);
	}

	@Override
	public void stencilMask(final int mask) {
		GL11.glStencilMask(mask);
	}

	@Override
	public void stencilMaskSeparate(final int face, final int mask) {
		GL20.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void stencilOp(final int fail, final int depthFail, final int pass) {
		GL11.glStencilOp(fail, depthFail, pass);
	}

	@Override
	public void stencilFunc(final int function, final int reference, final int mask) {
		GL11.glStencilFunc(function, reference, mask);
	}

	@Override
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass) {
		GL20.glStencilOpSeparate(face, fail, depthFail, pass);
	}

	@Override
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask) {
		GL20.glStencilFuncSeparate(face, function, reference, mask);
	}

	@Override
	public void cullFace(final int mode) {
		GL11.glCullFace(mode);
	}

	@Override
	public void frontFace(final int mode) {
		GL11.glFrontFace(mode);
	}

	@Override
	public void lineWidth(final float width) {
		GL11.glLineWidth(width);
	}

	@Override
	public void polygonMode(final int face, final int mode) {
		GL11.glPolygonMode(face, mode);
	}

	@Override
	public void pixelStorei(final int name, final int value) {
		GL11.glPixelStorei(name, value);
	}

}