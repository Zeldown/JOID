package dev.joid.backend.lwjgl3.binding;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import dev.joid.backend.lwjgl3.Backend;
import dev.joid.base.opengl.render.GlRenderBridge;

public class OpenGlCallTest {

	private static final Pattern CALL = Pattern.compile("org/lwjgl/opengl/(?:GL\\d|ARB|EXT)");

	@Test
	public void callsOpenGlOnlyThroughTheBindings() throws IOException, URISyntaxException {
		final List<String> callerList = new ArrayList<>();
		for (final Class<?> type : new Class<?>[] {Backend.class, GlRenderBridge.class}) {
			final File root = new File(type.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (root.isDirectory()) {
				try (Stream<Path> files = Files.walk(root.toPath())) {
					for (final Path file : files.filter(path -> path.toString().endsWith(".class")).collect(Collectors.toList())) {
						OpenGlCallTest.check(root.toPath().relativize(file).toString().replace('\\', '/'), Files.readAllBytes(file), callerList);
					}
				}
			} else {
				try (ZipFile jar = new ZipFile(root)) {
					final Enumeration<? extends ZipEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						final ZipEntry entry = entries.nextElement();
						if (entry.getName().endsWith(".class")) {
							OpenGlCallTest.check(entry.getName(), IOUtils.toByteArray(jar.getInputStream(entry)), callerList);
						}
					}
				}
			}
		}
		Assert.assertEquals("Classes that call OpenGL outside the LWJGL 3 bindings", new ArrayList<>(), callerList);
	}

	private static void check(final String name, final byte[] bytecode, final List<String> callerList) {
		if (!name.startsWith("dev/joid/backend/lwjgl3/binding/") && OpenGlCallTest.CALL.matcher(new String(bytecode, StandardCharsets.ISO_8859_1)).find()) {
			callerList.add(name);
		}
	}

}