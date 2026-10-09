package dev.joid.backend.lwjgl2.binding;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;

import dev.joid.base.opengl.binding.IGlTextureBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlTextureBinding implements IGlTextureBinding {

	private IntBuffer uploadBuffer;

	public static @NonNull Lwjgl2GlTextureBinding create() {
		return new Lwjgl2GlTextureBinding();
	}

	@Override
	public int genTexture() {
		return GL11.glGenTextures();
	}

	@Override
	public void activeTexture(final int unit) {
		GL13.glActiveTexture(unit);
	}

	@Override
	public void deleteTexture(final int texture) {
		GL11.glDeleteTextures(texture);
	}

	@Override
	public void bindSampler(final int unit, final int sampler) {
		GL33.glBindSampler(unit, sampler);
	}

	@Override
	public void bindTexture(final int target, final int texture) {
		GL11.glBindTexture(target, texture);
	}

	@Override
	public void texParameteri(final int target, final int name, final int value) {
		GL11.glTexParameteri(target, name, value);
	}

	@Override
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type) {
		GL11.glTexImage2D(target, level, internalFormat, width, height, 0, format, type, (IntBuffer) null);
	}

	@Override
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels) {
		if (this.uploadBuffer == null || this.uploadBuffer.capacity() < width * height) {
			this.uploadBuffer = BufferUtils.createIntBuffer(width * height);
		}

		this.uploadBuffer.clear();
		this.uploadBuffer.put(pixels, 0, width * height).flip();
		GL11.glTexSubImage2D(target, level, x, y, width, height, format, type, this.uploadBuffer);
	}

	@Override
	public boolean isTexture(final int texture) {
		return GL11.glIsTexture(texture);
	}

	@Override
	public int getTexParameteri(final int target, final int name) {
		return GL11.glGetTexParameteri(target, name);
	}

	@Override
	public int getTexLevelParameteri(final int target, final int level, final int name) {
		return GL11.glGetTexLevelParameteri(target, level, name);
	}

}