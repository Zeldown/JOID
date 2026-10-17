package dev.joid.lib.font;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

public class FontBoundsTest {

	@Test
	public void startsEmpty() {
		final FontBounds bounds = FontBounds.empty();
		Assert.assertEquals(0D, bounds.getWidth(), 0D);
		Assert.assertEquals(0D, bounds.getHeight(), 0D);
	}

	@Test
	public void keepsItsSize() {
		final FontBounds bounds = FontBounds.of(3D, 4D);
		Assert.assertEquals(3D, bounds.getWidth(), 0D);
		Assert.assertEquals(4D, bounds.getHeight(), 0D);
	}

	@Test
	public void isCreatedThroughItsFactories() {
		for (final Constructor<?> constructor : FontBounds.class.getDeclaredConstructors()) {
			Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		}
	}

}