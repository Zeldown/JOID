package dev.joid.impl.opengl.binding;

public interface IGlStateBinding {

	public void stencilMask(final int mask);
	public void lineWidth(final float width);
	public void depthMask(final boolean write);
	public void blendEquation(final int equation);
	public void stencilOp(final int fail, final int depthFail, final int pass);
	public void stencilFunc(final int function, final int reference, final int mask);
	public void viewport(final int x, final int y, final int width, final int height);
	public void clearColor(final float red, final float green, final float blue, final float alpha);
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha);
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha);

}