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
public final class UIDataOverlayInteractionObject {

	private boolean active         = false;
	private boolean cancelClick    = true;
	private boolean cancelScroll   = true;
	private boolean cancelKeyboard = true;

	public static @NonNull UIDataOverlayInteractionObject create() {
		return new UIDataOverlayInteractionObject();
	}

}