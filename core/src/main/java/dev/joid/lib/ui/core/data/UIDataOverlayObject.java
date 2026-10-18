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
public final class UIDataOverlayObject {

	private final UIDataOverlayRenderObject      render      = UIDataOverlayRenderObject.create();
	private final UIDataOverlayInteractionObject interaction = UIDataOverlayInteractionObject.create();

	private boolean active = false;

	public static @NonNull UIDataOverlayObject create() {
		return new UIDataOverlayObject();
	}

}