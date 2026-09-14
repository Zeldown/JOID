package be.zeldown.joid.impl.lwjgl2.render.framebuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import be.zeldown.joid.impl.lwjgl2.render.LWJGL2RenderBridge;
import be.zeldown.joid.impl.lwjgl2.render.texture.LWJGL2Texture;
import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LWJGL2FrameBuffer implements IFrameBuffer {

	private final int           id;
	private final LWJGL2Texture texture;

	public static @NonNull LWJGL2FrameBuffer create(final int width, final int height, final TextureFilter filter) {
		final LWJGL2Texture texture = LWJGL2Texture.create().allocate(width, height);
		final int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		LWJGL2RenderBridge.applyTextureParameters(filter, TextureWrap.CLAMP_TO_BORDER);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);

		final int previousFrameBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		final int id = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture.getId(), 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFrameBuffer);
		return new LWJGL2FrameBuffer(id, texture);
	}

	@Override
	public int getWidth() {
		return this.texture.getWidth();
	}

	@Override
	public int getHeight() {
		return this.texture.getHeight();
	}

	@Override
	public void delete() {
		GL30.glDeleteFramebuffers(this.id);
		this.texture.delete();
	}

}