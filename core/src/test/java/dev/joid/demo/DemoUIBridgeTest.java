package dev.joid.demo;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.cache.TemporaryCache;

public class DemoUIBridgeTest {

	@ClassRule
	public static final TemporaryCache CACHE = new TemporaryCache();

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Test
	public void opensTheMenuItsSubclassChoosesOnStart() {
		final MenuBridge bridge = new MenuBridge();
		bridge.start();
		Assert.assertEquals(1, bridge.menus);
	}

	public static final class MenuBridge extends DemoUIBridge {

		private int menus;

		@Override
		protected void openMenu() {
			this.menus++;
		}

	}

}