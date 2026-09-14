package be.zeldown.joid.test;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;

public class CoreShadersTest {

	@Test
	public void parsesCoreShaders() {
		Assert.assertFalse(CoreShaders.getNames().isEmpty());
		for (final String name : CoreShaders.getNames()) {
			Assert.assertTrue(name, CoreShaders.read(name, ShaderStage.VERTEX).getBuiltins().contains(ShaderBuiltin.POSITION));
			Assert.assertTrue(name, CoreShaders.read(name, ShaderStage.FRAGMENT).getBuiltins().contains(ShaderBuiltin.FRAGMENT_COLOR));
		}
	}

}