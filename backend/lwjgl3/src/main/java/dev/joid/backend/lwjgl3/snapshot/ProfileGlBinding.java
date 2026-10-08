package dev.joid.backend.lwjgl3.snapshot;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlProgramBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProfileGlBinding implements IGlBinding {

	private final IGlBinding      binding;
	private final SnapshotProfile profile;

	public static @NonNull ProfileGlBinding create(final @NonNull IGlBinding binding, final @NonNull SnapshotProfile profile) {
		return new ProfileGlBinding(binding, profile);
	}

	@Override
	public void enable(final int capability) {
		this.binding.enable(capability);
	}

	@Override
	public void disable(final int capability) {
		this.binding.disable(capability);
	}

	@Override
	public boolean isEnabled(final int capability) {
		return this.binding.isEnabled(capability);
	}

	@Override
	public int getInteger(final int name) {
		return name == GlConstants.NUM_EXTENSIONS ? this.getExtensions().size() : this.binding.getInteger(name);
	}

	@Override
	public String getString(final int name) {
		if (name == GlConstants.SHADING_LANGUAGE_VERSION && this.profile.getShadingLanguageVersion() != null) {
			return this.profile.getShadingLanguageVersion();
		}
		return name == GlConstants.EXTENSIONS ? String.join(" ", this.getExtensions()) : this.binding.getString(name);
	}

	@Override
	public String getString(final int name, final int index) {
		return name == GlConstants.EXTENSIONS ? this.getExtensions().get(index) : this.binding.getString(name, index);
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
	public void clear(final int mask) {
		this.binding.clear(mask);
	}

	@Override
	public void readBuffer(final int buffer) {
		this.binding.readBuffer(buffer);
	}

	@Override
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels) {
		this.binding.readPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public @NonNull IGlStateBinding getStateBinding() {
		return this.binding.getStateBinding();
	}

	@Override
	public @NonNull IGlBufferBinding getBufferBinding() {
		return this.binding.getBufferBinding();
	}

	@Override
	public @NonNull IGlProgramBinding getProgramBinding() {
		return this.binding.getProgramBinding();
	}

	@Override
	public @NonNull IGlTextureBinding getTextureBinding() {
		return this.binding.getTextureBinding();
	}

	@Override
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
		return this.binding.getFrameBufferBinding(family);
	}

	private List<String> getExtensions() {
		final List<String> extensionList = new ArrayList<>();
		if (this.binding.getString(GlConstants.VERSION).matches("^[012]\\..*")) {
			extensionList.addAll(Arrays.asList(this.binding.getString(GlConstants.EXTENSIONS).trim().split("\\s+")));
		} else {
			for (int i = 0; i < this.binding.getInteger(GlConstants.NUM_EXTENSIONS); i++) {
				extensionList.add(this.binding.getString(GlConstants.EXTENSIONS, i));
			}
		}
		extensionList.removeAll(this.profile.getHiddenExtensionList());
		return extensionList;
	}

}