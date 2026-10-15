package dev.joid.lib.ui.node.effect.impl;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TransformNodeEffect extends NodeEffect<Node> {

	private Transformation transformation;

	public static @NonNull TransformNodeEffect create(final @NonNull Transformation transformation) {
		return new TransformNodeEffect(transformation);
	}

	@Override
	public void draw(final @NonNull Node node, final @NonNull Runnable body) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			this.transformation.transform(node.getX() + node.getWidth() / 2D, node.getY() + node.getHeight() / 2D);
			body.run();
		});
	}

	public final <E extends TransformNodeEffect> @NonNull E transformation(final @NonNull Transformation transformation) {
		this.transformation = transformation;
		return (E) this;
	}

}