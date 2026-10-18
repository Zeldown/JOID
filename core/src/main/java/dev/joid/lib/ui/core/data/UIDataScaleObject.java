package dev.joid.lib.ui.core.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIDataScaleObject {

	private double  limit   = 1D;
	private boolean active  = true;
	private boolean limited = false;

	public static @NonNull UIDataScaleObject create() {
		return new UIDataScaleObject();
	}

	public double apply(final double interfaceScale) {
		if (!this.active) {
			return 1D;
		}

		return this.limited ? Math.min(interfaceScale, this.limit) : interfaceScale;
	}

}