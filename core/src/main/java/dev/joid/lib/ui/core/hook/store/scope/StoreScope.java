package dev.joid.lib.ui.core.hook.store.scope;

public enum StoreScope {

	LOCAL(false),
	GLOBAL(true),
	PERMANENT(true);

	private final boolean global;

	private StoreScope(final boolean global) {
		this.global = global;
	}

	public boolean isLocal() {
		return !this.global;
	}

	public boolean isGlobal() {
		return this.global;
	}

}