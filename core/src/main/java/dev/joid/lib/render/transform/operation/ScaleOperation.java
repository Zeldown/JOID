package dev.joid.lib.render.transform.operation;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.render.modifier.Scale;
import dev.joid.lib.render.modifier.Vector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScaleOperation implements TransformOperation {

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
		render.translate(pivotX, pivotY, pivotZ);
		render.scale(x, y, z);
		render.translate(-pivotX, -pivotY, -pivotZ);
	}

	@Override
	public void reset() {
		final double x = this.scale.getRawX();
		final double y = this.scale.getRawY();
		final double z = this.scale.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.translate(pivotX, pivotY, pivotZ);
		render.scale(1 / x, 1 / y, 1 / z);
		render.translate(-pivotX, -pivotY, -pivotZ);
	}

}