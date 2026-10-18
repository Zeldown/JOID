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
public final class UIDataOverlayRenderObject {

	private boolean always       = false;
	private boolean aboveScreens = false;

	public static @NonNull UIDataOverlayRenderObject create() {
		return new UIDataOverlayRenderObject();
	}

}