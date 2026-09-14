package be.zeldown.joid.impl.lwjgl2.render.shader;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.BooleanUniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.Float3Uniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.Float4ArrayUniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.FloatArrayUniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.FloatMatrixUniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.FloatUniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.IntUniform;
import be.zeldown.joid.impl.lwjgl2.render.shader.uniform.SamplerUniform;
import be.zeldown.joid.impl.lwjgl2.render.state.BlendSnapshot;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shader implements IShader {

	private static final Map<Integer, Shader> SHADER_MAP = new HashMap<>();

	private final Map<String, SamplerUniform> samplerMap;
	private final Map<String, Integer>        locationMap;
	private final int                         program;
	private final BlendState                  blend;
	private final boolean                     active;

	private boolean       bound;
	private BlendSnapshot previousBlend;

	private Shader(final int program, final BlendState blend, final boolean active) {
		this.samplerMap  = new HashMap<>();
		this.locationMap = new HashMap<>();
		this.program     = program;
		this.blend       = blend;
		this.active      = active;
	}

	public static @NonNull Shader create(final String vertex, final String fragment, final BlendState blend) {
		final int program = GL20.glCreateProgram();
		final Shader shader = new Shader(program, blend, Shader.link(program, vertex, fragment));
		Shader.SHADER_MAP.put(program, shader);
		return shader;
	}

	public static Shader fromProgram(final int program) {
		if (program == 0) {
			return null;
		}

		final Shader shader = Shader.SHADER_MAP.get(program);
		return shader != null ? shader : new Shader(program, null, true);
	}

	@Override
	public void bind() {
		this.previousBlend = BlendSnapshot.capture();
		GL20.glUseProgram(this.program);
		if (this.blend != null) {
			BridgeHandler.RENDER.get().blend(this.blend);
		}

		this.samplerMap.values().forEach(SamplerUniform::bind);

		final int lighting = this.getLocation("uLighting");
		if (lighting != -1) {
			GL20.glUniform1i(lighting, GL11.glIsEnabled(GL11.GL_LIGHTING) ? 1 : 0);
		}

		this.bound = true;
	}

	@Override
	public void unbind() {
		GL20.glUseProgram(0);
		this.samplerMap.values().forEach(SamplerUniform::unbind);
		if (this.previousBlend != null) {
			this.previousBlend.restore();
			this.previousBlend = null;
		}

		this.bound = false;
	}

	@Override
	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name) {
		return this.samplerMap.computeIfAbsent(name, key -> new SamplerUniform(this.getLocation(key), this.samplerMap.size() + 1, this));
	}

	@Override
	public @NonNull BooleanUniform getBooleanUniform(final @NonNull String name) {
		return new BooleanUniform(this.getLocation(name));
	}

	@Override
	public @NonNull IntUniform getIntUniform(final @NonNull String name) {
		return new IntUniform(this.getLocation(name));
	}

	@Override
	public @NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name) {
		return new FloatArrayUniform(this.getLocation(name));
	}

	@Override
	public @NonNull FloatUniform getFloatUniform(final @NonNull String name) {
		return new FloatUniform(this.getLocation(name));
	}

	@Override
	public @NonNull Float2Uniform getFloat2Uniform(final @NonNull String name) {
		return new Float2Uniform(this.getLocation(name));
	}

	@Override
	public @NonNull Float3Uniform getFloat3Uniform(final @NonNull String name) {
		return new Float3Uniform(this.getLocation(name));
	}

	@Override
	public @NonNull Float4Uniform getFloat4Uniform(final @NonNull String name) {
		return new Float4Uniform(this.getLocation(name));
	}

	@Override
	public @NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name) {
		return new FloatMatrixUniform(this.getLocation(name));
	}

	@Override
	public @NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name) {
		return new Float4ArrayUniform(this.getLocation(name));
	}

	public int getLocation(final String name) {
		return this.locationMap.computeIfAbsent(name, key -> GL20.glGetUniformLocation(this.program, key));
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