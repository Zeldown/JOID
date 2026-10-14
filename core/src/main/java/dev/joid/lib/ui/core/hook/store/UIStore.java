package dev.joid.lib.ui.core.hook.store;

import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.NonNull;

public abstract class UIStore {

	private UIStoreData data;

	public void init() {}

	public void destroy() {}

	public void save() {
		UIStoreHook.saveStore(this);
	}

	public void fromJson(final @NonNull String json) {}

	public @NonNull String toJson() {
		return "{}";
	}

	public UIStoreData getData() {
		if (this.data == null) {
			this.data = UIStoreHook.getData(this.getClass());
		}

		return this.data;
	}

}