package dev.joid.lib.ui.core.data.scale;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
public final class UIDataScaleObject {

	private double  limit   = 1D;
	private boolean active  = true;
	private boolean limited = false;

	public double apply(final double interfaceScale) {
		if (!this.active) {
			return 1D;
		}

		return this.limited ? Math.min(interfaceScale, this.limit) : interfaceScale;
	}

}