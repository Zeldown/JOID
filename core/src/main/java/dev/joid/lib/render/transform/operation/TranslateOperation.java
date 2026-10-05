package dev.joid.lib.render.transform.operation;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.modifier.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TranslateOperation implements TransformOperation {

	private final Vector vector;

	@Override
	public void transform() {
		BridgeHandler.RENDER.get().translate(this.vector.getX(), this.vector.getY(), this.vector.getZ());
	}

	@Override
	public void reset() {
		BridgeHandler.RENDER.get().translate(-this.vector.getX(), -this.vector.getY(), -this.vector.getZ());
	}

}