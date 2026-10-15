package dev.joid.lib.ui.core.hook.store.scope;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StoreScope {

	LOCAL(false),
	GLOBAL(true),
	PERMANENT(true);

	private final boolean global;

	public boolean isLocal() {
		return !this.global;
	}

}