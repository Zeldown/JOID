package dev.joid.lib.ui.core.hook.store.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.context.StoreContext;

public class UIStoreDataTest {

	@Test
	public void describesALocalStoreWithoutIdByDefault() {
		final UIStoreData data = new DefaultStore().getData();
		Assert.assertEquals("", data.id());
		Assert.assertSame(StoreContext.LOCAL, data.context());
	}

	@Test
	public void keepsTheDeclaredIdAndContext() {
		final UIStoreData data = new DeclaredStore().getData();
		Assert.assertEquals("inventory", data.id());
		Assert.assertSame(StoreContext.PERMANENT, data.context());
	}

	@UIStoreData
	public static class DefaultStore extends UIStore {}

	@UIStoreData(id = "inventory", context = StoreContext.PERMANENT)
	public static class DeclaredStore extends UIStore {}

}