package be.zeldown.joid.demo.ui.store.store;

import be.zeldown.joid.lib.ui.core.hook.store.UIStore;
import be.zeldown.joid.lib.ui.core.hook.store.context.StoreContext;
import be.zeldown.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@UIStoreData(id = "DemoGlobalStore", context = StoreContext.GLOBAL)
public class DemoGlobalStore extends UIStore {

	private long time;

	@Override
	public void init() {
		System.out.println("init DemoGlobalStore at " + this.time);
	}

}