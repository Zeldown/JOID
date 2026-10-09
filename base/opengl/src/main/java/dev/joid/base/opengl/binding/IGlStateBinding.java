package dev.joid.base.opengl.binding;

public interface IGlStateBinding {

	public void clearDepth(final double depth);
	public void clearStencil(final int stencil);
	public void viewport(final int x, final int y, final int width, final int height);
	public void clearColor(final float red, final float green, final float blue, final float alpha);
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha);

	public void blendEquationSeparate(final int color, final int alpha);
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha);

	public void depthFunc(final int function);
	public void depthMask(final boolean write);

	public void stencilMask(final int mask);
	public void stencilMaskSeparate(final int face, final int mask);
	public void stencilOp(final int fail, final int depthFail, final int pass);
	public void stencilFunc(final int function, final int reference, final int mask);
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass);
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask);

	public void cullFace(final int mode);
	public void frontFace(final int mode);
	public void lineWidth(final float width);
	public void polygonMode(final int face, final int mode);

	public void pixelStorei(final int name, final int value);

}