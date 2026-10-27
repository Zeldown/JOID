package com.example.state;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;

// #region permanent
@UIStoreData(id = "sound", scope = UIStoreScope.PERMANENT)
// #endregion
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
	public Map<String, Object> toJson() {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put("volume", this.volume.peek());
		json.put("muted", this.muted.peek());
		return json;
	}

	@Override
	public void fromJson(final Map<String, Object> json) {
		if (json.containsKey("volume")) {
			this.volume.set(((Number) json.get("volume")).intValue());
		}

		if (json.containsKey("muted")) {
			this.muted.set((Boolean) json.get("muted"));
		}
	}

}