package dev.joid.demo.ui;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.demo.ui.simple.UIDemoSimple;

public class DemoEntryTest {

	@Test
	public void namesAUiEntryAfterItsClass() {
		final DemoEntry entry = DemoEntry.create(UIDemoSimple.class);
		Assert.assertEquals("UIDemoSimple", entry.getText());
		Assert.assertEquals(UIDemoSimple.class.getName(), entry.getHover());
		Assert.assertFalse(entry.isActive());
	}

	@Test
	public void showsTheStateOfAnActionEntry() {
		final boolean[] on = {false};
		final DemoEntry entry = DemoEntry.create("Overlay", () -> on[0] = !on[0]).state(() -> on[0]);
		Assert.assertEquals("Overlay: off", entry.getText());
		Assert.assertFalse(entry.isActive());
		entry.getAction().run();
		Assert.assertEquals("Overlay: on", entry.getText());
		Assert.assertTrue(entry.isActive());
		Assert.assertNull(entry.getHover());
	}

	@Test
	public void listsTheOverlayToggleInTheMenu() {
		Assert.assertEquals("UIDemoOverlay: off", UIDemoChoice.LIST.get(UIDemoChoice.LIST.size() - 1).getText());
	}

}