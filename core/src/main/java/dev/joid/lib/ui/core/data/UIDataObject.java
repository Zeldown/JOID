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
		switch (this.anchorX) {
		case CENTER:
			return UIView.DESIGN_WIDTH / 2D;
		case END:
			return UIView.DESIGN_WIDTH;
		case START:
		default:
			return 0D;
		}
	}

	public double getAnchorPositionY() {
		switch (this.anchorY) {
		case CENTER:
			return UIView.DESIGN_HEIGHT / 2D;
		case END:
			return UIView.DESIGN_HEIGHT;
		case START:
		default:
			return 0D;
		}
	}

	public @NonNull Color getBackgroundColor() {
		return this.backgroundColorCache;
	}

	public @NonNull UIDataObject backgroundColor(final @NonNull String backgroundColor) {
		this.backgroundColorCache = Color.decode(backgroundColor);
		this.backgroundColor = backgroundColor;
		return this;
	}

}