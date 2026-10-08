package dev.joid.impl.lwjgl3.binding;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import dev.joid.impl.opengl.binding.IGlBinding;
import dev.joid.impl.opengl.binding.IGlBufferBinding;
import dev.joid.impl.opengl.binding.IGlFrameBufferBinding;
import dev.joid.impl.opengl.binding.IGlProgramBinding;
import dev.joid.impl.opengl.binding.IGlStateBinding;
import dev.joid.impl.opengl.binding.IGlTextureBinding;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlBinding implements IGlBinding {

	private static final Lwjgl3GlBinding INSTANCE = new Lwjgl3GlBinding();

	private final IGlStateBinding       stateBinding       = Lwjgl3GlStateBinding.create();
	private final IGlBufferBinding      bufferBinding      = Lwjgl3GlBufferBinding.create();
	private final IGlProgramBinding     programBinding     = Lwjgl3GlProgramBinding.create();
	private final IGlTextureBinding     textureBinding     = Lwjgl3GlTextureBinding.create();
	private final IGlFrameBufferBinding frameBufferBinding = Lwjgl3GlFrameBufferBinding.create();

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

}