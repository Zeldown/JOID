package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.capability.GlProfile;
import dev.joid.base.opengl.render.GlRenderBridge;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FixedMatrixImport {

	private final float[]        matrix;
	private final GlRenderBridge bridge;

	public static @NonNull FixedMatrixImport create(final @NonNull GlRenderBridge bridge) {
		if (bridge.getCapabilities().getProfile() != GlProfile.COMPATIBILITY) {
			throw new IllegalStateException("Only a compatibility context has fixed-function matrices to import, this context is " + bridge.getCapabilities().getProfile() + " with " + bridge.getCapabilities().getName());
		}
		return new FixedMatrixImport(new float[16], bridge);
	}

	public void apply() {
		this.bridge.getBinding().getFloats(GlConstants.PROJECTION_MATRIX, this.matrix);
		this.bridge.getProjection().load(this.matrix);
		this.bridge.getBinding().getFloats(GlConstants.MODELVIEW_MATRIX, this.matrix);
		this.bridge.getModelView().load(this.matrix);
	}

}