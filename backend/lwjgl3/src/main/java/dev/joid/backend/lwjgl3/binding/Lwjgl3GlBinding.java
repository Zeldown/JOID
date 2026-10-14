package dev.joid.backend.lwjgl3.binding;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlBinding implements IGlBinding {

	private static final Lwjgl3GlBinding INSTANCE = new Lwjgl3GlBinding();

	private final IGlFrameBufferBinding extFrameBufferBinding  = Lwjgl3GlExtFrameBufferBinding.create();
	private final IGlFrameBufferBinding coreFrameBufferBinding = Lwjgl3GlCoreFrameBufferBinding.create();

	public static @NonNull Lwjgl3GlBinding inst() {
		return Lwjgl3GlBinding.INSTANCE;
	}

	@Override
	public void enable(final int capability) {
		GL11C.glEnable(capability);
	}

	@Override
	public void disable(final int capability) {
		GL11C.glDisable(capability);
	}

	@Override
	public boolean isEnabled(final int capability) {
		return GL11C.glIsEnabled(capability);
	}

	@Override
	public int getInteger(final int name) {
		return GL11C.glGetInteger(name);
	}

	@Override
	public String getString(final int name) {
		try {
			GL.getCapabilities();
		} catch (final IllegalStateException exception) {
			return null;
		}

		return GL11C.glGetString(name);
	}

	@Override
	public String getString(final int name, final int index) {
		return GL30C.glGetStringi(name, index);
	}

	@Override
	public void getFloats(final int name, final @NonNull float[] values) {
		GL11C.glGetFloatv(name, values);
	}

	@Override
	public void getIntegers(final int name, final @NonNull int[] values) {
		GL11C.glGetIntegerv(name, values);
	}

	@Override
	public int getVertexAttribi(final int index, final int name) {
		return GL20C.glGetVertexAttribi(index, name);
	}

	@Override
	public long getVertexAttribPointer(final int index, final int name) {
		return GL20C.glGetVertexAttribPointer(index, name);
	}

	@Override
	public void getMaterialFloats(final int face, final int name, final @NonNull float[] values) {
		GL11.glGetMaterialfv(face, name, values);
	}

	@Override
	public void getVertexAttribFloats(final int index, final int name, final @NonNull float[] values) {
		GL20C.glGetVertexAttribfv(index, name, values);
	}

	@Override
	public void clear(final int mask) {
		GL11C.glClear(mask);
	}

	@Override
	public void readBuffer(final int buffer) {
		GL11C.glReadBuffer(buffer);
	}

	@Override
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels) {
		GL11C.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void clearStencil(final int stencil) {
		GL11C.glClearStencil(stencil);
	}

	@Override
	public void clearDepth(final double depth) {
		GL11C.glClearDepth(depth);
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		GL11C.glViewport(x, y, width, height);
	}

	@Override
	public void clearColor(final float red, final float green, final float blue, final float alpha) {
		GL11C.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
		GL11C.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void blendEquationSeparate(final int color, final int alpha) {
		GL20C.glBlendEquationSeparate(color, alpha);
	}

	@Override
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		GL14C.glBlendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	@Override
	public void depthFunc(final int function) {
		GL11C.glDepthFunc(function);
	}

	@Override
	public void depthMask(final boolean write) {
		GL11C.glDepthMask(write);
	}

	@Override
	public void stencilMask(final int mask) {
		GL11C.glStencilMask(mask);
	}

	@Override
	public void stencilMaskSeparate(final int face, final int mask) {
		GL20C.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void stencilOp(final int fail, final int depthFail, final int pass) {
		GL11C.glStencilOp(fail, depthFail, pass);
	}

	@Override
	public void stencilFunc(final int function, final int reference, final int mask) {
		GL11C.glStencilFunc(function, reference, mask);
	}

	@Override
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass) {
		GL20C.glStencilOpSeparate(face, fail, depthFail, pass);
	}

	@Override
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask) {
		GL20C.glStencilFuncSeparate(face, function, reference, mask);
	}

	@Override
	public void cullFace(final int mode) {
		GL11C.glCullFace(mode);
	}

	@Override
	public void frontFace(final int mode) {
		GL11C.glFrontFace(mode);
	}

	@Override
	public void lineWidth(final float width) {
		GL11C.glLineWidth(width);
	}

	@Override
	public void polygonMode(final int face, final int mode) {
		GL11C.glPolygonMode(face, mode);
	}

	@Override
	public void pixelStorei(final int name, final int value) {
		GL11C.glPixelStorei(name, value);
	}

	@Override
	public int genBuffer() {
		return GL15C.glGenBuffers();
	}

	@Override
	public void bindBuffer(final int target, final int buffer) {
		GL15C.glBindBuffer(target, buffer);
	}

	@Override
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage) {
		GL15C.glBufferData(target, data, usage);
	}

	@Override
	public int genVertexArray() {
		return GL30C.glGenVertexArrays();
	}

	@Override
	public void bindVertexArray(final int array) {
		GL30C.glBindVertexArray(array);
	}

	@Override
	public void enableVertexAttribArray(final int index) {
		GL20C.glEnableVertexAttribArray(index);
	}

	@Override
	public void disableVertexAttribArray(final int index) {
		GL20C.glDisableVertexAttribArray(index);
	}

	@Override
	public void vertexAttrib2f(final int index, final float x, final float y) {
		GL20C.glVertexAttrib2f(index, x, y);
	}

	@Override
	public void vertexAttrib3f(final int index, final float x, final float y, final float z) {
		GL20C.glVertexAttrib3f(index, x, y, z);
	}

	@Override
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w) {
		GL20C.glVertexAttrib4f(index, x, y, z, w);
	}

	@Override
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset) {
		GL20C.glVertexAttribPointer(index, size, type, normalized, stride, offset);
	}

	@Override
	public void drawArrays(final int mode, final int first, final int count) {
		GL11C.glDrawArrays(mode, first, count);
	}

	@Override
	public int createProgram() {
		return GL20C.glCreateProgram();
	}

	@Override
	public void deleteProgram(final int program) {
		GL20C.glDeleteProgram(program);
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

	@Override
	public int genTexture() {
		return GL11C.glGenTextures();
	}

	@Override
	public void activeTexture(final int unit) {
		GL13C.glActiveTexture(unit);
	}

	@Override
	public void deleteTexture(final int texture) {
		GL11C.glDeleteTextures(texture);
	}

	@Override
	public void bindSampler(final int unit, final int sampler) {
		GL33C.glBindSampler(unit, sampler);
	}

	@Override
	public void bindTexture(final int target, final int texture) {
		GL11C.glBindTexture(target, texture);
	}

	@Override
	public void texParameteri(final int target, final int name, final int value) {
		GL11C.glTexParameteri(target, name, value);
	}

	@Override
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type) {
		GL11C.glTexImage2D(target, level, internalFormat, width, height, 0, format, type, (IntBuffer) null);
	}

	@Override
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels) {
		GL11C.glTexSubImage2D(target, level, x, y, width, height, format, type, pixels);
	}

	@Override
	public boolean isTexture(final int texture) {
		return GL11C.glIsTexture(texture);
	}

	@Override
	public int getTexParameteri(final int target, final int name) {
		return GL11C.glGetTexParameteri(target, name);
	}

	@Override
	public int getTexLevelParameteri(final int target, final int level, final int name) {
		return GL11C.glGetTexLevelParameteri(target, level, name);
	}

	@Override
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return family == GlFrameBufferFamily.EXT ? this.extFrameBufferBinding : this.coreFrameBufferBinding;
	}

}