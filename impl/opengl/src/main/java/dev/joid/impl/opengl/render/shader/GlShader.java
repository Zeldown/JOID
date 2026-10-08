package dev.joid.impl.opengl.render.shader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.joid.impl.opengl.binding.GlConstants;
import dev.joid.impl.opengl.binding.IGlBinding;
import dev.joid.impl.opengl.binding.IGlBufferBinding;
import dev.joid.impl.opengl.binding.IGlProgramBinding;
import dev.joid.impl.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlShader extends Shader {

	private final IGlBinding           binding;
	private final int                  program;
	private final int                  uniformBuffer;
	private final Map<String, Integer> locationMap;

	private GlShader(final GlRenderBridge bridge, final int program, final boolean active, final BlendState blend, final UniformBlock block, final List<ShaderVariable> samplers) {
		super(bridge, blend, active, block, samplers);
		this.binding       = bridge.getBinding();
		this.program       = program;
		this.locationMap   = new HashMap<>();
		this.uniformBuffer = this.binding.getBufferBinding().genBuffer();

		final IGlBufferBinding buffer = this.binding.getBufferBinding();
		buffer.bindBuffer(GlConstants.UNIFORM_BUFFER, this.uniformBuffer);
		buffer.bufferData(GlConstants.UNIFORM_BUFFER, block.getData(), GlConstants.DYNAMIC_DRAW);
		buffer.bindBuffer(GlConstants.UNIFORM_BUFFER, 0);

		final IGlProgramBinding programs = this.binding.getProgramBinding();
		final int index = programs.getUniformBlockIndex(program, GlslShaderTranslator.BLOCK);
		if (active && index != GlConstants.INVALID_INDEX) {
			programs.uniformBlockBinding(program, index, 0);
		}
	}

	public static @NonNull GlShader create(final @NonNull GlRenderBridge bridge, final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		final GlslShaderTranslator translator = bridge.getStrategies().createTranslator();
		final IGlProgramBinding programs = bridge.getBinding().getProgramBinding();
		final int program = programs.createProgram();
		final boolean active = GlShader.link(programs, program, translator.translateVertex(vertex, fragment), translator.translateFragment(vertex, fragment));
		return new GlShader(bridge, program, active, blend, translator.createBlock(vertex, fragment), translator.getSamplers(vertex, fragment));
	}

	public void use(final @NonNull RenderState state) {
		this.binding.getProgramBinding().useProgram(this.program);
		final IGlBufferBinding buffer = this.binding.getBufferBinding();
		if (super.builtins(state, super.getBridge().getProjection().getMatrix(), super.getBridge().getModelView()).pack()) {
			buffer.bindBuffer(GlConstants.UNIFORM_BUFFER, this.uniformBuffer);
			buffer.bufferSubData(GlConstants.UNIFORM_BUFFER, 0L, super.getBlock().getData());
		}

		buffer.bindBufferBase(GlConstants.UNIFORM_BUFFER, 0, this.uniformBuffer);
	}

	public int getLocation(final @NonNull String name) {
		return this.locationMap.computeIfAbsent(name, key -> this.binding.getProgramBinding().getUniformLocation(this.program, key));
	}

	private static boolean link(final IGlProgramBinding programs, final int program, final String vertexSource, final String fragmentSource) {
		final int vertex = GlShader.compile(programs, GlConstants.VERTEX_SHADER, vertexSource);
		final int fragment = GlShader.compile(programs, GlConstants.FRAGMENT_SHADER, fragmentSource);
		if (vertex == 0 || fragment == 0) {
			programs.deleteShader(vertex);
			programs.deleteShader(fragment);
			return false;
		}

		programs.attachShader(program, vertex);
		programs.attachShader(program, fragment);
		programs.linkProgram(program);

		final boolean linked = programs.getProgrami(program, GlConstants.LINK_STATUS) == GlConstants.TRUE;
		if (!linked) {
			System.err.println("Failed to link shader program: " + programs.getProgramInfoLog(program));
		}

		programs.detachShader(program, vertex);
		programs.detachShader(program, fragment);
		programs.deleteShader(vertex);
		programs.deleteShader(fragment);
		return linked;
	}

	private static int compile(final IGlProgramBinding programs, final int type, final String source) {
		final int shader = programs.createShader(type);
		programs.shaderSource(shader, source);
		programs.compileShader(shader);
		if (programs.getShaderi(shader, GlConstants.COMPILE_STATUS) != GlConstants.TRUE) {
			System.err.println((type == GlConstants.VERTEX_SHADER ? "Vertex" : "Fragment") + " shader compilation failed: " + programs.getShaderInfoLog(shader));
			programs.deleteShader(shader);
			return 0;
		}

		return shader;
	}

}