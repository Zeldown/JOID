package dev.joid.backend.lwjgl2.binding;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.joid.base.opengl.binding.IGlProgramBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlProgramBinding implements IGlProgramBinding {

	public static @NonNull Lwjgl2GlProgramBinding create() {
		return new Lwjgl2GlProgramBinding();
	}

	@Override
	public int createProgram() {
		return GL20.glCreateProgram();
	}

	@Override
	public void useProgram(final int program) {
		GL20.glUseProgram(program);
	}

	@Override
	public void linkProgram(final int program) {
		GL20.glLinkProgram(program);
	}

	@Override
	public int getProgrami(final int program, final int name) {
		return GL20.glGetProgrami(program, name);
	}

	@Override
	public @NonNull String getProgramInfoLog(final int program) {
		return GL20.glGetProgramInfoLog(program, Math.max(1, GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH)));
	}

	@Override
	public int createShader(final int type) {
		return GL20.glCreateShader(type);
	}

	@Override
	public void deleteShader(final int shader) {
		GL20.glDeleteShader(shader);
	}

	@Override
	public void compileShader(final int shader) {
		GL20.glCompileShader(shader);
	}

	@Override
	public int getShaderi(final int shader, final int name) {
		return GL20.glGetShaderi(shader, name);
	}

	@Override
	public @NonNull String getShaderInfoLog(final int shader) {
		return GL20.glGetShaderInfoLog(shader, Math.max(1, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH)));
	}

	@Override
	public void shaderSource(final int shader, final @NonNull String source) {
		GL20.glShaderSource(shader, source);
	}

	@Override
	public void attachShader(final int program, final int shader) {
		GL20.glAttachShader(program, shader);
	}

	@Override
	public void detachShader(final int program, final int shader) {
		GL20.glDetachShader(program, shader);
	}

	@Override
	public void bindAttribLocation(final int program, final int index, final @NonNull String name) {
		GL20.glBindAttribLocation(program, index, name);
	}

	@Override
	public void bindFragDataLocation(final int program, final int color, final @NonNull String name) {
		GL30.glBindFragDataLocation(program, color, name);
	}

	@Override
	public void uniform1i(final int location, final int value) {
		GL20.glUniform1i(location, value);
	}

	@Override
	public int getUniformLocation(final int program, final @NonNull String name) {
		return GL20.glGetUniformLocation(program, name);
	}

	@Override
	public void uniformiv(final int location, final int components, final @NonNull IntBuffer values) {
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

	@Override
	public void uniformuiv(final int location, final int components, final @NonNull IntBuffer values) {
		switch (components) {
		case 2:
			GL30.glUniform2u(location, values);
			break;
		case 3:
			GL30.glUniform3u(location, values);
			break;
		case 4:
			GL30.glUniform4u(location, values);
			break;
		default:
			GL30.glUniform1u(location, values);
			break;
		}
	}

	@Override
	public void uniformfv(final int location, final int components, final @NonNull FloatBuffer values) {
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

	@Override
	public void uniformMatrixfv(final int location, final int columns, final @NonNull FloatBuffer values) {
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

}