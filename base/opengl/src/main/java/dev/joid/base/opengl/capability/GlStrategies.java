package dev.joid.base.opengl.capability;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.texture.BlitGlMipmapBuilder;
import dev.joid.base.opengl.render.texture.DrawGlMipmapBuilder;
import dev.joid.base.opengl.render.texture.IGlMipmapBuilder;
import dev.joid.base.opengl.render.vertex.ArrayObjectVertexInput;
import dev.joid.base.opengl.render.vertex.DefaultVertexInput;
import dev.joid.base.opengl.render.vertex.GlVertexInput;
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

	private final boolean             blitMipmaps;
	private final GlslDialect         dialect;
	private final boolean             vertexArrayObject;
	private final GlFrameBufferFamily frameBufferFamily;

	public static @NonNull GlStrategies of(final @NonNull GlCapabilities capabilities) {
		if (capabilities.getVersion() < 210 || capabilities.getGlslVersion() < GlslDialect.GLSL_120.getVersion()) {
			throw new IllegalStateException("JOID needs OpenGL 2.1 with GLSL 1.20, this context offers " + capabilities.getName() + ": update the graphics driver or create a newer OpenGL context");
		}

		final GlFrameBufferFamily frameBufferFamily = capabilities.getFrameBufferFamily();
		if (frameBufferFamily == GlFrameBufferFamily.UNSUPPORTED) {
			throw new IllegalStateException("JOID needs framebuffer objects (OpenGL 3.0, GL_ARB_framebuffer_object or GL_EXT_framebuffer_object), this context offers " + capabilities.getName());
		}

		GlslDialect dialect = GlslDialect.GLSL_120;
		for (final GlslDialect candidate : GlslDialect.values()) {
			if (!candidate.isEs() && candidate.getVersion() <= Math.min(capabilities.getGlslVersion(), GlslDialect.GLSL_330.getVersion()) && candidate.getVersion() > dialect.getVersion()) {
				dialect = candidate;
			}
		}
		return new GlStrategies(capabilities.hasFrameBufferBlit(), dialect, capabilities.hasVertexArrays(), frameBufferFamily);
	}

	public @NonNull GlslShaderTranslator createTranslator() {
		return GlslShaderTranslator.create(this.dialect, UniformLayout.LOOSE);
	}

	public @NonNull IGlMipmapBuilder createMipmapBuilder() {
		return this.blitMipmaps ? BlitGlMipmapBuilder.create() : DrawGlMipmapBuilder.create();
	}

	public @NonNull GlVertexInput createVertexInput(final @NonNull IGlBinding binding) {
		return this.vertexArrayObject ? ArrayObjectVertexInput.create(binding) : DefaultVertexInput.create(binding);
	}

}