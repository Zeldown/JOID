package dev.joid.lib.utils.signal.replay;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplayClass {

	private final File        file;
	private final long        modified;
	private final ClassNode   node;
	private final ClassLoader loader;

	private final Map<MethodNode, SignalReplayMethod> methodMap = new ConcurrentHashMap<>();

	public static SignalReplayClass read(final String name, final Class<?> type, final ClassLoader loader) {
		final String path = name.replace('.', '/') + ".class";
		final File file = SignalReplayClass.locate(type, path);
		if (file != null) {
			final long modified = file.lastModified();
			try (InputStream input = new FileInputStream(file)) {
				return new SignalReplayClass(file, modified, SignalReplayClass.parse(input), loader);
			} catch (final IOException | RuntimeException exception) {
				return SignalReplayClass.read(name, path, loader);
			}
		}
		return SignalReplayClass.read(name, path, loader);
	}

	public boolean isStale() {
		return this.file != null && this.file.lastModified() != this.modified;
	}

	public SignalReplayMethod method(final MethodNode method) {
		return this.methodMap.computeIfAbsent(method, key -> {
			try {
				return SignalReplayMethod.create(this, key);
			} catch (final AnalyzerException exception) {
				throw new SignalReplayException(SignalReplayFailure.CLASS_NOT_FOUND, this.node.name.replace('/', '.'));
			}
		});
	}

	private static SignalReplayClass read(final String name, final String path, final ClassLoader loader) {
		for (final ClassLoader candidate : new ClassLoader[] {loader, Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader()}) {
			if (candidate == null) {
				continue;
			}

			try (InputStream input = candidate.getResourceAsStream(path)) {
				if (input == null) {
					continue;
				}
				return new SignalReplayClass(null, 0L, SignalReplayClass.parse(input), candidate);
			} catch (final IOException | RuntimeException exception) {
				continue;
			}
		}

		throw new SignalReplayException(SignalReplayFailure.CLASS_NOT_FOUND, name);
	}

	private static File locate(final Class<?> type, final String path) {
		if (type == null) {
			return null;
		}

		try {
			final ProtectionDomain domain = type.getProtectionDomain();
			final CodeSource source = domain != null ? domain.getCodeSource() : null;
			final URL location = source != null ? source.getLocation() : null;
			if (location == null || !"file".equals(location.getProtocol())) {
				return null;
			}

			final File file = new File(new File(location.toURI()), path);
			return file.isFile() ? file : null;
		} catch (final URISyntaxException | RuntimeException exception) {
			return null;
		}
	}

	private static ClassNode parse(final InputStream input) throws IOException {
		final ClassNode node = new ClassNode();
		new ClassReader(SignalReplayClass.bytes(input)).accept(node, ClassReader.SKIP_FRAMES);
		return node;
	}

	private static byte[] bytes(final InputStream input) throws IOException {
		final byte[] bytes = IOUtils.toByteArray(input);
		if (bytes.length > 7 && ((bytes[6] & 0xFF) << 8 | bytes[7] & 0xFF) > Opcodes.V18) {
			bytes[6] = (byte) (Opcodes.V18 >>> 8);
			bytes[7] = (byte) Opcodes.V18;
		}
		return bytes;
	}

}