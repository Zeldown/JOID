package dev.joid.impl.lwjgl3.binding;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL32C;

import dev.joid.impl.opengl.binding.GlConstants;

public class Lwjgl3GlBindingTest {

	@Test
	public void namesTheOpenGlConstantsLikeLwjgl() throws ReflectiveOperationException {
		for (final Field field : GlConstants.class.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			Assert.assertEquals(field.getName(), Lwjgl3GlBindingTest.find("GL_" + field.getName()), field.getInt(null));
		}
	}

	@Test
	public void sharesOneBindingPerDomain() {
		Assert.assertSame(Lwjgl3GlBinding.inst(), Lwjgl3GlBinding.inst());
		Assert.assertSame(Lwjgl3GlBinding.inst().getStateBinding(), Lwjgl3GlBinding.inst().getStateBinding());
	}

	private static int find(final String name) throws ReflectiveOperationException {
		for (final Class<?> type : new Class<?>[] {GL11C.class, GL12C.class, GL13C.class, GL14C.class, GL15C.class, GL20C.class, GL30C.class, GL31C.class, GL32C.class}) {
			for (final Field field : type.getFields()) {
				if (field.getName().equals(name)) {
					return field.getInt(null);
				}
			}
		}
		throw new NoSuchFieldException(name);
	}

}