package dev.joid.lib.utils.signal.replay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import dev.joid.demo.replay.ReplayHiddenFixture;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public final class ReplayHiddenLoader extends ClassLoader {

	private final String hiddenName;
	private final byte[] defined;
	private final byte[] served;

	private ReplayHiddenLoader(final String name, final byte[] defined, final byte[] served) {
		super(ReplayHiddenLoader.class.getClassLoader());
		this.hiddenName = name;
		this.defined    = defined;
		this.served     = served;
	}

	public static ReplayHiddenLoader create(final String simpleName, final boolean lines, final boolean served, final String renamed, final String replacement) throws IOException {
		final String name = "dev.joid.demo.replay." + simpleName;
		final byte[] defined = ReplayHiddenLoader.transform(name, lines, null, null);
		return new ReplayHiddenLoader(name, defined, served ? ReplayHiddenLoader.transform(name, lines, renamed, replacement) : null);
	}

	public void run(final ReplayNode node, final IntegerSignal clicks) {
		try {
			super.loadClass(this.hiddenName).getMethod("run", ReplayNode.class, IntegerSignal.class).invoke(null, node, clicks);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	@Override
	public InputStream getResourceAsStream(final String path) {
		if (this.served != null && path.equals(this.hiddenName.replace('.', '/') + ".class")) {
			return new ByteArrayInputStream(this.served);
		}
		return super.getResourceAsStream(path);
	}

	@Override
	protected Class<?> findClass(final String className) throws ClassNotFoundException {
		if (className.equals(this.hiddenName)) {
			return super.defineClass(className, this.defined, 0, this.defined.length);
		}
		return super.findClass(className);
	}

	private static byte[] transform(final String name, final boolean lines, final String renamed, final String replacement) throws IOException {
		final byte[] original;
		try (InputStream input = ReplayHiddenFixture.class.getResourceAsStream("ReplayHiddenFixture.class")) {
			original = IOUtils.toByteArray(input);
		}
		original[6] = 0;
		original[7] = (byte) Math.min(original[7] & 0xFF, Opcodes.V1_8);

		final ClassWriter writer = new ClassWriter(0);
		new ClassReader(original).accept(new ClassVisitor(Opcodes.ASM9, writer) {

			@Override
			public void visit(final int version, final int access, final String className, final String signature, final String superName, final String[] interfaces) {
				super.visit(version, access, name.replace('.', '/'), signature, superName, interfaces);
			}

			@Override
			public MethodVisitor visitMethod(final int access, final String methodName, final String descriptor, final String signature, final String[] exceptions) {
				return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, methodName, descriptor, signature, exceptions)) {

					@Override
					public void visitLineNumber(final int line, final Label start) {
						if (lines) {
							super.visitLineNumber(line, start);
						}
					}

					@Override
					public void visitMethodInsn(final int opcode, final String owner, final String calledName, final String calledDescriptor, final boolean isInterface) {
						super.visitMethodInsn(opcode, owner, calledName.equals(renamed) ? replacement : calledName, calledDescriptor, isInterface);
					}

				};
			}

		}, 0);
		return writer.toByteArray();
	}

}