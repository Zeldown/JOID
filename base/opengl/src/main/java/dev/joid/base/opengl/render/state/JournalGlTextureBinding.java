package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.IGlTextureBinding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JournalGlTextureBinding implements IGlTextureBinding {

	private final IGlTextureBinding binding;
	private final GlStateJournal    journal;

	public static @NonNull JournalGlTextureBinding create(final @NonNull IGlTextureBinding binding, final @NonNull GlStateJournal journal) {
		return new JournalGlTextureBinding(binding, journal);
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

}