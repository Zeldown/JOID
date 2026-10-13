package dev.joid.lib.ui.event;

import dev.joid.lib.ui.core.UI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UIEvent<U extends UI> extends Event {

	@NonNull
	private final U ui;

}