package be.zeldown.joid.lib.shader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import be.zeldown.joid.lib.utils.tuple.Tuple;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class GLShader implements IGLShader {

	private final Map<String, DirectSamplerUniform> samplerMap = new HashMap<>();
	private final Map<Integer, Integer> textureBindingMap = new HashMap<>();

	@NonNull private final String vertSource;
	@NonNull private final String fragSource;
	@NonNull private final ShaderBlendState blendState;

	private final int program    = GL20.glCreateProgram();
	private final int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
	private final int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

	private boolean active;
	private boolean bound;

	private int prevActiveTexture;
	private ShaderBlendState prevBlendState;

	public GLShader(final @NonNull String vertSource, final @NonNull String fragSource, final @NonNull ShaderBlendState blendState) {
		this.vertSource = vertSource;
		this.fragSource = fragSource;
		this.blendState = blendState;
		this.buildShader();
	}

	public static IGLShader from(final @NonNull InputStream vert, final @NonNull InputStream frag, final @NonNull ShaderBlendState state) {
		try {
			final byte[] vertBytes = IOUtils.toByteArray(vert);
			final byte[] fragBytes = IOUtils.toByteArray(frag);
			return new GLShader(new String(vertBytes, StandardCharsets.UTF_8), new String(fragBytes, StandardCharsets.UTF_8), state);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void bind() {
		this.prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

		for (final DirectSamplerUniform sampler : this.samplerMap.values()) {
			this.bindTexture(sampler.getTextureUnit(), sampler.getTextureId());
		}

		this.blendState.apply();
		this.prevBlendState = ShaderBlendState.create();
		this.bound = true;

		GL20.glUseProgram(this.program);
	}

	@Override
	public void unbind() {
		for (final Entry<Integer, Integer> texture : this.textureBindingMap.entrySet()) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + texture.getKey());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getValue());
		}

		this.textureBindingMap.clear();
		GL13.glActiveTexture(this.prevActiveTexture);
		if (this.prevBlendState != null) {
			this.prevBlendState.apply();
		}
		this.bound = false;

		GL20.glUseProgram(0);
	}

	public void bindTexture(final int textureUnit, final int textureId) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		this.textureBindingMap.computeIfAbsent(textureUnit, e -> GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	private void buildShader() {
		final List<Tuple<Integer, String>> shaderTuple = Arrays.asList(new Tuple<>(this.vertShader, this.vertSource), new Tuple<>(this.fragShader, this.fragSource));
		for (final Tuple<Integer, String> pair : shaderTuple) {
			final int shader = pair.getFirst();
			final String source = pair.getSecond();

			GL20.glShaderSource(shader, source);
			GL20.glCompileShader(shader);

			if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != 1) {
				System.out.println("Failed to compile shader (" + GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) + ")");
				System.out.println(GL20.glGetShaderInfoLog(shader, 1024));
				return;
			}

			GL20.glAttachShader(this.program, shader);
		}

		GL20.glLinkProgram(this.program);
		GL20.glDetachShader(this.program, this.vertShader);
		GL20.glDetachShader(this.program, this.fragShader);
		GL20.glDeleteShader(this.vertShader);
		GL20.glDeleteShader(this.fragShader);

		if (GL20.glGetProgrami(this.program, GL20.GL_LINK_STATUS) != 1) {
			System.out.println("Failed to link program (" + GL20.glGetProgrami(this.program, GL20.GL_LINK_STATUS) + ")");
			System.out.println(GL20.glGetShaderInfoLog(this.program, 1024));
			return;
		}

		GL20.glValidateProgram(this.program);
		if (GL20.glGetProgrami(this.program, GL20.GL_VALIDATE_STATUS) != 1) {
			System.out.println("Failed to validate program (" + GL20.glGetProgrami(this.program, GL20.GL_VALIDATE_STATUS) + ")");
			System.out.println(GL20.glGetShaderInfoLog(this.program, 1024));
			return;
		}

		this.setActive(true);
	}

	private int getUniformLocation(final @NonNull String uniformName){
		final int value = GL20.glGetUniformLocation(this.program, uniformName);
		return value == -1 ? null : value;
	}

	@Override
	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name) {
		DirectSamplerUniform sampler = this.samplerMap.get(name);
		if (sampler != null) {
			return sampler;
		}

		sampler = new DirectSamplerUniform(this.getUniformLocation(name), this.samplerMap.size(), this);
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

}