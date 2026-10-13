package dev.joid.demo.ui.core.hook.store;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern TIME = Pattern.compile("\"time\"\\s*:\\s*(\\d+)");

	private long time;

	@Override
	public void init() {
		System.out.println("[UIDemoStore] DemoPermanentStore created at " + this.time);
	}

	@Override
	public void fromJson(final @NonNull String json) {
		final Matcher matcher = DemoPermanentStore.TIME.matcher(json);
		this.time = matcher.find() ? Long.parseLong(matcher.group(1)) : BridgeHandler.CLOCK.get().currentTimeMillis();
	}

	@Override
	public @NonNull String toJson() {
		return "{\"time\":" + this.time + "}";
	}

}