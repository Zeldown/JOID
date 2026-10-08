package dev.joid.impl.opengl.binding;

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

	public void uniform1i(final int location, final int value);
	public int getUniformLocation(final int program, final @NonNull String name);

	public int getUniformBlockIndex(final int program, final @NonNull String name);
	public void uniformBlockBinding(final int program, final int index, final int binding);

}