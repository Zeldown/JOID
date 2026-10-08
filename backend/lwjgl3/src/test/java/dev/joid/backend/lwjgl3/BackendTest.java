package dev.joid.backend.lwjgl3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;

public class BackendTest {

	@Test
	public void checksTheVersionOfJOIDItWasBuiltAgainst() throws IOException {
		final String bytecode = new String(Asset.of(Backend.class.getResourceAsStream("Backend.class")).read(), StandardCharsets.ISO_8859_1);
		Assert.assertTrue(bytecode.contains("checkVersion"));
		Assert.assertTrue(bytecode.contains(JOID.VERSION));
	}

}