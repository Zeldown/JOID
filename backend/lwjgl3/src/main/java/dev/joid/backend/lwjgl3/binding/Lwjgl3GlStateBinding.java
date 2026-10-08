package dev.joid.backend.lwjgl3.binding;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL20C;

import dev.joid.base.opengl.binding.IGlStateBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlStateBinding implements IGlStateBinding {

	public static @NonNull Lwjgl3GlStateBinding create() {
		return new Lwjgl3GlStateBinding();
	}

	@Override
	public void clearStencil(final int stencil) {
		GL11C.glClearStencil(stencil);
	}

	@Override
	public void clearDepth(final double depth) {
		GL11C.glClearDepth(depth);
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		GL11C.glViewport(x, y, width, height);
	}

	@Override
	public void clearColor(final float red, final float green, final float blue, final float alpha) {
		GL11C.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
		GL11C.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void blendEquationSeparate(final int color, final int alpha) {
		GL20C.glBlendEquationSeparate(color, alpha);
	}

	@Override
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		GL14C.glBlendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	@Override
	public void depthFunc(final int function) {
		GL11C.glDepthFunc(function);
	}

	@Override
	public void depthMask(final boolean write) {
		GL11C.glDepthMask(write);
	}

	@Override
	public void stencilMask(final int mask) {
		GL11C.glStencilMask(mask);
	}

	@Override
	public void stencilMaskSeparate(final int face, final int mask) {
		GL20C.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void stencilOp(final int fail, final int depthFail, final int pass) {
		GL11C.glStencilOp(fail, depthFail, pass);
	}

	@Override
	public void stencilFunc(final int function, final int reference, final int mask) {
		GL11C.glStencilFunc(function, reference, mask);
	}

	@Override
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass) {
		GL20C.glStencilOpSeparate(face, fail, depthFail, pass);
	}

	@Override
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask) {
		GL20C.glStencilFuncSeparate(face, function, reference, mask);
	}

	@Override
	public void cullFace(final int mode) {
		GL11C.glCullFace(mode);
	}

	@Override
	public void frontFace(final int mode) {
		GL11C.glFrontFace(mode);
	}

	@Override
	public void lineWidth(final float width) {
		GL11C.glLineWidth(width);
	}

	@Override
	public void polygonMode(final int face, final int mode) {
		GL11C.glPolygonMode(face, mode);
	}

	@Override
	public void pixelStorei(final int name, final int value) {
		GL11C.glPixelStorei(name, value);
	}

}