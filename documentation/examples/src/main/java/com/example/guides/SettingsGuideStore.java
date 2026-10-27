package com.example.guides;

import com.google.gson.JsonObject;

import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;

// #region fields
@UIStoreData(id = "audio", scope = UIStoreScope.PERMANENT)
public class SettingsGuideStore extends UIStore {

	private final BooleanSignal sound  = new BooleanSignal(true);
	private final IntegerSignal volume = new IntegerSignal(80);
	// #endregion

	// #region json
	@Override
	public JsonObject toJson() {
		final JsonObject json = new JsonObject();
		json.addProperty("sound", this.sound.peek());
		json.addProperty("volume", this.volume.peek());
		return json;
	}

	@Override
	public void fromJson(final JsonObject json) {
		if (json.has("sound")) {
			this.sound.set(json.get("sound").getAsBoolean());
		}

		if (json.has("volume")) {
			this.volume.set(json.get("volume").getAsInt());
		}
	}
	// #endregion

	// #region getters
	public BooleanSignal getSound() {
		return this.sound;
	}

	public IntegerSignal getVolume() {
		return this.volume;
	}
	// #endregion

}