package dev.joid.base.opengl.snapshot;

import java.util.Map;
import java.util.function.Supplier;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.test.contract.IStateGuardBackend;
import dev.joid.test.contract.StateTrap;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public abstract class GlStateGuardBackend implements IStateGuardBackend {

	private final ISnapshotBackend     backend;
	private final Supplier<IGlBinding> bindings;

	protected GlStateGuardBackend(final @NonNull ISnapshotBackend backend, final @NonNull Supplier<IGlBinding> bindings) {
		this.backend  = backend;
		this.bindings = bindings;
	}

	@Override
	public final void create(final int width, final int height) {
		this.backend.create(width, height);
	}

	@Override
	public final void destroy() {
		this.backend.destroy();
	}

	@Override
	public final void present() {
		this.backend.present();
	}

	@Override
	public final @NonNull SnapshotImage capture(final int width, final int height) {
		return this.backend.capture(width, height);
	}

	@Override
	public final @NonNull String getRenderer() {
		return this.backend.getRenderer();
	}

	@Override
	public final void inject(final @NonNull StateTrap trap) {
		if (trap != StateTrap.EVERYTHING) {
			this.injectTrap(trap);
			return;
		}

		for (final StateTrap other : StateTrap.values()) {
			if (other != StateTrap.EVERYTHING && this.supports(other)) {
				this.injectTrap(other);
			}
		}
	}

	@Override
	public final boolean supports(final @NonNull StateTrap trap) {
		switch (trap) {
		case ALPHA_TEST:
		case CLIENT_ARRAYS:
		case COLOR_MATERIAL:
			return this.isCompatibility();
		case PIXEL_BUFFER:
			return this.getCapabilities().hasPixelBuffers();
		case VERTEX_ARRAY:
			return this.getCapabilities().hasVertexArrays();
		case SAMPLER_OBJECTS:
			return this.getCapabilities().hasSamplerObjects();
		case FRAMEBUFFER:
			return this.getCapabilities().hasFrameBufferBlit();
		default:
			return true;
		}
	}

	@Override
	public final @NonNull Map<@NonNull String, @NonNull String> readState() {
		return GlStateSnapshot.read(this.bindings.get(), this.getCapabilities(), this::readMaterial);
	}

	protected final boolean isCompatibility() {
		return this.getCapabilities().getProfile() == GlProfile.COMPATIBILITY;
	}

	protected final @NonNull GlCapabilities getCapabilities() {
		return BridgeHandler.RENDER.getBridge(GlRenderBridge.class).getCapabilities();
	}

	protected abstract void injectTrap(final @NonNull StateTrap trap);

	protected abstract void readMaterial(final int face, final int name, final @NonNull float[] values);

}