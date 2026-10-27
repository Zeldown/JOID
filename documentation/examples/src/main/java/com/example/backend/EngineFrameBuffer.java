package com.example.backend;

import com.example.backend.engine.EngineDevice;

import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;

// #region framebuffer
public final class EngineFrameBuffer extends FrameBufferHandle<EngineTexture> {

	private final int          target;
	private final EngineDevice device;

	public EngineFrameBuffer(final EngineDevice device, final int width, final int height) {
		super(new EngineTexture(device));
		super.getTexture().allocate(width, height);
		this.device = device;
		this.target = device.createTarget(super.getTexture().getId(), width, height);
	}

	public int getTarget() {
		return this.target;
	}

	@Override
	protected void deleteHandle() {
		this.device.deleteTarget(this.target);
	}

}
// #endregion