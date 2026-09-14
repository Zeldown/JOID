package be.zeldown.joid.lib.render.transform.operation;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.render.modifier.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TranslateOperation implements TransformOperation {

	private final Vector vector;

	@Override
	public void transform() {
		BridgeHandler.getRender().translate(this.vector.getX(), this.vector.getY(), this.vector.getZ());
	}

	@Override
	public void reset() {
		BridgeHandler.getRender().translate(-this.vector.getX(), -this.vector.getY(), -this.vector.getZ());
	}

}