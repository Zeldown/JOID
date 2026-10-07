package dev.joid.lib.utils.signal.replay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import lombok.Getter;

@Getter
public final class ReplaySlice {

	private final int          end;
	private final int          start;
	private final Type         type;
	private final String       signature;
	private final ReplayMethod method;
	private final List<String> signalNameList;

	private int signalCount;

	private ReplaySlice(final ReplayMethod method, final int start, final int end, final Type type) {
		this.method         = method;
		this.start          = start;
		this.end            = end;
		this.type           = type;
		this.signalNameList = new ArrayList<>();
		this.signature      = this.describe();
	}

	public static ReplaySlice create(final ReplayMethod method, final int start, final int end, final Type type) {
		return new ReplaySlice(method, start, end, type);
	}

	public boolean isEquivalent(final ReplaySlice slice) {
		return Objects.equals(this.type, slice.type) && this.signature.equals(slice.signature);
	}

	public boolean isParameter() {
		final AbstractInsnNode[] instructions = this.method.getInstructions();
		boolean loaded = false;
		for (int index = this.start; index < this.end; index++) {
			final AbstractInsnNode instruction = instructions[index];
			if (instruction.getOpcode() < 0 || instruction.getOpcode() == Opcodes.CHECKCAST) {
				continue;
			}

			if (!loaded && instruction instanceof VarInsnNode && instruction.getOpcode() <= Opcodes.ALOAD && this.method.isParameter(((VarInsnNode) instruction).var)) {
				loaded = true;
			} else if (!loaded || instruction.getOpcode() != Opcodes.INVOKESTATIC || !((MethodInsnNode) instruction).name.equals("valueOf")) {
				return false;
			}
		}
		return loaded;
	}

	public Object convert(final Object value) {
		if (this.type == null || !(value instanceof Integer)) {
			return value;
		}

		final int number = (Integer) value;
		switch (this.type.getSort()) {
			case Type.BOOLEAN:
				return number != 0;
			case Type.CHAR:
				return (char) number;
			case Type.BYTE:
				return (byte) number;
			case Type.SHORT:
				return (short) number;
			default:
				return value;
		}
	}

	private String describe() {
		final StringBuilder builder = new StringBuilder();
		final AbstractInsnNode[] instructions = this.method.getInstructions();
		for (int index = this.start; index < this.end; index++) {
			final AbstractInsnNode instruction = instructions[index];
			if (instruction.getOpcode() < 0) {
				continue;
			}

			builder.append(instruction.getOpcode()).append(' ');
			if (instruction instanceof FieldInsnNode) {
				builder.append(((FieldInsnNode) instruction).owner).append('.').append(((FieldInsnNode) instruction).name);
			} else if (instruction instanceof MethodInsnNode) {
				final MethodInsnNode call = (MethodInsnNode) instruction;
				builder.append(call.owner).append('.').append(call.name).append(call.desc);
				if (this.method.isSignal(call.owner)) {
					final String name = this.method.signalName(index);
					if (name != null && !this.signalNameList.contains(name)) {
						this.signalNameList.add(name);
					}
					this.signalCount++;
				}
			} else if (instruction instanceof InvokeDynamicInsnNode) {
				builder.append(((InvokeDynamicInsnNode) instruction).name).append(((InvokeDynamicInsnNode) instruction).desc);
			} else if (instruction instanceof LdcInsnNode) {
				builder.append(((LdcInsnNode) instruction).cst);
			} else if (instruction instanceof IntInsnNode) {
				builder.append(((IntInsnNode) instruction).operand);
			} else if (instruction instanceof VarInsnNode) {
				builder.append(((VarInsnNode) instruction).var);
			} else if (instruction instanceof IincInsnNode) {
				builder.append(((IincInsnNode) instruction).var).append(' ').append(((IincInsnNode) instruction).incr);
			} else if (instruction instanceof TypeInsnNode) {
				builder.append(((TypeInsnNode) instruction).desc);
			} else if (instruction instanceof JumpInsnNode) {
				builder.append(this.method.indexOf(((JumpInsnNode) instruction).label) - index);
			}
			builder.append(';');
		}
		return builder.toString();
	}

}