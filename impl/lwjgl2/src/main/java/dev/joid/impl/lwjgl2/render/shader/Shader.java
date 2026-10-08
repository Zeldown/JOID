package dev.joid.impl.lwjgl2.render.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.RenderBridge;
import dev.joid.impl.lwjgl2.render.state.BlendSnapshot;
import dev.joid.impl.lwjgl2.render.texture.Texture;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shader extends dev.joid.lib.bridge.render.shader.Shader {

	public static final int NORMAL_LOCATION = 6;

	private static final Map<Integer, Shader> SHADER_MAP = new HashMap<>();

	private final int                          program;
	private final boolean                      active;
	private final BlendState                   blend;
	private final Map<String, Integer>         locationMap;
	private final Map<UniformSampler, Integer> previousTextureMap;

	private boolean       bound;
	private BlendSnapshot previousBlend;

	private Shader(final int program, final BlendState blend, final boolean active, final UniformBlock block, final List<ShaderVariable> samplers) {
		super(block, samplers);
		this.locationMap        = new HashMap<>();
		this.previousTextureMap = new HashMap<>();
		this.program            = program;
		this.blend              = blend;
		this.active             = active;
	}

	public static @NonNull Shader create(final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		final ShaderTranslator translator = ShaderTranslator.create();
		final int program = GL20.glCreateProgram();
		final boolean active = Shader.link(program, translator.translateVertex(vertex, fragment), translator.translateFragment(vertex, fragment));
		final Shader shader = new Shader(program, blend, active, translator.createBlock(vertex, fragment), translator.getSamplers(vertex, fragment));
		Shader.SHADER_MAP.put(program, shader);
		return shader;
	}

	public static Shader fromProgram(final int program) {
		if (program == 0) {
			return null;
		}

		final Shader shader = Shader.SHADER_MAP.get(program);
		return shader != null ? shader : new Shader(program, null, true, UniformBlock.create(new ArrayList<>(), ""), new ArrayList<>());
	}

	public static Shader current() {
		return Shader.SHADER_MAP.get(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM));
	}

	@Override
	public void bind() {
		this.previousBlend = BlendSnapshot.capture();
		GL20.glUseProgram(this.program);
		if (this.blend != null) {
			BridgeHandler.RENDER.get().blend(this.blend);
		}

		this.bound = true;
	}

	@Override
	public void unbind() {
		GL20.glUseProgram(0);
		this.previousTextureMap.forEach((sampler, texture) -> {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + sampler.getUnit());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		});
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		this.previousTextureMap.clear();

		if (this.previousBlend != null) {
			this.previousBlend.restore();
			this.previousBlend = null;
		}

		this.bound = false;
	}

	public void use() {
		super.getBlock().value(ShaderBuiltin.LIGHTING.getIdentifier(), GL11.glIsEnabled(GL11.GL_LIGHTING)).upload(this::upload);
		super.getSamplerMap().values().forEach(this::apply);
	}

	private void upload(final UniformMember member) {
		final int location = this.getLocation(member.getName());
		if (location == -1) {
			return;
		}

		if (member.getType().isInteger()) {
			Shader.upload(location, member.getType().getComponents(), member.getValues().asIntBuffer());
		} else if (member.getType().isMatrix()) {
			Shader.uploadMatrix(location, member.getType().getColumns(), member.getValues().asFloatBuffer());
		} else {
			Shader.upload(location, member.getType().getComponents(), member.getValues().asFloatBuffer());
		}
	}

	private void apply(final UniformSampler sampler) {
		if (sampler.getTexture() == null) {
			return;
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0 + sampler.getUnit());
		this.previousTextureMap.computeIfAbsent(sampler, key -> GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, ((Texture) sampler.getTexture()).getId());
		RenderBridge.applyTextureParameters(sampler.getFilter(), sampler.getWrap());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL20.glUniform1i(this.getLocation(sampler.getName()), sampler.getUnit());
	}

	private int getLocation(final String name) {
		return this.locationMap.computeIfAbsent(name, key -> GL20.glGetUniformLocation(this.program, key));
	}

	private static void upload(final int location, final int components, final IntBuffer values) {
		switch (components) {
		case 2:
			GL20.glUniform2(location, values);
			break;
		case 3:
			GL20.glUniform3(location, values);
			break;
		case 4:
			GL20.glUniform4(location, values);
			break;
		default:
			GL20.glUniform1(location, values);
			break;
		}
	}

	private static void upload(final int location, final int components, final FloatBuffer values) {
		switch (components) {
		case 2:
			GL20.glUniform2(location, values);
			break;
		case 3:
			GL20.glUniform3(location, values);
			break;
		case 4:
			GL20.glUniform4(location, values);
			break;
		default:
			GL20.glUniform1(location, values);
			break;
		}
	}

	private static void uploadMatrix(final int location, final int columns, final FloatBuffer values) {
		switch (columns) {
		case 2:
			GL20.glUniformMatrix2(location, false, values);
			break;
		case 3:
			GL20.glUniformMatrix3(location, false, values);
			break;
		default:
			GL20.glUniformMatrix4(location, false, values);
			break;
		}
	}

	private static boolean link(final int program, final String vertexSource, final String fragmentSource) {
		final int vertex = Shader.compile(GL20.GL_VERTEX_SHADER, vertexSource);
		final int fragment = Shader.compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);
		if (vertex == 0 || fragment == 0) {
			GL20.glDeleteShader(vertex);
			GL20.glDeleteShader(fragment);
			return false;
		}

		GL20.glAttachShader(program, vertex);
		GL20.glAttachShader(program, fragment);
		GL20.glBindAttribLocation(program, Shader.NORMAL_LOCATION, "joid_Normal");
		GL20.glLinkProgram(program);
		GL20.glValidateProgram(program);

		final boolean linked = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_TRUE && GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_TRUE;
		if (!linked) {
			System.err.println("Failed to link shader program: " + GL20.glGetProgramInfoLog(program, 1024));
		}

		GL20.glDetachShader(program, vertex);
		GL20.glDetachShader(program, fragment);
		GL20.glDeleteShader(vertex);
		GL20.glDeleteShader(fragment);
		return linked;
	}

	private static int compile(final int type, final String source) {
		final int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE) {
			System.err.println((type == GL20.GL_VERTEX_SHADER ? "Vertex" : "Fragment") + " shader compilation failed: " + GL20.glGetShaderInfoLog(shader, 1024));
			GL20.glDeleteShader(shader);
			return 0;
		}

		return shader;
	}

}