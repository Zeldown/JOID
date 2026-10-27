package dev.joid.lib.ui.core.hook.store;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.NonNull;

public abstract class UIStore {

	public void init() {}

	public void destroy() {}

	public void save() {
		UIStoreHook.saveStore(this);
	}

	public @NonNull Map<String, Object> toJson() {
		return new LinkedHashMap<>();
	}

	public void fromJson(final @NonNull Map<String, Object> json) {}

	public UIStoreData getData() {
		return UIStoreHook.getData(this.getClass());
	}

}