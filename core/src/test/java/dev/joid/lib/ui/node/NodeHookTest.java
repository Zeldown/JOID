package dev.joid.lib.ui.node;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class NodeHookTest {

	@Test
	public void letsEveryBuiltInNodeRedefineItsLifecycleAndEventMethods() throws Exception {
		final Set<String> hooks = new HashSet<>();
		for (final Method method : INode.class.getDeclaredMethods()) {
			hooks.add(method.getName());
		}
		hooks.add("drawField");

		final Path root = Paths.get(Node.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final List<Path> files;
		try (final Stream<Path> stream = Files.walk(root.resolve("dev/joid"))) {
			files = stream.filter(path -> path.toString().endsWith(".class")).collect(Collectors.toList());
		}

		final List<String> finals = new ArrayList<>();
		int nodes = 0;
		for (final Path file : files) {
			final String name = root.relativize(file).toString().replace(File.separatorChar, '.').replaceAll("\\.class$", "");
			final Class<?> clazz = Class.forName(name, false, Node.class.getClassLoader());
			if (!Node.class.isAssignableFrom(clazz)) {
				continue;
			}

			nodes++;
			for (final Method method : clazz.getDeclaredMethods()) {
				if (hooks.contains(method.getName()) && Modifier.isFinal(method.getModifiers())) {
					finals.add(clazz.getSimpleName() + "." + method.getName());
				}
			}
		}

		Assert.assertTrue(String.valueOf(nodes), nodes > 40);
		Assert.assertEquals(Collections.emptyList(), finals);
	}

}