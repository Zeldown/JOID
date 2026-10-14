package dev.joid.lib.ui.core.data.overlay.interaction;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
public final class UIDataOverlayInteractionObject {

	private boolean active         = false;
	private boolean cancelClick    = true;
	private boolean cancelScroll   = true;
	private boolean cancelKeyboard = true;

}