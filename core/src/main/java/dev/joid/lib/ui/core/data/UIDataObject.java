package dev.joid.lib.ui.core.data;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.utils.align.Align;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
public final class UIDataObject {

	private int     zindex     = 0;
	private double  zlevel     = 0D;
	private boolean active     = true;
	private boolean visible    = true;
	private boolean zoomable   = true;
	private boolean closeable  = true;
	private boolean projection = true;
	private boolean background = false;

	@NonNull private Align  anchorX         = Align.CENTER;
	@NonNull private Align  anchorY         = Align.CENTER;
	@NonNull private String backgroundColor = "#101010C0";

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Color backgroundColorCache = Color.decode(this.backgroundColor);

	public double getAnchorPositionX() {
		return UIDataObject.position(this.anchorX, UIView.DESIGN_WIDTH);
	}

	public double getAnchorPositionY() {
		return UIDataObject.position(this.anchorY, UIView.DESIGN_HEIGHT);
	}

	public @NonNull Color getBackgroundColor() {
		return this.backgroundColorCache;
	}

	public @NonNull UIDataObject backgroundColor(final @NonNull String backgroundColor) {
		this.backgroundColorCache = Color.decode(backgroundColor);
		this.backgroundColor = backgroundColor;
		return this;
	}

	private static double position(final Align align, final double size) {
		return align.isStart() ? 0D : align.isCenter() ? size / 2D : size;
	}

}