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
public final class UIDataDebugObject {

	private boolean profiler  = true;
	private boolean hotreload = true;

	public static @NonNull UIDataDebugObject create() {
		return new UIDataDebugObject();
	}

}