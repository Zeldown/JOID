package dev.joid.impl.opengl.capability;

import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlStrategies {

	private final GlslDialect   dialect;
	private final UniformLayout uniformLayout;

	public static @NonNull GlStrategies of(final @NonNull GlCapabilities capabilities) {
		if (capabilities.getGlslVersion() < GlslDialect.GLSL_330.getVersion() || !capabilities.hasVertexArrays() || !capabilities.hasUniformBuffers() || !capabilities.hasSamplerObjects() || !capabilities.hasFrameBufferObjects()) {
			throw new IllegalStateException("JOID needs OpenGL 3.3 with GLSL 3.30, this context offers " + capabilities.getName() + " with GLSL " + capabilities.getGlslVersion() / 100 + "." + String.format("%02d", capabilities.getGlslVersion() % 100) + " (" + capabilities.getRenderer() + ")");
		}
		return new GlStrategies(GlslDialect.GLSL_330, UniformLayout.BLOCK);
	}

	public @NonNull GlslShaderTranslator createTranslator() {
		return GlslShaderTranslator.create(this.dialect, this.uniformLayout);
	}

}