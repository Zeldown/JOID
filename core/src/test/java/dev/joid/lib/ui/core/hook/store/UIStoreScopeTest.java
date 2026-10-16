package dev.joid.lib.ui.core.hook.store;

import org.junit.Assert;
import org.junit.Test;

public class UIStoreScopeTest {

	@Test
	public void keepsALocalStoreToItsUi() {
		Assert.assertTrue(UIStoreScope.LOCAL.isLocal());
		Assert.assertFalse(UIStoreScope.LOCAL.isGlobal());
	}

	@Test
	public void sharesAGlobalStore() {
		Assert.assertTrue(UIStoreScope.GLOBAL.isGlobal());
		Assert.assertFalse(UIStoreScope.GLOBAL.isLocal());
	}

	@Test
	public void sharesAPermanentStore() {
		Assert.assertTrue(UIStoreScope.PERMANENT.isGlobal());
		Assert.assertFalse(UIStoreScope.PERMANENT.isLocal());
	}

}