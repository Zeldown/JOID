package dev.joid.lib.render.transform.operation;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.render.transform.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TranslateTransformOperation implements ITransformOperation {

	private final Vector vector;

	@Override
	public void transform() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final double x = this.vector.getX();
		final double y = this.vector.getY();
		render.getModelView().translate(x, y, this.vector.getZ());
		render.quantize(x, y);
	}

}