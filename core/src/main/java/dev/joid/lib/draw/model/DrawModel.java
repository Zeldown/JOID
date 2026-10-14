package dev.joid.lib.draw.model;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawModel {

	private static final DrawModel INSTANCE = new DrawModel();

	public static @NonNull DrawModel inst() {
		return DrawModel.INSTANCE;
	}

	public void drawModel(final double x, final double y, final double size, final @NonNull IDrawableModel model) {
		this.drawModel(x, y, size, size, size, model);
	}

	public void drawModel(final double x, final double y, final double sizeX, final double sizeY, final double sizeZ, final @NonNull IDrawableModel model) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().push();
		render.pushState();
		try {
			render.getModelView().translate(x, y, 0D);
			render.getModelView().scale(sizeX, -sizeY, sizeZ);

			render.getState().cull(false).lighting(true).depthTest(true).depthWrite(true);
			render.clearDepth();

			model.render();
			render.clearDepth();
		} finally {
			render.popState();
			render.getModelView().pop();
		}
	}

}