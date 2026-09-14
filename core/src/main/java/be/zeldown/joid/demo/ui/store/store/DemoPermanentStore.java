package be.zeldown.joid.demo.ui.store.store;

import com.google.gson.JsonObject;

import be.zeldown.joid.lib.ui.core.hook.store.UIStore;
import be.zeldown.joid.lib.ui.core.hook.store.context.StoreContext;
import be.zeldown.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@UIStoreData(id = "DemoPermanentStore", context = StoreContext.PERMANENT)
public class DemoPermanentStore extends UIStore {

	private long time;

	@Override
	public void init() {
		System.out.println("init DemoPermanentStore at " + this.time);
	}

	@Override
	public void load(final @NonNull JsonObject json) {
		this.time = json.has("time") ? json.get("time").getAsLong() : System.currentTimeMillis();
	}

	@Override
	public void save(@NonNull final JsonObject json) {
		json.addProperty("time", this.time);
	}

}