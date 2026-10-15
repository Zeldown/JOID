package dev.joid.lib.ui.core.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
public final class UIDataOverlayObject {

	private final UIDataOverlayRenderObject      render      = new UIDataOverlayRenderObject();
	private final UIDataOverlayInteractionObject interaction = new UIDataOverlayInteractionObject();

	private boolean active = false;

}