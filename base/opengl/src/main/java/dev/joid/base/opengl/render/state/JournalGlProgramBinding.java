package dev.joid.base.opengl.render.state;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import dev.joid.base.opengl.binding.IGlProgramBinding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JournalGlProgramBinding implements IGlProgramBinding {

	private final IGlProgramBinding binding;
	private final GlStateJournal    journal;

	public static @NonNull JournalGlProgramBinding create(final @NonNull IGlProgramBinding binding, final @NonNull GlStateJournal journal) {
		return new JournalGlProgramBinding(binding, journal);
	}

	@Override
	public int createProgram() {
		return this.binding.createProgram();
	}

	@Override
	public void useProgram(final int program) {
		this.journal.touch(GlStateKey.PROGRAM);
		this.binding.useProgram(program);
	}

	@Override
	public void linkProgram(final int program) {
		this.binding.linkProgram(program);
	}

	@Override
	public int getProgrami(final int program, final int name) {
		return this.binding.getProgrami(program, name);
	}

	@Override
	public @NonNull String getProgramInfoLog(final int program) {
		return this.binding.getProgramInfoLog(program);
	}

	@Override
	public int createShader(final int type) {
		return this.binding.createShader(type);
	}

	@Override
	public void deleteShader(final int shader) {
		this.binding.deleteShader(shader);
	}

	@Override
	public void compileShader(final int shader) {
		this.binding.compileShader(shader);
	}

	@Override
	public int getShaderi(final int shader, final int name) {
		return this.binding.getShaderi(shader, name);
	}

	@Override
	public @NonNull String getShaderInfoLog(final int shader) {
		return this.binding.getShaderInfoLog(shader);
	}

	@Override
	public void shaderSource(final int shader, final @NonNull String source) {
		this.binding.shaderSource(shader, source);
	}

	@Override
	public void attachShader(final int program, final int shader) {
		this.binding.attachShader(program, shader);
	}

	@Override
	public void detachShader(final int program, final int shader) {
		this.binding.detachShader(program, shader);
	}

	@Override
	public void bindAttribLocation(final int program, final int index, final @NonNull String name) {
		this.binding.bindAttribLocation(program, index, name);
	}

	@Override
	public void bindFragDataLocation(final int program, final int color, final @NonNull String name) {
		this.binding.bindFragDataLocation(program, color, name);
	}

	@Override
	public void uniform1i(final int location, final int value) {
		this.binding.uniform1i(location, value);
	}

	@Override
	public int getUniformLocation(final int program, final @NonNull String name) {
		return this.binding.getUniformLocation(program, name);
	}

	@Override
	public void uniformiv(final int location, final int components, final @NonNull IntBuffer values) {
		this.binding.uniformiv(location, components, values);
	}

	@Override
	public void uniformuiv(final int location, final int components, final @NonNull IntBuffer values) {
		this.binding.uniformuiv(location, components, values);
	}

	@Override
	public void uniformfv(final int location, final int components, final @NonNull FloatBuffer values) {
		this.binding.uniformfv(location, components, values);
	}

	@Override
	public void uniformMatrixfv(final int location, final int columns, final @NonNull FloatBuffer values) {
		this.binding.uniformMatrixfv(location, columns, values);
	}

}