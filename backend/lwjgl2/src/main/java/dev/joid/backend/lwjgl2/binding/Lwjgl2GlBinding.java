package dev.joid.backend.lwjgl2.binding;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlProgramBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlBinding implements IGlBinding {

	private static final Lwjgl2GlBinding INSTANCE = new Lwjgl2GlBinding();

	@Getter private final IGlStateBinding   stateBinding   = Lwjgl2GlStateBinding.create();
	@Getter private final IGlBufferBinding  bufferBinding  = Lwjgl2GlBufferBinding.create();
	@Getter private final IGlProgramBinding programBinding = Lwjgl2GlProgramBinding.create();
	@Getter private final IGlTextureBinding textureBinding = Lwjgl2GlTextureBinding.create();

	private final IGlFrameBufferBinding extFrameBufferBinding  = Lwjgl2GlExtFrameBufferBinding.create();
	private final IGlFrameBufferBinding coreFrameBufferBinding = Lwjgl2GlCoreFrameBufferBinding.create();

	private final IntBuffer   intBuffer     = BufferUtils.createIntBuffer(16);
	private final FloatBuffer floatBuffer   = BufferUtils.createFloatBuffer(16);
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
	public int getTexParameteri(final int target, final int name) {
		return GL11.glGetTexParameteri(target, name);
	}

	@Override
	public long getVertexAttribPointer(final int index, final int name) {
		this.pointerBuffer.clear();
		GL20.glGetVertexAttribPointer(index, name, this.pointerBuffer);
		return PointerBuffer.is64Bit() ? this.pointerBuffer.getLong(0) : this.pointerBuffer.getInt(0) & 0xFFFFFFFFL;
	}

	@Override
	public void getMaterialFloats(final int face, final int name, final @NonNull float[] values) {
		this.floatBuffer.clear();
		GL11.glGetMaterial(face, name, this.floatBuffer);
		this.floatBuffer.get(values, 0, Math.min(values.length, 4));
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
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return family == GlFrameBufferFamily.EXT ? this.extFrameBufferBinding : this.coreFrameBufferBinding;
	}

}