package dev.joid.base.opengl.render.state;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.NonNull;

public final class JournalGlBinding implements IGlBinding {

	private final IGlBinding     binding;
	private final GlStateJournal journal;

	private final IGlFrameBufferBinding extFrameBufferBinding;
	private final IGlFrameBufferBinding coreFrameBufferBinding;

	private JournalGlBinding(final IGlBinding binding, final GlStateJournal journal) {
		this.binding                = binding;
		this.journal                = journal;
		this.extFrameBufferBinding  = JournalGlFrameBufferBinding.create(binding.getFrameBufferBinding(GlFrameBufferFamily.EXT), journal);
		this.coreFrameBufferBinding = JournalGlFrameBufferBinding.create(binding.getFrameBufferBinding(GlFrameBufferFamily.CORE), journal);
	}

	public static @NonNull JournalGlBinding create(final IGlBinding binding, final GlStateJournal journal) {
		return new JournalGlBinding(binding, journal);
	}

	@Override
	public void enable(final int capability) {
		this.journal.touch(GlStateKey.CAPABILITY, capability);
		this.binding.enable(capability);
	}

	@Override
	public void disable(final int capability) {
		this.journal.touch(GlStateKey.CAPABILITY, capability);
		this.binding.disable(capability);
	}

	@Override
	public boolean isEnabled(final int capability) {
		return this.binding.isEnabled(capability);
	}

	@Override
	public int getInteger(final int name) {
		return this.binding.getInteger(name);
	}

	@Override
	public String getString(final int name) {
		return this.binding.getString(name);
	}

	@Override
	public String getString(final int name, final int index) {
		return this.binding.getString(name, index);
	}

	@Override
	public void getFloats(final int name, final @NonNull float[] values) {
		this.binding.getFloats(name, values);
	}

	@Override
	public void getIntegers(final int name, final @NonNull int[] values) {
		this.binding.getIntegers(name, values);
	}

	@Override
	public int getVertexAttribi(final int index, final int name) {
		return this.binding.getVertexAttribi(index, name);
	}

	@Override
	public long getVertexAttribPointer(final int index, final int name) {
		return this.binding.getVertexAttribPointer(index, name);
	}

	@Override
	public void getMaterialFloats(final int face, final int name, final @NonNull float[] values) {
		this.binding.getMaterialFloats(face, name, values);
	}

	@Override
	public void getVertexAttribFloats(final int index, final int name, final @NonNull float[] values) {
		this.binding.getVertexAttribFloats(index, name, values);
	}

	@Override
	public void clear(final int mask) {
		this.binding.clear(mask);
	}

	@Override
	public void readBuffer(final int buffer) {
		this.journal.saveReadBuffer();
		this.binding.readBuffer(buffer);
	}

	@Override
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels) {
		this.binding.readPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void clearStencil(final int stencil) {
		this.journal.touch(GlStateKey.CLEAR_STENCIL);
		this.binding.clearStencil(stencil);
	}

	@Override
	public void clearDepth(final double depth) {
		this.journal.touch(GlStateKey.CLEAR_DEPTH);
		this.binding.clearDepth(depth);
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		this.journal.touch(GlStateKey.VIEWPORT);
		this.binding.viewport(x, y, width, height);
	}

	@Override
	public void clearColor(final float red, final float green, final float blue, final float alpha) {
		this.journal.touch(GlStateKey.CLEAR_COLOR);
		this.binding.clearColor(red, green, blue, alpha);
	}

	@Override
	public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
		this.journal.touch(GlStateKey.COLOR_MASK);
		this.binding.colorMask(red, green, blue, alpha);
	}

	@Override
	public void blendEquationSeparate(final int color, final int alpha) {
		this.journal.touch(GlStateKey.BLEND_EQUATION);
		this.binding.blendEquationSeparate(color, alpha);
	}

	@Override
	public void blendFuncSeparate(final int sourceColor, final int destinationColor, final int sourceAlpha, final int destinationAlpha) {
		this.journal.touch(GlStateKey.BLEND_FUNCTION);
		this.binding.blendFuncSeparate(sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	@Override
	public void depthFunc(final int function) {
		this.journal.touch(GlStateKey.DEPTH_FUNCTION);
		this.binding.depthFunc(function);
	}

	@Override
	public void depthMask(final boolean write) {
		this.journal.touch(GlStateKey.DEPTH_MASK);
		this.binding.depthMask(write);
	}

	@Override
	public void stencilMask(final int mask) {
		this.journal.touch(GlStateKey.STENCIL_MASK);
		this.binding.stencilMask(mask);
	}

	@Override
	public void stencilMaskSeparate(final int face, final int mask) {
		this.journal.touch(GlStateKey.STENCIL_MASK);
		this.binding.stencilMaskSeparate(face, mask);
	}

	@Override
	public void stencilOp(final int fail, final int depthFail, final int pass) {
		this.journal.touch(GlStateKey.STENCIL_OPERATION);
		this.binding.stencilOp(fail, depthFail, pass);
	}

	@Override
	public void stencilFunc(final int function, final int reference, final int mask) {
		this.journal.touch(GlStateKey.STENCIL_FUNCTION);
		this.binding.stencilFunc(function, reference, mask);
	}

	@Override
	public void stencilOpSeparate(final int face, final int fail, final int depthFail, final int pass) {
		this.journal.touch(GlStateKey.STENCIL_OPERATION);
		this.binding.stencilOpSeparate(face, fail, depthFail, pass);
	}

	@Override
	public void stencilFuncSeparate(final int face, final int function, final int reference, final int mask) {
		this.journal.touch(GlStateKey.STENCIL_FUNCTION);
		this.binding.stencilFuncSeparate(face, function, reference, mask);
	}

	@Override
	public void cullFace(final int mode) {
		this.journal.touch(GlStateKey.CULL_FACE);
		this.binding.cullFace(mode);
	}

	@Override
	public void frontFace(final int mode) {
		this.journal.touch(GlStateKey.FRONT_FACE);
		this.binding.frontFace(mode);
	}

	@Override
	public void lineWidth(final float width) {
		this.journal.touch(GlStateKey.LINE_WIDTH);
		this.binding.lineWidth(width);
	}

	@Override
	public void polygonMode(final int face, final int mode) {
		this.journal.touch(GlStateKey.POLYGON_MODE);
		this.binding.polygonMode(face, mode);
	}

	@Override
	public void pixelStorei(final int name, final int value) {
		this.journal.touch(GlStateKey.PIXEL_STORE, name);
		this.binding.pixelStorei(name, value);
	}

	@Override
	public int genBuffer() {
		return this.binding.genBuffer();
	}

	@Override
	public void bindBuffer(final int target, final int buffer) {
		this.journal.touch(GlStateKey.BUFFER, target);
		this.binding.bindBuffer(target, buffer);
	}

	@Override
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage) {
		this.binding.bufferData(target, data, usage);
	}

	@Override
	public int genVertexArray() {
		final int array = this.binding.genVertexArray();
		this.journal.excludeVertexArray(array);
		return array;
	}

	@Override
	public void bindVertexArray(final int array) {
		this.journal.touch(GlStateKey.VERTEX_ARRAY);
		this.journal.trackVertexArray(array);
		this.binding.bindVertexArray(array);
	}

	@Override
	public void enableVertexAttribArray(final int index) {
		this.journal.saveVertexAttribute(index);
		this.binding.enableVertexAttribArray(index);
	}

	@Override
	public void disableVertexAttribArray(final int index) {
		this.journal.saveVertexAttribute(index);
		this.binding.disableVertexAttribArray(index);
	}

	@Override
	public void vertexAttrib2f(final int index, final float x, final float y) {
		this.journal.touch(GlStateKey.CURRENT_VERTEX_ATTRIBUTE, index);
		this.binding.vertexAttrib2f(index, x, y);
	}

	@Override
	public void vertexAttrib3f(final int index, final float x, final float y, final float z) {
		this.journal.touch(GlStateKey.CURRENT_VERTEX_ATTRIBUTE, index);
		this.binding.vertexAttrib3f(index, x, y, z);
	}

	@Override
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w) {
		this.journal.touch(GlStateKey.CURRENT_VERTEX_ATTRIBUTE, index);
		this.binding.vertexAttrib4f(index, x, y, z, w);
	}

	@Override
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset) {
		this.journal.saveVertexAttribute(index);
		this.binding.vertexAttribPointer(index, size, type, normalized, stride, offset);
	}

	@Override
	public void drawArrays(final int mode, final int first, final int count) {
		this.binding.drawArrays(mode, first, count);
	}

	@Override
	public int createProgram() {
		return this.binding.createProgram();
	}

	@Override
	public void deleteProgram(final int program) {
		this.binding.deleteProgram(program);
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

	@Override
	public int genTexture() {
		final int texture = this.binding.genTexture();
		this.journal.excludeTexture(texture);
		return texture;
	}

	@Override
	public void activeTexture(final int unit) {
		this.journal.touch(GlStateKey.ACTIVE_TEXTURE);
		this.journal.trackActiveTexture(unit);
		this.binding.activeTexture(unit);
	}

	@Override
	public void deleteTexture(final int texture) {
		this.journal.includeTexture(texture);
		this.binding.deleteTexture(texture);
	}

	@Override
	public void bindSampler(final int unit, final int sampler) {
		this.journal.saveSampler(unit);
		this.binding.bindSampler(unit, sampler);
	}

	@Override
	public void bindTexture(final int target, final int texture) {
		this.journal.saveTexture();
		this.journal.trackTexture(texture);
		this.binding.bindTexture(target, texture);
	}

	@Override
	public void texParameteri(final int target, final int name, final int value) {
		this.journal.saveTextureParameter(name);
		this.binding.texParameteri(target, name, value);
	}

	@Override
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type) {
		this.binding.texImage2D(target, level, internalFormat, width, height, format, type);
	}

	@Override
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels) {
		this.binding.texSubImage2D(target, level, x, y, width, height, format, type, pixels);
	}

	@Override
	public boolean isTexture(final int texture) {
		return this.binding.isTexture(texture);
	}

	@Override
	public int getTexParameteri(final int target, final int name) {
		return this.binding.getTexParameteri(target, name);
	}

	@Override
	public int getTexLevelParameteri(final int target, final int level, final int name) {
		return this.binding.getTexLevelParameteri(target, level, name);
	}

	@Override
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return family == GlFrameBufferFamily.EXT ? this.extFrameBufferBinding : this.coreFrameBufferBinding;
	}

	@Override
	public boolean canPointClientMemory() {
		return this.binding.canPointClientMemory();
	}

}