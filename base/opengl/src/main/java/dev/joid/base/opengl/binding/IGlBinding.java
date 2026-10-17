package dev.joid.base.opengl.binding;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.NonNull;

public interface IGlBinding {

	public void enable(final int capability);
	public void disable(final int capability);
	public boolean isEnabled(final int capability);

	public int getInteger(final int name);
	public String getString(final int name);
	public String getString(final int name, final int index);
	public void getFloats(final int name, final @NonNull float[] values);
	public void getIntegers(final int name, final @NonNull int[] values);

	public int getVertexAttribi(final int index, final int name);
	public long getVertexAttribPointer(final int index, final int name);
	public void getVertexAttribFloats(final int index, final int name, final @NonNull float[] values);

	public void clear(final int mask);
	public void readBuffer(final int buffer);
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels);

	public void clearDepth(final double depth);
	public void clearStencil(final int stencil);
	public void viewport(final int x, final int y, final int width, final int height);
	public void clearColor(final float red, final float green, final float blue, final float alpha);
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha);

	public void blendEquationSeparate(final int color, final int alpha);
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha);

	public void depthFunc(final int function);
	public void depthMask(final boolean write);

	public void stencilMask(final int mask);
	public void stencilMaskSeparate(final int face, final int mask);
	public void stencilOp(final int fail, final int depthFail, final int pass);
	public void stencilFunc(final int function, final int reference, final int mask);
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass);
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask);

	public void cullFace(final int mode);
	public void frontFace(final int mode);
	public void lineWidth(final float width);
	public void polygonMode(final int face, final int mode);

	public void pixelStorei(final int name, final int value);

	public int genBuffer();
	public void bindBuffer(final int target, final int buffer);
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage);

	public int genVertexArray();
	public void bindVertexArray(final int array);

	public void enableVertexAttribArray(final int index);
	public void disableVertexAttribArray(final int index);
	public void vertexAttrib2f(final int index, final float x, final float y);
	public void vertexAttrib3f(final int index, final float x, final float y, final float z);
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w);
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset);

	public void drawArrays(final int mode, final int first, final int count);

	public int createProgram();
	public void deleteProgram(final int program);
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

	public int genTexture();
	public void activeTexture(final int unit);
	public void deleteTexture(final int texture);
	public void bindSampler(final int unit, final int sampler);
	public void bindTexture(final int target, final int texture);
	public void texParameteri(final int target, final int name, final int value);
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type);
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels);

	public boolean isTexture(final int texture);
	public int getTexParameteri(final int target, final int name);
	public int getTexLevelParameteri(final int target, final int level, final int name);

	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family);

	public default boolean canPointClientMemory() {
		return true;
	}

}