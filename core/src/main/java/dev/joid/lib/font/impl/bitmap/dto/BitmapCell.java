package dev.joid.lib.font.impl.bitmap.dto;

import dev.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class BitmapCell {

	private final int      texelTop;
	private final int      texelLeft;
	private final int      texelRight;
	private final int      texelBottom;
	private final ITexture texture;

	private double  top;
	private double  left;
	private double  right;
	private boolean bold;
	private double  bottom;
	private boolean grayscale;

	private BitmapCell(final ITexture texture, final int texelLeft, final int texelTop, final int texelRight, final int texelBottom) {
		this.texture = texture;
		this.texelLeft = texelLeft;
		this.texelTop = texelTop;
		this.texelRight = texelRight;
		this.texelBottom = texelBottom;
		this.right = texelRight - texelLeft;
		this.bottom = texelBottom - texelTop;
	}

	public static @NonNull BitmapCell create(final @NonNull ITexture texture, final int texelLeft, final int texelTop, final int texelRight, final int texelBottom) {
		if (texelRight <= texelLeft || texelBottom <= texelTop) {
			throw new IllegalArgumentException("A bitmap cell needs at least one texel: " + texelLeft + ", " + texelTop + ", " + texelRight + ", " + texelBottom);
		}

		return new BitmapCell(texture, texelLeft, texelTop, texelRight, texelBottom);
	}

	public @NonNull BitmapCell bold(final boolean bold) {
		this.bold = bold;
		return this;
	}

	public @NonNull BitmapCell grayscale(final boolean grayscale) {
		this.grayscale = grayscale;
		return this;
	}

	public @NonNull BitmapCell bounds(final double left, final double top, final double right, final double bottom) {
		if (right <= left || bottom <= top) {
			throw new IllegalArgumentException("The bounds of a bitmap cell cannot be empty: " + left + ", " + top + ", " + right + ", " + bottom);
		}

		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		return this;
	}

	public double getWidth() {
		return this.right - this.left;
	}

	public double getHeight() {
		return this.bottom - this.top;
	}

	public int getTexelWidth() {
		return this.texelRight - this.texelLeft;
	}

	public int getTexelHeight() {
		return this.texelBottom - this.texelTop;
	}

}