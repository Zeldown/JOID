package dev.joid.base.opengl.render.host;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.Getter;
import lombok.NonNull;

public final class JournalGlHostGuard implements IGlHostGuard {

	private final GlStateJournal  journal;
	private final GlPipelineReset reset;

	@Getter private final IGlBinding binding;

	private int depth;

	private JournalGlHostGuard(final IGlBinding binding, final GlCapabilities capabilities, final GlFrameBufferFamily family) {
		this.journal = GlStateJournal.create(binding, capabilities, family);
		this.binding = JournalGlBinding.create(binding, this.journal);
		this.reset   = GlPipelineReset.create(this.binding, capabilities);
	}

	public static @NonNull JournalGlHostGuard create(final @NonNull IGlBinding binding, final @NonNull GlCapabilities capabilities, final @NonNull GlFrameBufferFamily family) {
		return new JournalGlHostGuard(binding, capabilities, family);
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
	public void host(final @NonNull Runnable host) {
		if (this.depth == 0) {
			host.run();
			return;
		}

		this.journal.restore();
		try {
			host.run();
		} finally {
			this.journal.start();
			this.reset.apply();
		}
	}

}