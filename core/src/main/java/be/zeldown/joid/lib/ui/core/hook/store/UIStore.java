package be.zeldown.joid.lib.ui.core.hook.store;

import com.google.gson.JsonObject;

import be.zeldown.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.NonNull;

public abstract class UIStore {

	private UIStoreData data;

	public void init() {}
	public void destroy() {}

	public void save() {
		UIStoreHook.saveStore(this);
	}

	public void save(final @NonNull JsonObject json) {}
	public void load(final @NonNull JsonObject json) {}

	public UIStoreData getData() {
		if (this.data == null) {
			if (!this.getClass().isAnnotationPresent(UIStoreData.class)) {
				throw new IllegalStateException("StoreData annotation is missing on " + this.getClass().getName());
			}

			this.data = this.getClass().getAnnotation(UIStoreData.class);
		}

		return this.data;
	}

}