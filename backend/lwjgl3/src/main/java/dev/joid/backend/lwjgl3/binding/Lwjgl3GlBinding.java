package dev.joid.backend.lwjgl3.binding;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

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
public final class Lwjgl3GlBinding implements IGlBinding {

	private static final Lwjgl3GlBinding INSTANCE = new Lwjgl3GlBinding();

	@Getter private final IGlStateBinding   stateBinding   = Lwjgl3GlStateBinding.create();
	@Getter private final IGlBufferBinding  bufferBinding  = Lwjgl3GlBufferBinding.create();
	@Getter private final IGlProgramBinding programBinding = Lwjgl3GlProgramBinding.create();
	@Getter private final IGlTextureBinding textureBinding = Lwjgl3GlTextureBinding.create();

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
	public int getTexParameteri(final int target, final int name) {
		return GL11C.glGetTexParameteri(target, name);
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
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return family == GlFrameBufferFamily.EXT ? this.extFrameBufferBinding : this.coreFrameBufferBinding;
	}

}