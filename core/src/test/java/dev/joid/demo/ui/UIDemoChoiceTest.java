package dev.joid.demo.ui;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.control.UIDemoControl;
import dev.joid.demo.ui.simple.UIDemoSimple;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.cache.TemporaryCache;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;

public class UIDemoChoiceTest {

	@ClassRule
	public static final TemporaryCache CACHE = new TemporaryCache();

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private MsdfFont     previous;
	private Supplier<UI> menu;

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Before
	public void useTheDevFont() {
		this.previous = DemoFont.MONTSERRAT;
		this.menu = UIDemoChoice.getMenu();
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
	}

	@After
	public void restoreTheDemoFont() {
		DemoFont.MONTSERRAT = this.previous;
		UIDemoChoice.menu(this.menu);
	}

	@Test
	public void staysOpenOnEscape() {
		final UIDemoChoice menu = new UIDemoChoice();
		this.bridges.open(menu);
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		this.bridges.frames(120);
		Assert.assertFalse(menu.isClosed());
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
	}

	@Test
	public void bringsADemoBackToTheMenuOnEscape() {
		final UIDemoControl demo = new UIDemoControl();
		this.bridges.open(demo);
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		this.bridges.frames(120);
		Assert.assertTrue(this.bridges.getUi().getUiList().stream().anyMatch(ui -> ui instanceof UIDemoChoice));
	}

	@Test
	public void bringsADemoBackToTheChosenMenuOnEscape() {
		UIDemoChoice.menu(UIDemoSimple::new);
		final UIDemoControl demo = new UIDemoControl();
		this.bridges.open(demo);
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		this.bridges.frames(120);
		Assert.assertTrue(this.bridges.getUi().getUiList().stream().anyMatch(ui -> ui instanceof UIDemoSimple));
		Assert.assertFalse(this.bridges.getUi().getUiList().stream().anyMatch(ui -> ui instanceof UIDemoChoice));
	}

	@Test
	public void listsItsEntriesInOrderForOtherMenus() throws ReflectiveOperationException {
		final Field field = UIDemoChoice.class.getDeclaredField("LIST");
		field.setAccessible(true);
		Assert.assertEquals(field.get(null), UIDemoChoice.getEntries());
		Assert.assertThrows(UnsupportedOperationException.class, () -> UIDemoChoice.getEntries().set(0, UIDemoChoice.getEntries().get(0)));
	}

}