package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.RoundedShaderType;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.NonNull;

public class RoundedShaderPass implements IShaderPass {

	private final float x1;
	private final float y1;
	private final float x2;
	private final float y2;
	private final float radius;

	public RoundedShaderPass(final float radius, final float x1, final float y1, final float x2, final float y2) {
		this.x1     = x1;
		this.y1     = y1;
		this.x2     = x2;
		this.y2     = y2;
		this.radius = radius;
	}

	@Override
	public void unbind() {
		RoundedShader.inst().unbind();
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!RoundedShader.inst().canDraw()) {
			return;
		}

		RoundedShader.inst().bind(this.radius, this.x1, this.y1, this.x2, this.y2, RoundedShaderType.TEXTURE);
		RoundedShader.inst().aligned(BridgeHandler.RENDER.get().getPixelGrid().isAligned());
	}

}