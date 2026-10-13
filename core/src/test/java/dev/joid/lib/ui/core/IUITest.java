package dev.joid.lib.ui.core;

import org.junit.Assert;
import org.junit.Test;

public class IUITest {

	@Test
	public void initializesAndUpdatesNothingByDefault() {
		final PlainUI ui = new PlainUI();
		ui.init();
		ui.update();
		Assert.assertFalse(ui.toString().isEmpty());
	}

	public static final class PlainUI implements IUI {}

}