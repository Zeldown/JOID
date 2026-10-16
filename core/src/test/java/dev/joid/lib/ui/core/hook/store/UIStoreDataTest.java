package dev.joid.lib.ui.core.hook.store;

import org.junit.Assert;
import org.junit.Test;

public class UIStoreDataTest {

	@Test
	public void describesALocalStoreWithoutIdByDefault() {
		final UIStoreData data = new DefaultStore().getData();
		Assert.assertEquals("", data.id());
		Assert.assertSame(UIStoreScope.LOCAL, data.scope());
	}

	@Test
	public void keepsTheDeclaredIdAndContext() {
		final UIStoreData data = new DeclaredStore().getData();
		Assert.assertEquals("inventory", data.id());
		Assert.assertSame(UIStoreScope.PERMANENT, data.scope());
	}

	@UIStoreData
	public static class DefaultStore extends UIStore {}

	@UIStoreData(id = "inventory", scope = UIStoreScope.PERMANENT)
	public static class DeclaredStore extends UIStore {}

}