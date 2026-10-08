package dev.joid.impl.lwjgl3.binding;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;

import dev.joid.impl.opengl.binding.IGlProgramBinding;
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
	public void uniform1i(final int location, final int value) {
		GL20C.glUniform1i(location, value);
	}

	@Override
	public int getUniformLocation(final int program, final @NonNull String name) {
		return GL20C.glGetUniformLocation(program, name);
	}

	@Override
	public int getUniformBlockIndex(final int program, final @NonNull String name) {
		return GL31C.glGetUniformBlockIndex(program, name);
	}

	@Override
	public void uniformBlockBinding(final int program, final int index, final int binding) {
		GL31C.glUniformBlockBinding(program, index, binding);
	}

}