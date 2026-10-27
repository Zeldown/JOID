package dev.joid.demo.ui;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.demo.ui.simple.UIDemoSimple;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
	public void namesTheDemoThatCannotBeCreated() {
		try {
			DemoEntry.create(ArgumentUI.class).getAction().run();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The demo ArgumentUI cannot be created: give its UI a constructor without argument", exception.getMessage());
		}
	}

	@Test
	public void namesTheExceptionThrownByTheConstructorOfADemo() {
		try {
			DemoEntry.create(ThrowingUI.class).getAction().run();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The demo ThrowingUI cannot be created: its constructor failed (java.lang.IllegalStateException: Broken), fix the constructor from this error", exception.getMessage());
			Assert.assertEquals("Broken", exception.getCause().getMessage());
		}
	}

	@Test
	public void listsTheOverlayToggleInTheMenu() {
		Assert.assertTrue(UIDemoChoice.getEntries().stream().anyMatch(entry -> "UIDemoOverlay: off".equals(entry.getText())));
	}

	@Getter
	@RequiredArgsConstructor
	public static final class ArgumentUI extends UI {

		private final int argument;

		@Override
		public void init() {}

	}

	public static final class ThrowingUI extends UI {

		public ThrowingUI() {
			throw new IllegalStateException("Broken");
		}

		@Override
		public void init() {}

	}

}