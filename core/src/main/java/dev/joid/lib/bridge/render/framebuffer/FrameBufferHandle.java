package dev.joid.lib.bridge.render.framebuffer;

import dev.joid.lib.bridge.render.texture.Texture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class FrameBufferHandle<T extends Texture> implements IFrameBuffer {

	private final T texture;

	private boolean deleted;

	protected FrameBufferHandle(final @NonNull T texture) {
		this.texture = texture;
	}

	@Override
	public final int getWidth() {
		return this.texture.getWidth();
	}

	@Override
	public final int getHeight() {
		return this.texture.getHeight();
	}

	@Override
	public final void delete() {
		if (this.deleted) {
			return;
		}

		this.onDelete();
		this.texture.delete();
		this.deleted = true;
	}

	protected abstract void onDelete();

}