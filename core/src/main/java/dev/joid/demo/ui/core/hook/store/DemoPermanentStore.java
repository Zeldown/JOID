package dev.joid.demo.ui.core.hook.store;

import com.google.gson.JsonObject;

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
	public void fromJson(final @NonNull JsonObject json) {
		this.time = json.has("time") ? json.get("time").getAsLong() : BridgeHandler.CLOCK.get().currentTimeMillis();
	}

	@Override
	public @NonNull JsonObject toJson() {
		final JsonObject json = new JsonObject();
		json.addProperty("time", this.time);
		return json;
	}

}