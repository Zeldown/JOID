package dev.joid.test.shader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;

public class CoreShadersTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void parsesCoreShaders() {
		Assert.assertFalse(CoreShaders.getNames().isEmpty());
		for (final String name : CoreShaders.getNames()) {
			Assert.assertTrue(name, CoreShaders.read(name, ShaderStage.VERTEX).getBuiltins().contains(ShaderBuiltin.POSITION));
			Assert.assertTrue(name, CoreShaders.read(name, ShaderStage.FRAGMENT).getBuiltins().contains(ShaderBuiltin.FRAGMENT_COLOR));
		}
	}

	@Test
	public void namesTheCoreShadersInOrder() {
		final List<String> names = CoreShaders.getNames();
		final List<String> sorted = new ArrayList<>(names);
		sorted.sort(null);
		Assert.assertEquals(sorted, names);
		Assert.assertTrue(names.contains("rounded"));
		for (final String name : names) {
			Assert.assertFalse(name, name.contains("/"));
		}
	}

	@Test
	public void listsTheShaderFoldersOfADirectory() throws IOException, ReflectiveOperationException {
		final File shaders = this.folder.newFolder("assets", "shaders");
		Assert.assertTrue(new File(shaders, "wave").mkdir());
		Assert.assertTrue(new File(shaders, "glow").mkdir());
		Assert.assertTrue(new File(shaders, "notes.txt").createNewFile());
		try (URLClassLoader loader = new URLClassLoader(new URL[] {this.folder.getRoot().toURI().toURL(), CoreShaders.class.getProtectionDomain().getCodeSource().getLocation(), ShaderStage.class.getProtectionDomain().getCodeSource().getLocation()}, null)) {
			Assert.assertEquals(Arrays.asList("glow", "wave"), loader.loadClass(CoreShaders.class.getName()).getMethod("getNames").invoke(null));
		}
	}

}