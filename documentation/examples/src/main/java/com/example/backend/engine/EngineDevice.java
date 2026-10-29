package com.example.backend.engine;

import java.nio.ByteBuffer;

import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.render.texture.TextureSampling;

public interface EngineDevice {

	public String getName();
	public int getMaxTextureSize();

	public int createTexture();
	public void deleteTexture(final int texture);

	public void allocateLevels(final int texture, final int width, final int height, final int first, final int levels);
	public void uploadTexture(final int texture, final int width, final int height, final int[] pixels);
	public void copyLevel(final int texture, final int level, final int sourceWidth, final int sourceHeight, final int width, final int height);

	public boolean isTexture(final int texture);
	public int getTextureWidth(final int texture);
	public int getTextureLevels(final int texture);
	public int getTextureHeight(final int texture);

	public int createTarget(final int texture, final int width, final int height);
	public void deleteTarget(final int target);

	public int createProgram(final String vertex, final String fragment);
	public void deleteProgram(final int program);
	public void uniforms(final int program, final ByteBuffer block);

	public void target(final int target);
	public void viewport(final int x, final int y, final int width, final int height);

	public void clearDepth(final float depth);
	public void clearStencil(final int stencil);
	public void clearColor(final float red, final float green, final float blue, final float alpha);

	public void cull(final boolean cull);
	public void stencilMask(final int mask);
	public void colorMask(final boolean write);
	public void blend(final BlendState blend);
	public void stencil(final StencilState stencil);
	public void depth(final boolean test, final boolean write);
	public void texture(final int unit, final int texture, final TextureSampling sampling);

	public void readPixels(final int x, final int y, final int width, final int height, final ByteBuffer pixels);

	public void draw(final boolean lines, final ByteBuffer vertices, final int count, final int program);

}