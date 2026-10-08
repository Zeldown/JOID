package dev.joid.base.opengl.render.texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.shader.GlShader;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.MipmapChain;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public final class DrawMipmapBuilder implements IGlMipmapBuilder {

	private final ByteBuffer quad;

	private GlShader shader;
	private boolean  warned;

	private DrawMipmapBuilder() {
		final float[][] corners = {{-1F, -1F, 0F, 0F}, {1F, -1F, 1F, 0F}, {1F, 1F, 1F, 1F}, {-1F, -1F, 0F, 0F}, {1F, 1F, 1F, 1F}, {-1F, 1F, 0F, 1F}};
		this.quad = ByteBuffer.allocateDirect(corners.length * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		for (int i = 0; i < corners.length; i++) {
			this.quad.putFloat(i * VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset(), corners[i][0]);
			this.quad.putFloat(i * VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset() + 4, corners[i][1]);
			this.quad.putFloat(i * VertexBuffer.STRIDE + VertexAttribute.TEXTURE_COORDINATE.getOffset(), corners[i][2]);
			this.quad.putFloat(i * VertexBuffer.STRIDE + VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4, corners[i][3]);
		}
	}

	public static @NonNull DrawMipmapBuilder create() {
		return new DrawMipmapBuilder();
	}

	@Override
	public void build(final @NonNull GlRenderBridge bridge, final @NonNull GlTexture texture, final @NonNull MipmapChain chain) {
		if (!this.warned && JOID.inst().isDevMode()) {
			System.err.println("[JOID] This OpenGL context cannot blit framebuffers (" + bridge.getCapabilities().getName() + "): mipmaps are drawn, which does not give exactly the pixels of the other backends");
			this.warned = true;
		}

		final IGlBinding binding = bridge.getBinding();
		final IGlTextureBinding textures = binding.getTextureBinding();
		final IGlFrameBufferBinding frameBuffer = bridge.getFrameBufferBinding();
		final GlShader shader = this.getShader(bridge);
		binding.getProgramBinding().useProgram(shader.getProgram());
		binding.getProgramBinding().uniform1i(shader.getLocation("tex"), 0);
		binding.disable(GlConstants.BLEND);
		binding.disable(GlConstants.CULL_FACE);
		binding.disable(GlConstants.DEPTH_TEST);
		binding.disable(GlConstants.STENCIL_TEST);
		binding.disable(GlConstants.SCISSOR_TEST);
		binding.getStateBinding().colorMask(true, true, true, true);

		textures.activeTexture(GlConstants.TEXTURE0);
		textures.bindTexture(GlConstants.TEXTURE_2D, texture.getId());
		textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlConstants.LINEAR);
		textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlConstants.LINEAR);
		textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_S, GlConstants.CLAMP_TO_EDGE);
		textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_T, GlConstants.CLAMP_TO_EDGE);

		final IGlBufferBinding vertices = binding.getBufferBinding();
		bridge.getVertexInput().bind();
		vertices.bufferData(GlConstants.ARRAY_BUFFER, this.quad, GlConstants.STREAM_DRAW);
		vertices.enableVertexAttribArray(VertexAttribute.TEXTURE_COORDINATE.getLocation());
		vertices.disableVertexAttribArray(VertexAttribute.COLOR.getLocation());
		vertices.disableVertexAttribArray(VertexAttribute.NORMAL.getLocation());

		final int target = frameBuffer.genFramebuffer();
		frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, target);
		try {
			chain.forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
				textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_BASE_LEVEL, level - 1);
				textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL, level - 1);
				frameBuffer.framebufferTexture2D(GlConstants.FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, texture.getId(), level);
				binding.getStateBinding().viewport(0, 0, targetWidth, targetHeight);
				vertices.drawArrays(GlConstants.TRIANGLES, 0, 6);
			});
		} finally {
			textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_BASE_LEVEL, 0);
			textures.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL, chain.getLevels() - 1);
			frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, 0);
			frameBuffer.deleteFramebuffer(target);
			texture.forgetSampling();
		}
	}

	private GlShader getShader(final GlRenderBridge bridge) {
		if (this.shader == null) {
			final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, "out vec2 vTexCoord;\n\nvoid main() {\n    vTexCoord = aTexCoord;\n    gl_Position = vec4(aPosition, 1.0);\n}");
			final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, "in vec2 vTexCoord;\n\nuniform sampler2D tex;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord);\n}");
			this.shader = GlShader.create(bridge, vertex, fragment, BlendState.DISABLED);
		}
		return this.shader;
	}

}