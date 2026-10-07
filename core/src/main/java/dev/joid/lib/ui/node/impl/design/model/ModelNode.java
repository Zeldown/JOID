package dev.joid.lib.ui.node.impl.design.model;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.model.utils.IDrawableModel;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ModelNode extends Node {

	private IDrawableModel model;

	private double  size;
	private double  rotationYaw;
	private double  rotationPitch;

	private double pipeLineLevel;

	protected ModelNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.size          = 1D;
		this.rotationYaw   = 0D;
		this.rotationPitch = 0D;
		this.pipeLineLevel = -1D;
	}

	public static @NonNull ModelNode create(final double x, final double y, final double width, final double height) {
		return new ModelNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.model == null || this.size == 0D) {
			return;
		}

		final double modelWidth  = this.model.getWidth() == 0 ? 1 : this.model.getWidth();
		final double modelHeight = this.model.getHeight() == 0 ? 1 : this.model.getHeight();
		final double modelDepth  = this.model.getDepth() == 0 ? 1 : this.model.getDepth();

		final double modelDiagonal = Math.sqrt(modelWidth * modelWidth + modelHeight * modelHeight + modelDepth * modelDepth);

		final double scale = super.dw(modelWidth) * this.size;

		final double drawX = super.getX() + super.getWidth() / 2D;
		final double drawY = super.getY() + super.getHeight() / 2D;
		final double drawZ = modelDepth / 2D * scale;

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushMatrix();
		try {
			render.translate(drawX, drawY, drawZ);
			render.rotate(this.rotationYaw, 0D, 1D, 0D);
			render.rotate(this.rotationPitch, 1D, 0D, 0D);
			render.translate(-drawX, -drawY, -drawZ);
			render.translate(0D, 0D, drawZ);
			DrawUtils.MODEL.drawModel(drawX, drawY, scale, this.model);
		} finally {
			render.popMatrix();
		}

		super.getUi().setRenderPipelineLevel(super.getUi().getRenderPipelineLevel() + (this.pipeLineLevel == -1D ? modelDiagonal * scale : this.pipeLineLevel));
	}

	public final <T extends ModelNode> @NonNull T model(final @NonNull IDrawableModel model) {
		return this.model(Signal.from(model));
	}

	public final <T extends ModelNode> @NonNull T model(final @NonNull Supplier<@NonNull IDrawableModel> model) {
		return super.follow("model", model, value -> this.model = value);
	}

	public final <T extends ModelNode> @NonNull T size(final double size) {
		return this.size(Signal.from(size));
	}

	public final <T extends ModelNode> @NonNull T size(final @NonNull Supplier<Double> size) {
		return super.follow("size", size, value -> this.size = value);
	}

	public final <T extends ModelNode> @NonNull T rotationYaw(final double rotationYaw) {
		return this.rotationYaw(Signal.from(rotationYaw));
	}

	public final <T extends ModelNode> @NonNull T rotationYaw(final @NonNull Supplier<Double> rotationYaw) {
		return super.follow("rotationYaw", rotationYaw, value -> this.rotationYaw = value);
	}

	public final <T extends ModelNode> @NonNull T rotationPitch(final double rotationPitch) {
		return this.rotationPitch(Signal.from(rotationPitch));
	}

	public final <T extends ModelNode> @NonNull T rotationPitch(final @NonNull Supplier<Double> rotationPitch) {
		return super.follow("rotationPitch", rotationPitch, value -> this.rotationPitch = value);
	}

	public final <T extends ModelNode> @NonNull T pipeLineLevel(final double pipeLineLevel) {
		return this.pipeLineLevel(Signal.from(pipeLineLevel));
	}

	public final <T extends ModelNode> @NonNull T pipeLineLevel(final @NonNull Supplier<Double> pipeLineLevel) {
		return super.follow("pipeLineLevel", pipeLineLevel, value -> this.pipeLineLevel = value);
	}

	protected final void transform(final double size, final double rotationYaw, final double rotationPitch) {
		this.size          = size;
		this.rotationYaw   = rotationYaw;
		this.rotationPitch = rotationPitch;
	}

}