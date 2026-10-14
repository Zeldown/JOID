package dev.joid.lib.ui.core.data.overlay.render;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
public final class UIDataOverlayRenderObject {

	private boolean always       = false;
	private boolean aboveScreens = false;

}