package dev.joid.base.opengl.render.host;

import java.nio.ByteBuffer;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlProgramBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.Getter;
import lombok.NonNull;

public final class JournalGlBinding implements IGlBinding {

	private final IGlBinding     binding;
	private final GlStateJournal journal;

	@Getter private final IGlStateBinding   stateBinding;
	@Getter private final IGlBufferBinding  bufferBinding;
	@Getter private final IGlProgramBinding programBinding;
	@Getter private final IGlTextureBinding textureBinding;

	private final IGlFrameBufferBinding extFrameBufferBinding;
	private final IGlFrameBufferBinding coreFrameBufferBinding;

	private JournalGlBinding(final IGlBinding binding, final GlStateJournal journal) {
		this.binding                = binding;
		this.journal                = journal;
		this.stateBinding           = JournalGlStateBinding.create(binding.getStateBinding(), journal);
		this.bufferBinding          = JournalGlBufferBinding.create(binding.getBufferBinding(), journal);
		this.programBinding         = JournalGlProgramBinding.create(binding.getProgramBinding(), journal);
		this.textureBinding         = JournalGlTextureBinding.create(binding.getTextureBinding(), journal);
		this.extFrameBufferBinding  = JournalGlFrameBufferBinding.create(binding.getFrameBufferBinding(GlFrameBufferFamily.EXT), journal);
		this.coreFrameBufferBinding = JournalGlFrameBufferBinding.create(binding.getFrameBufferBinding(GlFrameBufferFamily.CORE), journal);
	}

	public static @NonNull JournalGlBinding create(final @NonNull IGlBinding binding, final @NonNull GlStateJournal journal) {
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
	public int getTexParameteri(final int target, final int name) {
		return this.binding.getTexParameteri(target, name);
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
		this.journal.touchReadBuffer();
		this.binding.readBuffer(buffer);
	}

	@Override
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels) {
		this.binding.readPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return family == GlFrameBufferFamily.EXT ? this.extFrameBufferBinding : this.coreFrameBufferBinding;
	}

}