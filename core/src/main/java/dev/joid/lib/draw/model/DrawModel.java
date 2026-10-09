package dev.joid.lib.draw.model;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import lombok.Getter;
import lombok.NonNull;

public final class DrawModel {

	@Getter
	private static DrawModel instance;

	public DrawModel() {
		if (DrawModel.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawModel.");
		}
		DrawModel.instance = this;
	}

	public void drawModel(final double x, final double y, final double size, final @NonNull IDrawableModel model) {
		this.drawModel(x, y, size, size, size, model);
	}

	public void drawModel(final double x, final double y, final double sizeX, final double sizeY, final double sizeZ, final @NonNull IDrawableModel model) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushMatrix();
		render.pushState();
		try {
			render.translate(x, y, 0D);
			render.scale(sizeX, -sizeY, sizeZ);

			render.cull(false);
			render.lighting(true);
			render.depth(true, true);
			render.clearDepth();

			model.render();
			render.clearDepth();
		} finally {
			render.popState();
			render.popMatrix();
		}
	}

}