package dev.joid.backend.lwjgl2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;

public class Lwjgl2BackendTest {

	@Test
	public void keepsTheVersionOfJOIDItWasBuiltAgainst() throws IOException {
		final String bytecode = new String(Asset.of(Lwjgl2Backend.class.getResourceAsStream("Lwjgl2Backend.class")).read(), StandardCharsets.ISO_8859_1);
		Assert.assertTrue(bytecode.contains(JOID.VERSION));
	}

}