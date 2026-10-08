package dev.joid.lib.bridge.render.shader.uniform;

import org.junit.Assert;
import org.junit.Test;

public class UniformMemberTest {

	@Test
	public void keepsTheValuesTightlyPacked() {
		final UniformMember member = UniformMember.create("u_Matrix", UniformType.MAT3, 0, 0).value(1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F);
		Assert.assertEquals(36, member.getValues().capacity());
		Assert.assertEquals(4F, member.getValues().getFloat(12), 0F);
	}

	@Test
	public void becomesDirtyOnlyWhenItsValueChanges() {
		final UniformMember member = UniformMember.create("u_Radius", UniformType.FLOAT, 0, 0);
		Assert.assertFalse(member.value(0F).isDirty());
		Assert.assertTrue(member.value(2F).isDirty());
		Assert.assertFalse(member.clean().value(2F).isDirty());
	}

	@Test
	public void takesTheFirstElementsOfAnArray() {
		final UniformMember member = UniformMember.create("u_Colors", UniformType.VEC4, 4, 0).value(1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F);
		Assert.assertEquals(8F, member.getValues().getFloat(28), 0F);
		Assert.assertEquals(0F, member.getValues().getFloat(32), 0F);
	}

	@Test
	public void takesABooleanForAnInt() {
		Assert.assertEquals(1, UniformMember.create("u_Fill", UniformType.INT, 0, 0).value(true).getValues().getInt(0));
	}

	@Test
	public void takesAnIntForABool() {
		Assert.assertEquals(1, UniformMember.create("u_Enabled", UniformType.BOOL, 0, 0).value(1).getValues().getInt(0));
	}

	@Test
	public void refusesTooManyFloatsForAVector() {
		try {
			UniformMember.create("u_Tint", UniformType.VEC3, 0, 0).value(1F, 2F, 3F, 4F);
			Assert.fail("A vec3 must refuse four floats");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The uniform vec3 u_Tint cannot take 4 floats", expected.getMessage());
		}
	}

	@Test
	public void refusesTooManyArrayElements() {
		try {
			UniformMember.create("u_Weights", UniformType.FLOAT, 3, 0).value(1F, 2F, 3F, 4F);
			Assert.fail("A float[3] must refuse four floats");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The uniform float u_Weights[3] cannot take 4 floats", expected.getMessage());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAPartialArrayElement() {
		UniformMember.create("u_Colors", UniformType.VEC4, 4, 0).value(1F, 2F, 3F);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAMatrixOfTheWrongSize() {
		UniformMember.create("u_Transform", UniformType.MAT3, 0, 0).value(new float[16]);
	}

	@Test
	public void refusesAnIntForAFloat() {
		try {
			UniformMember.create("u_Radius", UniformType.FLOAT, 0, 0).value(4);
			Assert.fail("A float must refuse an int");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The uniform float u_Radius cannot take an int", expected.getMessage());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesFloatsForAnInt() {
		UniformMember.create("u_Mode", UniformType.INT, 0, 0).value(1F);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesABooleanForAFloat() {
		UniformMember.create("u_Radius", UniformType.FLOAT, 0, 0).value(true);
	}

}