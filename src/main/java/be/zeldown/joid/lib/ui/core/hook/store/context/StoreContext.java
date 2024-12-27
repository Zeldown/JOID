package be.zeldown.joid.lib.ui.core.hook.store.context;

public enum StoreContext {

	LOCAL(false),
	GLOBAL(true),
	PERMANENT(true);

	private final boolean global;

	StoreContext(final boolean global) {
		this.global = global;
	}

	public boolean isGlobal() {
		return this.global;
	}

	public boolean isLocal() {
		return !this.global;
	}

}