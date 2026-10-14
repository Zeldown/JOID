package dev.joid.backend.lwjgl2.binding;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;

import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.base.opengl.binding.GlConstants;

public class Lwjgl2GlBindingTest {

	@BeforeClass
	public static void installTheNatives() {
		Lwjgl2Natives.install();
	}

	@Test
	public void namesTheOpenGlConstantsLikeLwjgl() throws ReflectiveOperationException {
		for (final Field field : GlConstants.class.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			Assert.assertEquals(field.getName(), Lwjgl2GlBindingTest.find("GL_" + field.getName()), field.getInt(null));
		}
	}

	@Test
	public void sharesOneBindingPerDomain() {
		Assert.assertSame(Lwjgl2GlBinding.inst(), Lwjgl2GlBinding.inst());
		Assert.assertSame(Lwjgl2GlBinding.inst().getStateBinding(), Lwjgl2GlBinding.inst().getStateBinding());
	}

	@Test
	public void readsNoStringWithoutACurrentContext() throws InterruptedException {
		final AtomicReference<String> version = new AtomicReference<>("unread");
		final Thread thread = new Thread(() -> version.set(Lwjgl2GlBinding.inst().getString(GlConstants.VERSION)));
		thread.start();
		thread.join();
		Assert.assertNull(version.get());
	}

	@Test
	public void refusesToPointAnAttributeAtClientMemory() {
		try {
			Lwjgl2GlBufferBinding.create().vertexAttribPointer(2, 4, GlConstants.UNSIGNED_BYTE, true, 32, 20L);
			Assert.fail("A pointer without array buffer must be refused");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("LWJGL 2 cannot point the vertex attribute 2 at client memory (offset 20), bind an array buffer with bindBuffer(ARRAY_BUFFER, buffer) before vertexAttribPointer", expected.getMessage());
		}
	}

	private static int find(final String name) throws ReflectiveOperationException {
		for (final Class<?> type : new Class<?>[] {GL11.class, GL12.class, GL13.class, GL14.class, GL15.class, GL20.class, GL21.class, GL30.class, GL31.class, GL32.class, GL33.class}) {
			for (final Field field : type.getFields()) {
				if (field.getName().equals(name)) {
					return field.getInt(null);
				}
			}
		}
		throw new NoSuchFieldException(name);
	}

}