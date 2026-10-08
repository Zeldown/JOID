package dev.joid.base.opengl.binding;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import lombok.NonNull;

public interface IGlProgramBinding {

	public int createProgram();
	public void useProgram(final int program);
	public void linkProgram(final int program);
	public int getProgrami(final int program, final int name);
	public @NonNull String getProgramInfoLog(final int program);

	public int createShader(final int type);
	public void deleteShader(final int shader);
	public void compileShader(final int shader);
	public int getShaderi(final int shader, final int name);
	public @NonNull String getShaderInfoLog(final int shader);
	public void shaderSource(final int shader, final @NonNull String source);

	public void attachShader(final int program, final int shader);
	public void detachShader(final int program, final int shader);

	public void bindAttribLocation(final int program, final int index, final @NonNull String name);
	public void bindFragDataLocation(final int program, final int color, final @NonNull String name);

	public void uniform1i(final int location, final int value);
	public int getUniformLocation(final int program, final @NonNull String name);
	public void uniformiv(final int location, final int components, final @NonNull IntBuffer values);
	public void uniformuiv(final int location, final int components, final @NonNull IntBuffer values);
	public void uniformfv(final int location, final int components, final @NonNull FloatBuffer values);
	public void uniformMatrixfv(final int location, final int columns, final @NonNull FloatBuffer values);

}