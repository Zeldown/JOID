package dev.joid.demo.ui.core.hook.store;

import com.google.gson.JsonObject;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.hook.store.scope.StoreScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@UIStoreData(id = "DemoPermanentStore", scope = StoreScope.PERMANENT)
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