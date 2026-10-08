package dev.joid.base.opengl.binding;

public interface IGlFrameBufferBinding {

	public int genFramebuffer();
	public void deleteFramebuffer(final int framebuffer);
	public void bindFramebuffer(final int target, final int framebuffer);
	public void framebufferTexture2D(final int target, final int attachment, final int textureTarget, final int texture, final int level);

	public int genRenderbuffer();
	public void deleteRenderbuffer(final int renderbuffer);
	public void bindRenderbuffer(final int target, final int renderbuffer);
	public void renderbufferStorage(final int target, final int internalFormat, final int width, final int height);
	public void framebufferRenderbuffer(final int target, final int attachment, final int renderbufferTarget, final int renderbuffer);

	public void blitFramebuffer(final int sourceX0, final int sourceY0, final int sourceX1, final int sourceY1, final int targetX0, final int targetY0, final int targetX1, final int targetY1, final int mask, final int filter);

}