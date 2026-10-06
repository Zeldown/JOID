package dev.joid.lib.utils.signal.replay;

import java.io.IOException;
import java.io.InputStream;
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
public final class ReplayClass {

	private final ClassNode   node;
	private final ClassLoader loader;

	private final Map<MethodNode, ReplayMethod> methodMap = new ConcurrentHashMap<>();

	public static ReplayClass read(final String name, final ClassLoader loader) {
		final String path = name.replace('.', '/') + ".class";
		for (final ClassLoader candidate : new ClassLoader[] {loader, Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader()}) {
			if (candidate == null) {
				continue;
			}

			try (InputStream input = candidate.getResourceAsStream(path)) {
				if (input == null) {
					continue;
				}

				final ClassNode node = new ClassNode();
				new ClassReader(ReplayClass.bytes(input)).accept(node, ClassReader.SKIP_FRAMES);
				return new ReplayClass(node, candidate);
			} catch (final IOException | RuntimeException exception) {
				continue;
			}
		}

		throw new ReplayException(ReplayFailure.CLASS_NOT_FOUND, name);
	}

	public ReplayMethod method(final MethodNode method) {
		return this.methodMap.computeIfAbsent(method, key -> {
			try {
				return ReplayMethod.create(this, key);
			} catch (final AnalyzerException exception) {
				throw new ReplayException(ReplayFailure.CLASS_NOT_FOUND, this.node.name.replace('/', '.'));
			}
		});
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