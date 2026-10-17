package dev.joid.backend.lwjgl2.binding;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLContext;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlBinding implements IGlBinding {

	private static final Lwjgl2GlBinding INSTANCE = new Lwjgl2GlBinding();

	private final IGlFrameBufferBinding extFrameBufferBinding  = Lwjgl2ExtGlFrameBufferBinding.create();
	private final IGlFrameBufferBinding coreFrameBufferBinding = Lwjgl2CoreGlFrameBufferBinding.create();

	private final IntBuffer   intBuffer     = BufferUtils.createIntBuffer(16);
	private final FloatBuffer floatBuffer   = BufferUtils.createFloatBuffer(16);
	private final IntBuffer   uploadBuffer  = BufferUtils.createIntBuffer(1 << 20);
	private final ByteBuffer  pointerBuffer = BufferUtils.createByteBuffer(PointerBuffer.getPointerSize());

	public static @NonNull Lwjgl2GlBinding inst() {
		return Lwjgl2GlBinding.INSTANCE;
	}

	@Override
	public void enable(final int capability) {
		GL11.glEnable(capability);
	}

	@Override
	public void disable(final int capability) {
		GL11.glDisable(capability);
	}

	@Override
	public boolean isEnabled(final int capability) {
		return GL11.glIsEnabled(capability);
	}

	@Override
	public int getInteger(final int name) {
		return GL11.glGetInteger(name);
	}

	@Override
	public String getString(final int name) {
		try {
			GLContext.getCapabilities();
		} catch (final RuntimeException exception) {
			return null;
		}

		return GL11.glGetString(name);
	}

	@Override
	public String getString(final int name, final int index) {
		return GL30.glGetStringi(name, index);
	}

	@Override
	public void getFloats(final int name, final @NonNull float[] values) {
		this.floatBuffer.clear();
		GL11.glGetFloat(name, this.floatBuffer);
		this.floatBuffer.get(values);
	}

	@Override
	public void getIntegers(final int name, final @NonNull int[] values) {
		this.intBuffer.clear();
		GL11.glGetInteger(name, this.intBuffer);
		this.intBuffer.get(values);
	}

	@Override
	public int getVertexAttribi(final int index, final int name) {
		this.intBuffer.clear();
		GL20.glGetVertexAttrib(index, name, this.intBuffer);
		return this.intBuffer.get(0);
	}

	@Override
	public long getVertexAttribPointer(final int index, final int name) {
		this.pointerBuffer.clear();
		GL20.glGetVertexAttribPointer(index, name, this.pointerBuffer);
		return PointerBuffer.is64Bit() ? this.pointerBuffer.getLong(0) : this.pointerBuffer.getInt(0) & 0xFFFFFFFFL;
	}

	@Override
	public void getVertexAttribFloats(final int index, final int name, final @NonNull float[] values) {
		this.floatBuffer.clear();
		GL20.glGetVertexAttrib(index, name, this.floatBuffer);
		this.floatBuffer.get(values, 0, Math.min(values.length, 4));
	}

	@Override
	public void clear(final int mask) {
		GL11.glClear(mask);
	}

	@Override
	public void readBuffer(final int buffer) {
		GL11.glReadBuffer(buffer);
	}

	@Override
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels) {
		GL11.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void clearStencil(final int stencil) {
		GL11.glClearStencil(stencil);
	}

	@Override
	public void clearDepth(final double depth) {
		GL11.glClearDepth(depth);
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		GL11.glViewport(x, y, width, height);
	}

	@Override
	public void clearColor(final float red, final float green, final float blue, final float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
		GL11.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void blendEquationSeparate(final int color, final int alpha) {
		GL20.glBlendEquationSeparate(color, alpha);
	}

	@Override
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		GL14.glBlendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	@Override
	public void depthFunc(final int function) {
		GL11.glDepthFunc(function);
	}

	@Override
	public void depthMask(final boolean write) {
		GL11.glDepthMask(write);
	}

	@Override
	public void stencilMask(final int mask) {
		GL11.glStencilMask(mask);
	}

	@Override
	public void stencilMaskSeparate(final int face, final int mask) {
		GL20.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void stencilOp(final int fail, final int depthFail, final int pass) {
		GL11.glStencilOp(fail, depthFail, pass);
	}

	@Override
	public void stencilFunc(final int function, final int reference, final int mask) {
		GL11.glStencilFunc(function, reference, mask);
	}

	@Override
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass) {
		GL20.glStencilOpSeparate(face, fail, depthFail, pass);
	}

	@Override
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask) {
		GL20.glStencilFuncSeparate(face, function, reference, mask);
	}

	@Override
	public void cullFace(final int mode) {
		GL11.glCullFace(mode);
	}

	@Override
	public void frontFace(final int mode) {
		GL11.glFrontFace(mode);
	}

	@Override
	public void lineWidth(final float width) {
		GL11.glLineWidth(width);
	}

	@Override
	public void polygonMode(final int face, final int mode) {
		GL11.glPolygonMode(face, mode);
	}

	@Override
	public void pixelStorei(final int name, final int value) {
		GL11.glPixelStorei(name, value);
	}

	@Override
	public int genBuffer() {
		return GL15.glGenBuffers();
	}

	@Override
	public void bindBuffer(final int target, final int buffer) {
		GL15.glBindBuffer(target, buffer);
	}

	@Override
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage) {
		GL15.glBufferData(target, data, usage);
	}

	@Override
	public int genVertexArray() {
		return GL30.glGenVertexArrays();
	}

	@Override
	public void bindVertexArray(final int array) {
		GL30.glBindVertexArray(array);
	}

	@Override
	public void enableVertexAttribArray(final int index) {
		GL20.glEnableVertexAttribArray(index);
	}

	@Override
	public void disableVertexAttribArray(final int index) {
		GL20.glDisableVertexAttribArray(index);
	}

	@Override
	public void vertexAttrib2f(final int index, final float x, final float y) {
		GL20.glVertexAttrib2f(index, x, y);
	}

	@Override
	public void vertexAttrib3f(final int index, final float x, final float y, final float z) {
		GL20.glVertexAttrib3f(index, x, y, z);
	}

	@Override
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w) {
		GL20.glVertexAttrib4f(index, x, y, z, w);
	}

	@Override
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset) {
		GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
	}

	@Override
	public void drawArrays(final int mode, final int first, final int count) {
		GL11.glDrawArrays(mode, first, count);
	}

	@Override
	public int createProgram() {
		return GL20.glCreateProgram();
	}

	@Override
	public void deleteProgram(final int program) {
		GL20.glDeleteProgram(program);
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

	@Override
	public int genTexture() {
		return GL11.glGenTextures();
	}

	@Override
	public void activeTexture(final int unit) {
		GL13.glActiveTexture(unit);
	}

	@Override
	public void deleteTexture(final int texture) {
		GL11.glDeleteTextures(texture);
	}

	@Override
	public void bindSampler(final int unit, final int sampler) {
		GL33.glBindSampler(unit, sampler);
	}

	@Override
	public void bindTexture(final int target, final int texture) {
		GL11.glBindTexture(target, texture);
	}

	@Override
	public void texParameteri(final int target, final int name, final int value) {
		GL11.glTexParameteri(target, name, value);
	}

	@Override
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type) {
		GL11.glTexImage2D(target, level, internalFormat, width, height, 0, format, type, (IntBuffer) null);
	}

	@Override
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels) {
		final int rows = this.uploadBuffer.capacity() / width;
		for (int row = 0; row < height; row += rows) {
			final int count = Math.min(rows, height - row);
			this.uploadBuffer.clear();
			this.uploadBuffer.put(pixels, row * width, count * width).flip();
			GL11.glTexSubImage2D(target, level, x, y + row, width, count, format, type, this.uploadBuffer);
		}
	}

	@Override
	public boolean isTexture(final int texture) {
		return GL11.glIsTexture(texture);
	}

	@Override
	public int getTexParameteri(final int target, final int name) {
		return GL11.glGetTexParameteri(target, name);
	}

	@Override
	public int getTexLevelParameteri(final int target, final int level, final int name) {
		return GL11.glGetTexLevelParameteri(target, level, name);
	}

	@Override
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return family == GlFrameBufferFamily.EXT ? this.extFrameBufferBinding : this.coreFrameBufferBinding;
	}

	@Override
	public boolean canPointClientMemory() {
		return false;
	}

}