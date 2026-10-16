package dev.joid.demo.ui.model;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.impl.msdf.MsdfFont;

public class UIDemoModelTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Test
	public void stopsItsEndlessAnimatorsOnceClosed() throws ReflectiveOperationException {
		final MsdfFont previous = DemoFont.MONTSERRAT;
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
		try {
			final int running = UIDemoModelTest.getRunningSet().size();
			final UIDemoModel ui = new UIDemoModel();
			this.bridges.open(ui);
			Assert.assertTrue(UIDemoModelTest.getRunningSet().size() > running);
			ui.getData().active(false);
			Assert.assertTrue(ui.fireClose());
			Assert.assertEquals(running, UIDemoModelTest.getRunningSet().size());
		} finally {
			DemoFont.MONTSERRAT = previous;
		}
	}

	private static Set<?> getRunningSet() throws ReflectiveOperationException {
		final Field field = TweenAnimator.class.getDeclaredField("RUNNING_SET");
		field.setAccessible(true);
		return (Set<?>) field.get(null);
	}

}