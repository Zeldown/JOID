package dev.joid.demo.ui.resource;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.video.VideoAudioPlayer;

public class UIDemoAudioTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Test
	public void forgetsItsListenerPositionOnceClosed() throws ReflectiveOperationException {
		final MsdfFont previous = DemoFont.MONTSERRAT;
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
		try {
			final UIDemoAudio ui = new UIDemoAudio();
			this.bridges.open(ui);
			Assert.assertNotNull(UIDemoAudioTest.getListenerPosition());
			ui.getData().active(false);
			Assert.assertTrue(ui.fireClose());
			Assert.assertNull(UIDemoAudioTest.getListenerPosition());
		} finally {
			DemoFont.MONTSERRAT = previous;
		}
	}

	private static Object getListenerPosition() throws ReflectiveOperationException {
		final Field field = VideoAudioPlayer.class.getDeclaredField("audioListenerPosition");
		field.setAccessible(true);
		return field.get(null);
	}

}