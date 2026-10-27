package dev.joid.demo.ui.core.hook.store;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@UIStoreData(id = "DemoPermanentStore", scope = UIStoreScope.PERMANENT)
public class DemoPermanentStore extends UIStore {

	private long time;

	@Override
	public @NonNull Map<String, Object> toJson() {
		final Map<String, Object> json = new LinkedHashMap<>();
		json.put("time", this.time);
		return json;
	}

	@Override
	public void fromJson(final @NonNull Map<String, Object> json) {
		final Object time = json.get("time");
		this.time = time instanceof Number ? ((Number) time).longValue() : BridgeHandler.CLOCK.get().currentTimeMillis();
	}

}