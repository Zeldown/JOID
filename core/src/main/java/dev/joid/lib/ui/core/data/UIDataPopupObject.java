package dev.joid.lib.ui.core.data;

import dev.joid.lib.ui.core.data.UIDataPopup.PopupTransition;
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
public final class UIDataPopupObject {

	private boolean active = false;

	@NonNull
	private PopupTransition transition = PopupTransition.IN_OUT;

	public static @NonNull UIDataPopupObject create() {
		return new UIDataPopupObject();
	}

}