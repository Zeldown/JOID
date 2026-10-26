package dev.joid.lib.bridge.render.framebuffer;

import dev.joid.lib.bridge.render.texture.Texture;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FrameBufferHandle<T extends Texture> implements IFrameBuffer {

	@NonNull
	private final T texture;

	private boolean deleted;

	@Override
	public final void delete() {
		if (this.deleted) {
			return;
		}

		this.deleteHandle();
		this.texture.delete();
		this.deleted = true;
	}

	@Override
	public final int getWidth() {
		return this.texture.getWidth();
	}

	@Override
	public final int getHeight() {
		return this.texture.getHeight();
	}

	protected abstract void deleteHandle();

}