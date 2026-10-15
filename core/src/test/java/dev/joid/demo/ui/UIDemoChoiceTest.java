package dev.joid.demo.ui;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.control.UIDemoControl;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.input.key.Key;

public class UIDemoChoiceTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private MsdfFont previous;

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Before
	public void useTheDevFont() {
		this.previous = DemoFont.MONTSERRAT;
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
	}

	@After
	public void restoreTheDemoFont() {
		DemoFont.MONTSERRAT = this.previous;
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

}