package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.ShaderFillType;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RoundedShaderPass implements IShaderPass {

	private final float radius;
	private final float x1;
	private final float y1;
	private final float x2;
	private final float y2;

	public static @NonNull RoundedShaderPass create(final float radius, final float x1, final float y1, final float x2, final float y2) {
		return new RoundedShaderPass(radius, x1, y1, x2, y2);
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

		RoundedShader.inst().bind(this.radius, this.x1, this.y1, this.x2, this.y2, ShaderFillType.TEXTURE);
		RoundedShader.inst().aligned(BridgeHandler.RENDER.get().getPixelGrid().isAligned());
	}

}