package dev.joid.lib.shader.impl;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import dev.joid.lib.bridge.render.shader.uniform.FloatUniform;
import lombok.NonNull;

public class ShadowShader extends ShaderImpl {

	private static final ShadowShader INSTANCE = new ShadowShader();

	private ShadowShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/shadow/shadow.vsh"), JOID.class.getResourceAsStream("/assets/shaders/shadow/shadow.fsh"));
	}

	public static @NonNull ShadowShader inst() {
		return ShadowShader.INSTANCE;
	}

	public static void use(final float radius, final float blur, final float x1, final float y1, final float x2, final float y2, final @NonNull Runnable runnable) {
		if (!ShadowShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getShader();
		ShadowShader.INSTANCE.bind(radius, blur, x1, y1, x2, y2);
		try {
			runnable.run();
		} finally {
			ShadowShader.INSTANCE.unbind();
			render.shader(previousShader);
		}
	}

	public void bind(final float radius, final float blur, final float x1, final float y1, final float x2, final float y2) {
		ShadowShader.INSTANCE.bind();
		final FloatUniform radiusUniform = ShadowShader.INSTANCE.shader.getFloatUniform("u_Radius");
		radiusUniform.setValue(radius);

		final FloatUniform blurUniform = ShadowShader.INSTANCE.shader.getFloatUniform("u_Blur");
		blurUniform.setValue(blur);

		final Float4Uniform boxUniform = ShadowShader.INSTANCE.shader.getFloat4Uniform("u_Box");
		boxUniform.setValue(x1, y1, x2, y2);
	}

}