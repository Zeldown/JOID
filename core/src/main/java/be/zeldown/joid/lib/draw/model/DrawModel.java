package be.zeldown.joid.lib.draw.model;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.draw.model.utils.IDrawableModel;
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
		render.translate(x, y, 0D);
		render.scale(sizeX, sizeY, sizeZ);
		render.rotate(180D, 0D, 1D, 0D);

		render.cull(false);
		render.lighting(true);

		model.render();

		render.lighting(false);

		render.popMatrix();
	}

}