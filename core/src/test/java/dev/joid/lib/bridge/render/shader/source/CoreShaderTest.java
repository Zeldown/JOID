package dev.joid.lib.bridge.render.shader.source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class CoreShaderTest {

	@Test
	public void parsesEveryCoreShader() {
		for (final CoreShader shader : CoreShader.values()) {
			Assert.assertTrue(shader.name(), shader.read(ShaderStage.VERTEX).getBuiltins().contains(ShaderBuiltin.POSITION));
			Assert.assertTrue(shader.name(), shader.read(ShaderStage.FRAGMENT).getBuiltins().contains(ShaderBuiltin.FRAGMENT_COLOR));
		}
	}

	@Test
	public void readsTheStageItIsAskedFor() {
		Assert.assertSame(ShaderStage.VERTEX, CoreShader.DEFAULT.read(ShaderStage.VERTEX).getStage());
		Assert.assertSame(ShaderStage.FRAGMENT, CoreShader.DEFAULT.read(ShaderStage.FRAGMENT).getStage());
	}

	@Test
	public void namesEveryShaderFolder() throws IOException, URISyntaxException {
		try (Stream<Path> paths = Files.list(Paths.get(CoreShader.class.getResource("/assets/shaders").toURI()))) {
			final List<String> folders = paths.filter(Files::isDirectory).map(path -> path.getFileName().toString()).sorted().collect(Collectors.toList());
			Assert.assertEquals(folders, Arrays.stream(CoreShader.values()).map(shader -> shader.name().toLowerCase(Locale.ROOT)).sorted().collect(Collectors.toList()));
		}
	}

}