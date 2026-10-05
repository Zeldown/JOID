package dev.joid.lib.bridge.render;

import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;

@Getter
public final class RecordingFrameBuffer implements IFrameBuffer {

	private final int      width;
	private final int      height;
	private final ITexture texture;

	private boolean deleted;

	public RecordingFrameBuffer(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.texture = new RecordingTexture().allocate(width, height);
	}

	@Override
	public void delete() {
		this.deleted = true;
	}

}