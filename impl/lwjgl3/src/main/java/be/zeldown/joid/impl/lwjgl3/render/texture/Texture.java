package be.zeldown.joid.impl.lwjgl3.render.texture;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Texture implements ITexture {

	private final int id;

	private int     width;
	private int     height;
	private boolean deleted;
	private boolean mipmapped;

	private Texture(final int id) {
		this.id = id;
	}

	public static @NonNull Texture create() {
		return new Texture(GL11C.glGenTextures());
	}

	@Override
	public void delete() {
		if (this.deleted) {
			return;
		}

		GL11C.glDeleteTextures(this.id);
		this.deleted = true;
	}

	@Override
	public @NonNull Texture mipmap(final boolean mipmap) {
		if (this.mipmapped == mipmap) {
			return this;
		}

		this.mipmapped = mipmap;

		if (mipmap && this.width > 0 && this.height > 0) {
			GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
			GL30C.glGenerateMipmap(GL11C.GL_TEXTURE_2D);
		}

		return this;
	}

	@Override
	public @NonNull Texture allocate(final int width, final int height) {
		this.width = width;
		this.height = height;

		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
		return this;
	}

	@Override
	public @NonNull Texture upload(final @NonNull int[] pixels, final int width, final int height) {
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		GL11C.glTexSubImage2D(GL11C.GL_TEXTURE_2D, 0, 0, 0, width, height, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);

		if (this.mipmapped) {
			GL30C.glGenerateMipmap(GL11C.GL_TEXTURE_2D);
		}

		return this;
	}

}