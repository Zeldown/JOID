package dev.joid.demo.ui.core.hook.store;

import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@UIStoreData(id = "DemoGlobalStore", scope = UIStoreScope.GLOBAL)
public class DemoGlobalStore extends UIStore {

	private long time;

}