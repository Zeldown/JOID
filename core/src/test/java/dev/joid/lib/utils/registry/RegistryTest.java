package dev.joid.lib.utils.registry;

import org.junit.Assert;
import org.junit.Test;

public class RegistryTest {

	@Test
	public void findsNothingWhileEmpty() {
		Assert.assertNull(Registry.<String>create().find(value -> true));
	}

	@Test
	public void asksTheLastRegisteredValueFirst() {
		final Registry<String> registry = Registry.create();
		registry.register("first");
		registry.register("second");
		Assert.assertEquals("second", registry.find(value -> true));
		Assert.assertEquals("first", registry.find(value -> value.startsWith("f")));
	}

	@Test
	public void movesAValueRegisteredAgainToTheFront() {
		final Registry<String> registry = Registry.create();
		registry.register("first");
		registry.register("second");
		registry.register("first");
		Assert.assertEquals("first", registry.find(value -> true));
	}

	@Test
	public void forgetsAnUnregisteredValue() {
		final Registry<String> registry = Registry.create();
		registry.register("first");
		registry.unregister("first");
		Assert.assertNull(registry.find(value -> true));
	}

}