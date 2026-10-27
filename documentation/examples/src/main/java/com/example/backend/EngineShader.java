package com.example.backend;

import com.example.backend.engine.EngineDevice;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderTranslation;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;

public final class EngineShader extends Shader {

	private final EngineDevice device;

	private int program;

	public EngineShader(final EngineRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		// #region translator
		super(bridge, new GlslShaderTranslator(GlslDialect.GLSL_330, UniformLayout.BLOCK), vertex, fragment, blend);
		// #endregion
		this.device = bridge.getDevice();
	}

	public int getProgram() {
		return this.program;
	}

	// #region program
	public void upload(final RenderState state, final VertexBuffer buffer) {
		final float[] projection = super.getBridge().getProjection().getRawMatrix();
		final UniformBlock block = super.builtins(state, buffer, projection, super.getBridge().getModelView());
		if (block.pack()) {
			this.device.uniforms(this.program, block.getData());
		}
	}

	@Override
	protected void compileProgram(final ShaderTranslation translation) {
		this.program = this.device.createProgram(translation.getVertex(), translation.getFragment());
		if (this.program == 0) {
			JOIDLog.warn("The engine cannot compile a shader program, fix its sources from the log of the engine");
		}
	}

	@Override
	protected boolean isProgramActive() {
		return this.program != 0;
	}

	@Override
	protected void deleteProgram() {
		this.device.deleteProgram(this.program);
		this.program = 0;
	}
	// #endregion

}