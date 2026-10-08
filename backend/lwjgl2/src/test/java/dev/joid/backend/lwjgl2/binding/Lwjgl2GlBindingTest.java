package dev.joid.backend.lwjgl2.binding;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Assert;
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

import dev.joid.base.opengl.binding.GlConstants;

public class Lwjgl2GlBindingTest {

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