package be.zeldown.joid.lib.opengl.framebuffer;

import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class FrameBuffer {

	private static final Cache<String, ResourceData> RESOURCE_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
	private static final ResourceBuilder RESOURCE_BUILDER = ResourceBuilder.create().async().linear().cache(FrameBuffer.RESOURCE_CACHE);

	private final int framebuffer;
	private final int texture;

	private int width;
	private int height;
	private Resource resource;

	private boolean filled = false;

	public FrameBuffer() {
		this.framebuffer = GL30.glGenFramebuffers();
		this.texture = GL11.glGenTextures();
	}

	public @NonNull FrameBuffer prepare(final int width, final int height, final int interpolation) {
		this.width = width;
		this.height = height;
		this.resource = FrameBuffer.RESOURCE_BUILDER.of(this.texture).interpolation(interpolation);

		AllocatedTextureUtil.allocateTexture(this.texture, this.width, this.height);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.texture, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		return this;
	}

	public void bind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
	}

	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	public void fill(final @NonNull Runnable runnable) {
		this.bind();
		runnable.run();
		this.unbind();
	}

	public void draw(final double x, final double y, final double width, final double height) {
		if (this.resource == null) {
			throw new RuntimeException("You have to prepare the framebuffer before drawing it.");
		}

		if (!this.filled) {
			throw new RuntimeException("You have to fill the framebuffer before drawing it.");
		}

		DrawUtils.RESOURCE.drawResource(x, y, width, height, this.resource);
	}

	public void delete() {
		GL30.glDeleteFramebuffers(this.framebuffer);
		GL11.glDeleteTextures(this.texture);
	}

	@Override
	protected void finalize() throws Throwable {
		this.delete();
		super.finalize();
	}

}