package dev.joid.backend.lwjgl2;

import org.junit.Assert;
import org.junit.Test;

public class Lwjgl2BackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), Lwjgl2Backend.create().getVersion());
	}

}