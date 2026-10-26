package dev.joid.lib.ui.core.hook.store;

import com.google.gson.JsonObject;

import lombok.NonNull;

public abstract class UIStore {

	public void init() {}

	public void destroy() {}

	public void save() {
		UIStoreHook.saveStore(this);
	}

	public @NonNull JsonObject toJson() {
		return new JsonObject();
	}

	public void fromJson(final @NonNull JsonObject json) {}

	public UIStoreData getData() {
		return UIStoreHook.getData(this.getClass());
	}

}