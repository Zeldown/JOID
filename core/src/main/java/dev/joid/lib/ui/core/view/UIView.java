package dev.joid.lib.ui.core.view;

import dev.joid.lib.bridge.render.IRenderBridge;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UIView {

	private double anchorX;
	private double anchorY;

	private double zoom;
	private double width;
	private double height;
	private double viewportWidth;
	private double interfaceScale;
	private double viewportHeight;

	private UIView(final double anchorX, final double anchorY) {
		this.anchorX = anchorX;
		this.anchorY = anchorY;
		this.zoom = 1D;
		this.interfaceScale = 1D;
		this.viewportWidth = 1920D;
		this.viewportHeight = 1080D;
	}

	public static @NonNull UIView create(final double anchorX, final double anchorY) {
		return new UIView(anchorX, anchorY);
	}

	public double getScale() {
		return this.interfaceScale * this.zoom;
	}

	public double getMaxZoom() {
		return Math.max(1D, 1D / this.interfaceScale);
	}

	public double getOffsetX() {
		return (this.viewportWidth - 1920D) * this.anchorX / 1920D;
	}

	public double getOffsetY() {
		return (this.viewportHeight - 1080D) * this.anchorY / 1080D;
	}

	public double getVisibleWidth() {
		return this.viewportWidth / this.getScale();
	}

	public double getVisibleHeight() {
		return this.viewportHeight / this.getScale();
	}

	public double toUiX(final double screenX) {
		return (screenX * this.viewportWidth / this.width - this.getOffsetX() - this.anchorX) / this.getScale() + this.anchorX;
	}

	public double toUiY(final double screenY) {
		return (screenY * this.viewportHeight / this.height - this.getOffsetY() - this.anchorY) / this.getScale() + this.anchorY;
	}

	public double toScreenX(final double uiX) {
		return (this.getOffsetX() + this.anchorX + (uiX - this.anchorX) * this.getScale()) * this.width / this.viewportWidth;
	}

	public double toScreenY(final double uiY) {
		return (this.getOffsetY() + this.anchorY + (uiY - this.anchorY) * this.getScale()) * this.height / this.viewportHeight;
	}

	public double toScreenWidth(final double uiWidth) {
		return uiWidth * this.getScale() * this.width / this.viewportWidth;
	}

	public double toScreenHeight(final double uiHeight) {
		return uiHeight * this.getScale() * this.height / this.viewportHeight;
	}

	public @NonNull UIView anchorX(final double anchorX) {
		this.anchorX = anchorX;
		return this;
	}

	public @NonNull UIView anchorY(final double anchorY) {
		this.anchorY = anchorY;
		return this;
	}

	public @NonNull UIView zoom(final double zoom) {
		this.zoom = Math.max(0.1D, Math.min(this.getMaxZoom(), zoom));
		return this;
	}

	public @NonNull UIView interfaceScale(final double interfaceScale) {
		this.interfaceScale = interfaceScale;
		return this.zoom(this.zoom);
	}

	public @NonNull UIView resize(final double width, final double height) {
		final double fit = Math.min(width / 1920D, height / 1080D);
		this.width = width;
		this.height = height;
		this.viewportWidth = width / fit;
		this.viewportHeight = height / fit;
		return this;
	}

	public void render(final @NonNull IRenderBridge render, final boolean projection, final @NonNull Runnable draw) {
		if (projection) {
			render.pushProjection();
			render.ortho(0D, this.viewportWidth, this.viewportHeight, 0D, 0D, 10000D);
		}

		render.pushMatrix();
		try {
			render.translate(this.getOffsetX(), this.getOffsetY(), 0D);
			if (this.getScale() != 1D) {
				render.translate(this.anchorX, this.anchorY, 0D);
				render.scale(this.getScale(), this.getScale(), 1D);
				render.translate(-this.anchorX, -this.anchorY, 0D);
			}

			draw.run();
		} finally {
			render.popMatrix();
			if (projection) {
				render.popProjection();
			}
		}
	}

}