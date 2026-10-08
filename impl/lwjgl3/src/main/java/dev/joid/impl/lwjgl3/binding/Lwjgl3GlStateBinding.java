package dev.joid.impl.lwjgl3.binding;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;

import dev.joid.impl.opengl.binding.IGlStateBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlStateBinding implements IGlStateBinding {

	public static @NonNull Lwjgl3GlStateBinding create() {
		return new Lwjgl3GlStateBinding();
	}

	@Override
	public void stencilMask(final int mask) {
		GL11C.glStencilMask(mask);
	}

	@Override
	public void lineWidth(final float width) {
		GL11C.glLineWidth(width);
	}

	@Override
	public void depthMask(final boolean write) {
		GL11C.glDepthMask(write);
	}

	@Override
	public void blendEquation(final int equation) {
		GL14C.glBlendEquation(equation);
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
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		GL14C.glBlendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

}