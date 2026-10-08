package dev.joid.impl.lwjgl3.binding;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL33C;

import dev.joid.impl.opengl.binding.IGlTextureBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlTextureBinding implements IGlTextureBinding {

	public static @NonNull Lwjgl3GlTextureBinding create() {
		return new Lwjgl3GlTextureBinding();
	}

	@Override
	public int genTexture() {
		return GL11C.glGenTextures();
	}

	@Override
	public void activeTexture(final int unit) {
		GL13C.glActiveTexture(unit);
	}

	@Override
	public void deleteTexture(final int texture) {
		GL11C.glDeleteTextures(texture);
	}

	@Override
	public void bindTexture(final int target, final int texture) {
		GL11C.glBindTexture(target, texture);
	}

	@Override
	public void texParameteri(final int target, final int name, final int value) {
		GL11C.glTexParameteri(target, name, value);
	}

	@Override
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type) {
		GL11C.glTexImage2D(target, level, internalFormat, width, height, 0, format, type, (IntBuffer) null);
	}

	@Override
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels) {
		GL11C.glTexSubImage2D(target, level, x, y, width, height, format, type, pixels);
	}

	@Override
	public int genSampler() {
		return GL33C.glGenSamplers();
	}

	@Override
	public void bindSampler(final int unit, final int sampler) {
		GL33C.glBindSampler(unit, sampler);
	}

	@Override
	public void samplerParameteri(final int sampler, final int name, final int value) {
		GL33C.glSamplerParameteri(sampler, name, value);
	}

}