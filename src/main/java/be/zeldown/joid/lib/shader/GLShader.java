package be.zeldown.joid.lib.shader;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float3Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4ArrayUniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatArrayUniform;
import be.zeldown.joid.lib.shader.uniform.FloatMatrixUniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import be.zeldown.joid.lib.shader.uniform.SamplerUniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloat2Uniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloat3Uniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloat4ArrayUniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloat4Uniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloatArrayUniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloatMatrixUniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectFloatUniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectIntUniform;
import be.zeldown.joid.lib.shader.uniform.impl.DirectSamplerUniform;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class GLShader implements IGLShader {

	private final Map<String, DirectSamplerUniform> samplerMap = new HashMap<>();
	private final Map<Integer, Integer> textureBindingMap = new HashMap<>();
	private final Map<String, Integer> uniformLocationCache = new HashMap<>();

	@NonNull private final String vertSource;
	@NonNull private final String fragSource;
	@NonNull private final ShaderBlendState blendState;

	private final int program;
	private final int vertShader;
	private final int fragShader;

	private boolean active = false;
	private boolean bound = false;
	private int prevActiveTexture;
	private ShaderBlendState prevBlendState;

	private final FloatBuffer modelMatrixBuffer;
	private final FloatBuffer viewMatrixBuffer;
	private final FloatBuffer projectionMatrixBuffer;
	private FloatMatrixUniform modelMatrixUniform;
	private FloatMatrixUniform viewMatrixUniform;
	private FloatMatrixUniform projectionMatrixUniform;
	private boolean transformationSupported = false;

	public GLShader(final @NonNull String vertSource, final @NonNull String fragSource, final @NonNull ShaderBlendState blendState) {
		this.vertSource = vertSource;
		this.fragSource = fragSource;
		this.blendState = blendState;

		this.program = GL20.glCreateProgram();
		this.vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		this.fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

		this.modelMatrixBuffer = this.createFloatBuffer(16);
		this.viewMatrixBuffer = this.createFloatBuffer(16);
		this.projectionMatrixBuffer = this.createFloatBuffer(16);

		this.buildShader();

		this.setupTransformationUniforms();
	}

	private FloatBuffer createFloatBuffer(final int capacity) {
		final ByteBuffer bb = ByteBuffer.allocateDirect(capacity * 4);
		bb.order(ByteOrder.nativeOrder());
		return bb.asFloatBuffer();
	}

	private void setupTransformationUniforms() {
		try {
			this.uniformLocationCache.put("uModelMatrix", GL20.glGetUniformLocation(this.program, "uModelMatrix"));
			this.uniformLocationCache.put("uViewMatrix", GL20.glGetUniformLocation(this.program, "uViewMatrix"));
			this.uniformLocationCache.put("uProjectionMatrix", GL20.glGetUniformLocation(this.program, "uProjectionMatrix"));

			final int modelLoc = this.uniformLocationCache.get("uModelMatrix");
			final int viewLoc = this.uniformLocationCache.get("uViewMatrix");
			final int projLoc = this.uniformLocationCache.get("uProjectionMatrix");

			if (modelLoc != -1 && viewLoc != -1 && projLoc != -1) {
				this.modelMatrixUniform = new DirectFloatMatrixUniform(modelLoc);
				this.viewMatrixUniform = new DirectFloatMatrixUniform(viewLoc);
				this.projectionMatrixUniform = new DirectFloatMatrixUniform(projLoc);
				this.transformationSupported = true;
			}
		} catch (final Exception e) {
			System.out.println("This shader does not support transformations: " + e.getMessage());
			this.transformationSupported = false;
		}
	}

	public static IGLShader from(final @NonNull InputStream vert, final @NonNull InputStream frag, final @NonNull ShaderBlendState state) {
		try {
			final byte[] vertBytes = IOUtils.toByteArray(vert);
			final byte[] fragBytes = IOUtils.toByteArray(frag);
			return new GLShader(
					new String(vertBytes, StandardCharsets.UTF_8),
					new String(fragBytes, StandardCharsets.UTF_8),
					state
					);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void updateModelMatrix(final @NonNull float[] matrix) {
		if (!this.transformationSupported) {
			return;
		}

		this.modelMatrixBuffer.clear();
		this.modelMatrixBuffer.put(matrix);
		this.modelMatrixBuffer.flip();

		if (this.bound) {
			this.modelMatrixUniform.setValue(matrix);
		}
	}

	@Override
	public void updateViewMatrix(final @NonNull float[] matrix) {
		if (!this.transformationSupported) {
			return;
		}

		this.viewMatrixBuffer.clear();
		this.viewMatrixBuffer.put(matrix);
		this.viewMatrixBuffer.flip();

		if (this.bound) {
			this.viewMatrixUniform.setValue(matrix);
		}
	}

	@Override
	public void updateProjectionMatrix(final @NonNull float[] matrix) {
		if (!this.transformationSupported) {
			return;
		}

		this.projectionMatrixBuffer.clear();
		this.projectionMatrixBuffer.put(matrix);
		this.projectionMatrixBuffer.flip();

		if (this.bound) {
			this.projectionMatrixUniform.setValue(matrix);
		}
	}

	@Override
	public void captureCurrentTransformations() {
		if (!this.transformationSupported) {
			return;
		}

		final FloatBuffer modelViewBuffer = this.createFloatBuffer(16);
		final FloatBuffer projectionBuffer = this.createFloatBuffer(16);

		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);

		if (this.bound) {
			this.modelMatrixUniform.setValue(this.toArray(modelViewBuffer));
			this.viewMatrixUniform.setValue(new float[] {
					1, 0, 0, 0,
					0, 1, 0, 0,
					0, 0, 1, 0,
					0, 0, 0, 1
			});
			this.projectionMatrixUniform.setValue(this.toArray(projectionBuffer));
		}
	}

	private float[] toArray(final FloatBuffer buffer) {
		buffer.rewind();
		final float[] array = new float[buffer.remaining()];
		buffer.get(array);
		return array;
	}

	@Override
	public void bind() {
		this.prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		this.prevBlendState = ShaderBlendState.create();

		GL20.glUseProgram(this.program);

		this.blendState.apply();
		for (final DirectSamplerUniform sampler : this.samplerMap.values()) {
			this.bindTexture(sampler.getTextureUnit(), sampler.getTextureId());
		}

		if (this.transformationSupported) {
			this.captureCurrentTransformations();
		}

		this.bound = true;
	}

	@Override
	public void unbind() {
		GL20.glUseProgram(0);

		for (final Map.Entry<Integer, Integer> texture : this.textureBindingMap.entrySet()) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + texture.getKey());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getValue());
		}

		this.textureBindingMap.clear();
		GL13.glActiveTexture(this.prevActiveTexture);
		if (this.prevBlendState != null) {
			this.prevBlendState.apply();
		}

		this.bound = false;
	}

	@Override
	public void bindTexture(final int textureUnit, final int textureId) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		this.textureBindingMap.computeIfAbsent(textureUnit, unit -> GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	private void buildShader() {
		try {
			GL20.glShaderSource(this.vertShader, this.vertSource);
			GL20.glCompileShader(this.vertShader);
			this.checkShaderCompilation(this.vertShader, "Vertex");

			GL20.glShaderSource(this.fragShader, this.fragSource);
			GL20.glCompileShader(this.fragShader);
			this.checkShaderCompilation(this.fragShader, "Fragment");

			GL20.glAttachShader(this.program, this.vertShader);
			GL20.glAttachShader(this.program, this.fragShader);

			GL20.glLinkProgram(this.program);
			if (GL20.glGetProgrami(this.program, GL20.GL_LINK_STATUS) != GL11.GL_TRUE) {
				final String error = GL20.glGetProgramInfoLog(this.program, 1024);
				System.err.println("Failed to link shader program: " + error);
				this.active = false;
				return;
			}

			GL20.glValidateProgram(this.program);
			if (GL20.glGetProgrami(this.program, GL20.GL_VALIDATE_STATUS) != GL11.GL_TRUE) {
				final String error = GL20.glGetProgramInfoLog(this.program, 1024);
				System.err.println("Failed to validate shader program: " + error);
				this.active = false;
				return;
			}

			GL20.glDetachShader(this.program, this.vertShader);
			GL20.glDetachShader(this.program, this.fragShader);
			GL20.glDeleteShader(this.vertShader);
			GL20.glDeleteShader(this.fragShader);

			this.active = true;
		} catch (final Exception e) {
			System.err.println("Exception during shader compilation: " + e.getMessage());
			e.printStackTrace();
			this.active = false;
		}
	}

	private void checkShaderCompilation(final int shader, final @NonNull String shaderType) {
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE) {
			final String error = GL20.glGetShaderInfoLog(shader, 1024);
			throw new RuntimeException(shaderType + " shader compilation failed: " + error);
		}
	}

	private int getUniformLocation(final @NonNull String uniformName) {
		return this.uniformLocationCache.computeIfAbsent(uniformName, name -> {
			final int location = GL20.glGetUniformLocation(this.program, name);
			return location;
		});
	}

	@Override
	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name) {
		DirectSamplerUniform sampler = this.samplerMap.get(name);
		if (sampler != null) {
			return sampler;
		}

		final int location = this.getUniformLocation(name);
		sampler = new DirectSamplerUniform(location, this.samplerMap.size(), this);
		this.samplerMap.put(name, sampler);
		return sampler;
	}

	@Override
	public @NonNull IntUniform getIntUniform(final @NonNull String name) {
		return new DirectIntUniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name) {
		return new DirectFloatArrayUniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull FloatUniform getFloatUniform(final @NonNull String name) {
		return new DirectFloatUniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull Float2Uniform getFloat2Uniform(final @NonNull String name) {
		return new DirectFloat2Uniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull Float3Uniform getFloat3Uniform(final @NonNull String name) {
		return new DirectFloat3Uniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull Float4Uniform getFloat4Uniform(final @NonNull String name) {
		return new DirectFloat4Uniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name) {
		return new DirectFloatMatrixUniform(this.getUniformLocation(name));
	}

	@Override
	public @NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name) {
		return new DirectFloat4ArrayUniform(this.getUniformLocation(name));
	}

	@Override
	public boolean isBound() {
		return this.bound;
	}

	@Override
	public boolean supportsTransformations() {
		return this.transformationSupported;
	}

}