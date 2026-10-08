package dev.joid.backend.lwjgl3.binding;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import dev.joid.base.opengl.binding.IGlProgramBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlProgramBinding implements IGlProgramBinding {

	public static @NonNull Lwjgl3GlProgramBinding create() {
		return new Lwjgl3GlProgramBinding();
	}

	@Override
	public int createProgram() {
		return GL20C.glCreateProgram();
	}

	@Override
	public void useProgram(final int program) {
		GL20C.glUseProgram(program);
	}

	@Override
	public void linkProgram(final int program) {
		GL20C.glLinkProgram(program);
	}

	@Override
	public int getProgrami(final int program, final int name) {
		return GL20C.glGetProgrami(program, name);
	}

	@Override
	public @NonNull String getProgramInfoLog(final int program) {
		return GL20C.glGetProgramInfoLog(program);
	}

	@Override
	public int createShader(final int type) {
		return GL20C.glCreateShader(type);
	}

	@Override
	public void deleteShader(final int shader) {
		GL20C.glDeleteShader(shader);
	}

	@Override
	public void compileShader(final int shader) {
		GL20C.glCompileShader(shader);
	}

	@Override
	public int getShaderi(final int shader, final int name) {
		return GL20C.glGetShaderi(shader, name);
	}

	@Override
	public @NonNull String getShaderInfoLog(final int shader) {
		return GL20C.glGetShaderInfoLog(shader);
	}

	@Override
	public void shaderSource(final int shader, final @NonNull String source) {
		GL20C.glShaderSource(shader, source);
	}

	@Override
	public void attachShader(final int program, final int shader) {
		GL20C.glAttachShader(program, shader);
	}

	@Override
	public void detachShader(final int program, final int shader) {
		GL20C.glDetachShader(program, shader);
	}

	@Override
	public void bindAttribLocation(final int program, final int index, final @NonNull String name) {
		GL20C.glBindAttribLocation(program, index, name);
	}

	@Override
	public void bindFragDataLocation(final int program, final int color, final @NonNull String name) {
		GL30C.glBindFragDataLocation(program, color, name);
	}

	@Override
	public void uniform1i(final int location, final int value) {
		GL20C.glUniform1i(location, value);
	}

	@Override
	public int getUniformLocation(final int program, final @NonNull String name) {
		return GL20C.glGetUniformLocation(program, name);
	}

	@Override
	public void uniformiv(final int location, final int components, final @NonNull IntBuffer values) {
		switch (components) {
		case 2:
			GL20C.glUniform2iv(location, values);
			break;
		case 3:
			GL20C.glUniform3iv(location, values);
			break;
		case 4:
			GL20C.glUniform4iv(location, values);
			break;
		default:
			GL20C.glUniform1iv(location, values);
			break;
		}
	}

	@Override
	public void uniformuiv(final int location, final int components, final @NonNull IntBuffer values) {
		switch (components) {
		case 2:
			GL30C.glUniform2uiv(location, values);
			break;
		case 3:
			GL30C.glUniform3uiv(location, values);
			break;
		case 4:
			GL30C.glUniform4uiv(location, values);
			break;
		default:
			GL30C.glUniform1uiv(location, values);
			break;
		}
	}

	@Override
	public void uniformfv(final int location, final int components, final @NonNull FloatBuffer values) {
		switch (components) {
		case 2:
			GL20C.glUniform2fv(location, values);
			break;
		case 3:
			GL20C.glUniform3fv(location, values);
			break;
		case 4:
			GL20C.glUniform4fv(location, values);
			break;
		default:
			GL20C.glUniform1fv(location, values);
			break;
		}
	}

	@Override
	public void uniformMatrixfv(final int location, final int columns, final @NonNull FloatBuffer values) {
		switch (columns) {
		case 2:
			GL20C.glUniformMatrix2fv(location, false, values);
			break;
		case 3:
			GL20C.glUniformMatrix3fv(location, false, values);
			break;
		default:
			GL20C.glUniformMatrix4fv(location, false, values);
			break;
		}
	}

}