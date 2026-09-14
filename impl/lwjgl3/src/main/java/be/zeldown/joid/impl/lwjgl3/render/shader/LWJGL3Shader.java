package be.zeldown.joid.impl.lwjgl3.render.shader;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.LWJGL3RenderBridge;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3BooleanUniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3Float2Uniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3Float3Uniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3Float4ArrayUniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3Float4Uniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3FloatArrayUniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3FloatMatrixUniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3FloatUniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3IntUniform;
import be.zeldown.joid.impl.lwjgl3.render.shader.uniform.LWJGL3SamplerUniform;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.BooleanUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float3Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.IntUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.SamplerUniform;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.state.RenderState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class LWJGL3Shader implements IShader {

	private static final String ALPHA_TEST_MAIN = "\nuniform bool joid_AlphaTest;\nuniform float joid_AlphaThreshold;\n\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n";

	private final LWJGL3RenderBridge                bridge;
	private final int                               program;
	private final boolean                           active;
	private final BlendState                        blend;
	private final Map<String, Integer>              locationMap;
	private final Map<String, LWJGL3SamplerUniform> samplerMap;
	private final Map<Integer, Runnable>            uniformQueue;

	private boolean    bound;
	private BlendState previousBlend;

	private LWJGL3Shader(final LWJGL3RenderBridge bridge, final int program, final boolean active, final BlendState blend) {
		this.bridge       = bridge;
		this.program      = program;
		this.active       = active;
		this.blend        = blend;
		this.locationMap  = new HashMap<>();
		this.samplerMap   = new HashMap<>();
		this.uniformQueue = new HashMap<>();
	}

	public static @NonNull LWJGL3Shader create(final LWJGL3RenderBridge bridge, final String vertex, final String fragment, final BlendState blend) {
		final int program = GL20C.glCreateProgram();
		final String alphaTestedFragment = fragment.replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_main()") + LWJGL3Shader.ALPHA_TEST_MAIN;
		return new LWJGL3Shader(bridge, program, LWJGL3Shader.link(program, vertex, alphaTestedFragment), blend);
	}

	@Override
	public void bind() {
		this.previousBlend = this.bridge.getState().getBlend();
		this.bridge.shader(this);
		this.bridge.blend(this.blend);
		this.bound = true;
	}

	@Override
	public void unbind() {
		this.bridge.shader(null);
		if (this.previousBlend != null) {
			this.bridge.blend(this.previousBlend);
			this.previousBlend = null;
		}

		this.bound = false;
	}

	@Override
	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name) {
		return this.samplerMap.computeIfAbsent(name, key -> new LWJGL3SamplerUniform(this, this.getLocation(key), this.samplerMap.size() + 1));
	}

	@Override
	public @NonNull BooleanUniform getBooleanUniform(final @NonNull String name) {
		return new LWJGL3BooleanUniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull IntUniform getIntUniform(final @NonNull String name) {
		return new LWJGL3IntUniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name) {
		return new LWJGL3FloatArrayUniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull FloatUniform getFloatUniform(final @NonNull String name) {
		return new LWJGL3FloatUniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull Float2Uniform getFloat2Uniform(final @NonNull String name) {
		return new LWJGL3Float2Uniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull Float3Uniform getFloat3Uniform(final @NonNull String name) {
		return new LWJGL3Float3Uniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull Float4Uniform getFloat4Uniform(final @NonNull String name) {
		return new LWJGL3Float4Uniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name) {
		return new LWJGL3FloatMatrixUniform(this, this.getLocation(name));
	}

	@Override
	public @NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name) {
		return new LWJGL3Float4ArrayUniform(this, this.getLocation(name));
	}

	public void queueUniform(final int location, final Runnable upload) {
		if (location != -1) {
			this.uniformQueue.put(location, upload);
		}
	}

	public void use(final RenderState state) {
		GL20C.glUseProgram(this.program);
		this.uniformQueue.values().forEach(Runnable::run);
		this.uniformQueue.clear();

		final int projection = this.getLocation("uProjectionMatrix");
		if (projection != -1) {
			GL20C.glUniformMatrix4fv(projection, false, this.bridge.getProjection().getMatrix());
		}

		final int modelView = this.getLocation("uModelViewMatrix");
		if (modelView != -1) {
			GL20C.glUniformMatrix4fv(modelView, false, this.bridge.getModelView().getMatrix());
		}

		final int normal = this.getLocation("uNormalMatrix");
		if (normal != -1) {
			GL20C.glUniformMatrix3fv(normal, false, this.bridge.getModelView().getNormalMatrix());
		}

		final int lighting = this.getLocation("uLighting");
		if (lighting != -1) {
			GL20C.glUniform1i(lighting, state.isLighting() ? 1 : 0);
		}

		final int alphaTest = this.getLocation("joid_AlphaTest");
		if (alphaTest != -1) {
			GL20C.glUniform1i(alphaTest, state.isAlphaTest() ? 1 : 0);
			GL20C.glUniform1f(this.getLocation("joid_AlphaThreshold"), state.getAlphaThreshold());
		}

		this.samplerMap.values().forEach(sampler -> sampler.apply(this.bridge));
	}

	private int getLocation(final String name) {
		return this.locationMap.computeIfAbsent(name, key -> GL20C.glGetUniformLocation(this.program, key));
	}

	private static boolean link(final int program, final String vertexSource, final String fragmentSource) {
		final int vertex = LWJGL3Shader.compile(GL20C.GL_VERTEX_SHADER, vertexSource);
		final int fragment = LWJGL3Shader.compile(GL20C.GL_FRAGMENT_SHADER, fragmentSource);
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