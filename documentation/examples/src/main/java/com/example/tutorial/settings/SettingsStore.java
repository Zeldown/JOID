package com.example.tutorial.settings;

import com.google.gson.JsonObject;

import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;

// #region class
@UIStoreData(id = "settings", scope = UIStoreScope.PERMANENT)
public class SettingsStore extends UIStore {

	private final BooleanSignal music  = new BooleanSignal(true);
	private final IntegerSignal volume = new IntegerSignal(8);

	public BooleanSignal getMusic() {
		return this.music;
	}

	public IntegerSignal getVolume() {
		return this.volume;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject json = new JsonObject();
		json.addProperty("music", this.music.peek());
		json.addProperty("volume", this.volume.peek());
		return json;
	}

	@Override
	public void fromJson(final JsonObject json) {
		if (json.has("music")) {
			this.music.set(json.get("music").getAsBoolean());
		}

		if (json.has("volume")) {
			this.volume.set(json.get("volume").getAsInt());
		}
	}

}
// #endregion