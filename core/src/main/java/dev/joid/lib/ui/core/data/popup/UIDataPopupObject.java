package dev.joid.lib.ui.core.data.popup;

import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(fluent = true, chain = true)
public final class UIDataPopupObject {

	private boolean active = false;

	@NonNull
	private PopupTransition transition = PopupTransition.IN_OUT;

}