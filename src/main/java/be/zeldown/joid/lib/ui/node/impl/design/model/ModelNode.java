package be.zeldown.joid.lib.ui.node.impl.design.model;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.model.utils.IDrawableModel;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
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

		double sizeX = super.dw(modelWidth);
		double sizeY = sizeX * (modelHeight / modelWidth);
		double sizeZ = sizeX * (modelDepth / modelWidth);

		sizeX *= this.size;
		sizeY *= this.size;
		sizeZ *= this.size;

		final double drawX = super.getX() + super.getWidth() / 2D;
		final double drawY = super.getY() + super.getHeight() / 2D;
		final double drawZ = modelDepth / 2D * sizeZ;

		GL11.glPushMatrix();
		GL11.glTranslated(drawX, drawY, drawZ);
		GL11.glRotated(this.rotationYaw, 0D, 1D, 0D);
		GL11.glRotated(this.rotationPitch, 1D, 0D, 0D);
		GL11.glTranslated(-drawX, -drawY, -drawZ);
		GL11.glTranslated(0D, 0D, drawZ);
		DrawUtils.MODEL.drawModel(drawX, drawY, sizeX, sizeY, sizeZ, this.model);
		GL11.glPopMatrix();

		super.getUi().setRenderPipelineLevel(super.getUi().getRenderPipelineLevel() + (this.pipeLineLevel == -1D ? modelDiagonal * sizeZ : this.pipeLineLevel));
	}

	public <T extends ModelNode> @NonNull T model(final @NonNull IDrawableModel model) {
		this.model = model;
		return (T) this;
	}

	public <T extends ModelNode> @NonNull T size(final double size) {
		this.size = size;
		return (T) this;
	}

	public <T extends ModelNode> @NonNull T rotationYaw(final double rotationYaw) {
		this.rotationYaw = rotationYaw;
		return (T) this;
	}

	public <T extends ModelNode> @NonNull T rotationPitch(final double rotationPitch) {
		this.rotationPitch = rotationPitch;
		return (T) this;
	}

	public <T extends ModelNode> @NonNull T pipeLineLevel(final double pipeLineLevel) {
		this.pipeLineLevel = pipeLineLevel;
		return (T) this;
	}

}
