package dev.joid.lib.render.transform.operation;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Vector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScaleTransformOperation implements ITransformOperation {

	private final Scale  scale;
	private final Vector pivot;

	@Override
	public void transform() {
		final double x = this.scale.getRawX();
		final double y = this.scale.getRawY();
		final double z = this.scale.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().translate(pivotX, pivotY, pivotZ);
		render.getModelView().scale(x, y, z);
		render.getModelView().translate(-pivotX, -pivotY, -pivotZ);
	}

}