package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.Getter;
import lombok.NonNull;

public final class JournalGlStateGuard implements IGlStateGuard {

	private final GlPipelineReset reset;
	private final GlStateJournal  journal;

	@Getter
	private final IGlBinding binding;

	private int depth;

	private JournalGlStateGuard(final IGlBinding binding, final GlCapabilities capabilities, final GlFrameBufferFamily family) {
		this.journal = GlStateJournal.create(binding, capabilities, family);
		this.binding = JournalGlBinding.create(binding, this.journal);
		this.reset   = GlPipelineReset.create(this.binding, capabilities);
	}

	public static @NonNull JournalGlStateGuard create(final @NonNull IGlBinding binding, final @NonNull GlCapabilities capabilities, final @NonNull GlFrameBufferFamily family) {
		return new JournalGlStateGuard(binding, capabilities, family);
	}

	@Override
	public void exit() {
		if (this.depth > 0 && --this.depth == 0) {
			this.journal.restore();
		}
	}

	@Override
	public void enter() {
		if (this.depth++ == 0) {
			this.journal.start();
			this.reset.apply();
		}
	}

	@Override
	public void suspend(final @NonNull Runnable draw) {
		if (this.depth == 0) {
			draw.run();
			return;
		}

		this.journal.restore();
		try {
			draw.run();
		} finally {
			this.journal.start();
			this.reset.apply();
		}
	}

}