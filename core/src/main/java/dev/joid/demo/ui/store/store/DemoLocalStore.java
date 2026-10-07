package dev.joid.demo.ui.store.store;

import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.context.StoreContext;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@UIStoreData(id = "DemoLocalStore", context = StoreContext.LOCAL)
public class DemoLocalStore extends UIStore {

	private long time;

	@Override
	public void init() {
		System.out.println("[UIDemoStore] DemoLocalStore created at " + this.time);
	}

}