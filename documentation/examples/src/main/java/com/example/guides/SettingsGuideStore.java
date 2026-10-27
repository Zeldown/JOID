package com.example.guides;

import java.util.LinkedHashMap;
import java.util.Map;

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
	public Map<String, Object> toJson() {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put("sound", this.sound.peek());
		json.put("volume", this.volume.peek());
		return json;
	}

	@Override
	public void fromJson(final Map<String, Object> json) {
		if (json.containsKey("sound")) {
			this.sound.set((Boolean) json.get("sound"));
		}

		if (json.containsKey("volume")) {
			this.volume.set(((Number) json.get("volume")).intValue());
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