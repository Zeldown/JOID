package com.example.tutorial.polish;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;

// #region class
@UIStoreData(id = "settings", scope = UIStoreScope.PERMANENT)
public class SettingsStore extends UIStore {

	private final IntegerSignal volume = new IntegerSignal(8);
	private final BooleanSignal music  = new BooleanSignal(true);

	public BooleanSignal getMusic() {
		return this.music;
	}

	public IntegerSignal getVolume() {
		return this.volume;
	}

	@Override
	public Map<String, Object> toJson() {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put("music", this.music.peek());
		json.put("volume", this.volume.peek());
		return json;
	}

	@Override
	public void fromJson(final Map<String, Object> json) {
		if (json.containsKey("music")) {
			this.music.set((Boolean) json.get("music"));
		}

		if (json.containsKey("volume")) {
			this.volume.set(((Number) json.get("volume")).intValue());
		}
	}

}
// #endregion