package dev.joid.backend.lwjgl2;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;

public class Lwjgl2BackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(JOID.VERSION, Lwjgl2Backend.create().getVersion());
	}

}