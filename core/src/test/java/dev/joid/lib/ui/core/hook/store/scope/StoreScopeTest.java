package dev.joid.lib.ui.core.hook.store.scope;

import org.junit.Assert;
import org.junit.Test;

public class StoreScopeTest {

	@Test
	public void keepsALocalStoreToItsUi() {
		Assert.assertTrue(StoreScope.LOCAL.isLocal());
		Assert.assertFalse(StoreScope.LOCAL.isGlobal());
	}

	@Test
	public void sharesAGlobalStore() {
		Assert.assertTrue(StoreScope.GLOBAL.isGlobal());
		Assert.assertFalse(StoreScope.GLOBAL.isLocal());
	}

	@Test
	public void sharesAPermanentStore() {
		Assert.assertTrue(StoreScope.PERMANENT.isGlobal());
		Assert.assertFalse(StoreScope.PERMANENT.isLocal());
	}

}