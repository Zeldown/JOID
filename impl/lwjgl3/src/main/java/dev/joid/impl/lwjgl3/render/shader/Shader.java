package dev.joid.impl.lwjgl3.render.shader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL33C;

import dev.joid.impl.lwjgl3.render.RenderBridge;
import dev.joid.impl.lwjgl3.render.texture.Texture;
import dev.joid.lib.bridge.render.shader.source.BlockShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shader extends dev.joid.lib.bridge.render.shader.Shader {

	private final int                  program;
	private final int                  uniformBuffer;
	private final Map<String, Integer> locationMap;

	private Shader(final RenderBridge bridge, final int program, final boolean active, final BlendState blend, final UniformBlock block, final List<ShaderVariable> samplers) {
		super(bridge, blend, active, block, samplers);
		this.program       = program;
		this.locationMap   = new HashMap<>();
		this.uniformBuffer = GL15C.glGenBuffers();

		GL15C.glBindBuffer(GL31C.GL_UNIFORM_BUFFER, this.uniformBuffer);
		GL15C.glBufferData(GL31C.GL_UNIFORM_BUFFER, block.getData(), GL15C.GL_DYNAMIC_DRAW);
		GL15C.glBindBuffer(GL31C.GL_UNIFORM_BUFFER, 0);

		final int index = GL31C.glGetUniformBlockIndex(program, BlockShaderTranslator.BLOCK);
		if (active && index != GL31C.GL_INVALID_INDEX) {
			GL31C.glUniformBlockBinding(program, index, 0);
		}
	}

	public static @NonNull Shader create(final RenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		final BlockShaderTranslator translator = BlockShaderTranslator.create();
		final int program = GL20C.glCreateProgram();
		final boolean active = Shader.link(program, translator.translateVertex(vertex, fragment), translator.translateFragment(vertex, fragment));
		return new Shader(bridge, program, active, blend, translator.createBlock(vertex, fragment), translator.getSamplers(vertex, fragment));
	}

	public void use(final RenderState state) {
		GL20C.glUseProgram(this.program);
		if (super.builtins(state, super.getBridge().getProjection().getMatrix(), super.getBridge().getModelView()).pack()) {
			GL15C.glBindBuffer(GL31C.GL_UNIFORM_BUFFER, this.uniformBuffer);
			GL15C.glBufferSubData(GL31C.GL_UNIFORM_BUFFER, 0L, super.getBlock().getData());
		}

		GL31C.glBindBufferBase(GL31C.GL_UNIFORM_BUFFER, 0, this.uniformBuffer);
		super.getSamplerMap().values().forEach(this::apply);
	}

	private void apply(final UniformSampler sampler) {
		final int location = this.locationMap.computeIfAbsent(sampler.getName(), name -> GL20C.glGetUniformLocation(this.program, name));
		if (sampler.getTexture() == null || location == -1) {
			return;
		}

		final Texture texture = (Texture) sampler.getTexture();
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + sampler.getUnit());
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture.getId());
		GL33C.glBindSampler(sampler.getUnit(), ((RenderBridge) super.getBridge()).getSampler(sampler.getFilter(), sampler.getWrap(), texture.isMipmapped()));
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
		GL20C.glUniform1i(location, sampler.getUnit());
	}

	private static boolean link(final int program, final String vertexSource, final String fragmentSource) {
		final int vertex = Shader.compile(GL20C.GL_VERTEX_SHADER, vertexSource);
		final int fragment = Shader.compile(GL20C.GL_FRAGMENT_SHADER, fragmentSource);
		if (vertex == 0 || fragment == 0) {
			GL20C.glDeleteShader(vertex);
			GL20C.glDeleteShader(fragment);
			return false;
		}

		GL20C.glAttachShader(program, vertex);
		GL20C.glAttachShader(program, fragment);
		GL20C.glLinkProgram(program);

		final boolean linked = GL20C.glGetProgrami(program, GL20C.GL_LINK_STATUS) == GL11C.GL_TRUE;
		if (!linked) {
			System.err.println("Failed to link shader program: " + GL20C.glGetProgramInfoLog(program));
		}

		GL20C.glDetachShader(program, vertex);
		GL20C.glDetachShader(program, fragment);
		GL20C.glDeleteShader(vertex);
		GL20C.glDeleteShader(fragment);
		return linked;
	}

	private static int compile(final int type, final String source) {
		final int shader = GL20C.glCreateShader(type);
		GL20C.glShaderSource(shader, source);
		GL20C.glCompileShader(shader);
		if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) != GL11C.GL_TRUE) {
			System.err.println((type == GL20C.GL_VERTEX_SHADER ? "Vertex" : "Fragment") + " shader compilation failed: " + GL20C.glGetShaderInfoLog(shader));
			GL20C.glDeleteShader(shader);
			return 0;
		}

		return shader;
	}

}