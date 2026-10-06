package dev.joid.lib.ui.core.hook.store.context;

import org.junit.Assert;
import org.junit.Test;

public class StoreContextTest {

	@Test
	public void keepsALocalStoreToItsUi() {
		Assert.assertTrue(StoreContext.LOCAL.isLocal());
		Assert.assertFalse(StoreContext.LOCAL.isGlobal());
	}

	@Test
	public void sharesAGlobalStore() {
		Assert.assertTrue(StoreContext.GLOBAL.isGlobal());
		Assert.assertFalse(StoreContext.GLOBAL.isLocal());
	}

	@Test
	public void sharesAPermanentStore() {
		Assert.assertTrue(StoreContext.PERMANENT.isGlobal());
		Assert.assertFalse(StoreContext.PERMANENT.isLocal());
	}

}