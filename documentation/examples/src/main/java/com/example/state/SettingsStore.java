package com.example.state;

import com.google.gson.JsonObject;

import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;

@UIStoreData(id = "settings", scope = UIStoreScope.PERMANENT)
public class SettingsStore extends UIStore {

	private final IntegerSignal volume = new IntegerSignal(5);
	private final BooleanSignal muted  = new BooleanSignal(false);

	public IntegerSignal getVolume() {
		return this.volume;
	}

	public BooleanSignal getMuted() {
		return this.muted;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject json = new JsonObject();
		json.addProperty("volume", this.volume.peek());
		json.addProperty("muted", this.muted.peek());
		return json;
	}

	@Override
	public void fromJson(final JsonObject json) {
		if (json.has("volume")) {
			this.volume.set(json.get("volume").getAsInt());
		}

		if (json.has("muted")) {
			this.muted.set(json.get("muted").getAsBoolean());
		}
	}

}