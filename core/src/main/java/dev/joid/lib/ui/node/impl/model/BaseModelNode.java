package dev.joid.lib.ui.node.impl.model;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.model.IDrawableModel;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class BaseModelNode extends Node {

	private IDrawableModel model;

	protected BaseModelNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.<BaseModelNode>require("model", "call model(...) before attaching it", node -> node.model != null);
	}

	public final <T extends BaseModelNode> @NonNull T model(final @NonNull IDrawableModel model) {
		return this.model(Signal.from(model));
	}

	public final <T extends BaseModelNode> @NonNull T model(final @NonNull Supplier<@NonNull IDrawableModel> model) {
		return super.follow("model", model, value -> this.model = value);
	}

	protected final void drawModel(final double zoom, final double rotationYaw, final double rotationPitch) {
		if (zoom == 0D) {
			return;
		}

		final double modelWidth = this.model.getWidth() == 0D ? 1D : this.model.getWidth();
		final double modelDepth = this.model.getDepth() == 0D ? 1D : this.model.getDepth();

		final double scale = super.dw(modelWidth) * zoom;

		final double drawX = super.getX() + super.getWidth() / 2D;
		final double drawY = super.getY() + super.getHeight() / 2D;
		final double drawZ = modelDepth / 2D * scale;

		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(drawX, drawY, drawZ);
			matrix.rotate(rotationYaw, 0D, 1D, 0D);
			matrix.rotate(rotationPitch, 1D, 0D, 0D);
			matrix.translate(-drawX, -drawY, -drawZ);
			matrix.translate(0D, 0D, drawZ);
			DrawUtils.MODEL.drawModel(drawX, drawY, scale, this.model);
		});
	}

}