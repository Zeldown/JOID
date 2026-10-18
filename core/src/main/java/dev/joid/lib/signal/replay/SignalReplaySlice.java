package dev.joid.lib.signal.replay;

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
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import lombok.Getter;

@Getter
public final class SignalReplaySlice {

	private final int                end;
	private final int                start;
	private final Type               type;
	private final String             signature;
	private final SignalReplayMethod method;
	private final List<String>       signalNameList;

	private int signalCount;

	private SignalReplaySlice(final SignalReplayMethod method, final int start, final int end, final Type type) {
		this.method         = method;
		this.start          = start;
		this.end            = end;
		this.type           = type;
		this.signalNameList = new ArrayList<>();
		this.signature      = this.describe();
	}

	public static SignalReplaySlice of(final SignalReplayMethod method, final int start, final int end, final Type type) {
		return new SignalReplaySlice(method, start, end, type);
	}

	public boolean hasLambda() {
		final AbstractInsnNode[] instructions = this.method.getInstructions();
		for (int index = this.start; index < this.end; index++) {
			if (instructions[index] instanceof InvokeDynamicInsnNode && ((InvokeDynamicInsnNode) instructions[index]).bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
				return true;
			}
		}
		return false;
	}

	public boolean isEnclosed() {
		final AbstractInsnNode[] instructions = this.method.getInstructions();
		for (int index = this.end + 1; index < instructions.length; index++) {
			final Frame<SourceValue> frame = this.method.getFrames()[index];
			if (frame == null) {
				continue;
			}

			if (frame.getStackSize() == 0) {
				return false;
			}

			if (!(instructions[index] instanceof MethodInsnNode)) {
				continue;
			}

			final int argumentCount = Type.getArgumentTypes(((MethodInsnNode) instructions[index]).desc).length;
			int end = index;
			for (int argument = argumentCount - 1; argument >= 0; argument--) {
				final SourceValue value = frame.getStack(frame.getStackSize() - argumentCount + argument);
				final int start = this.method.expressionStart(value.insns, end);
				if (start <= this.start && this.end < end && this.readsSignal(start, this.start)) {
					return true;
				}
				end = start;
			}
		}
		return false;
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

	public boolean isEquivalent(final SignalReplaySlice slice) {
		return Objects.equals(this.type, slice.type) && this.signature.equals(slice.signature);
	}

	public Object convert(final Object value) {
		return this.type != null && value instanceof Integer ? SignalReplayFrame.convert(value, this.method.type(this.type)) : value;
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

	private boolean readsSignal(final int start, final int end) {
		final AbstractInsnNode[] instructions = this.method.getInstructions();
		for (int index = start; index < end; index++) {
			if (instructions[index] instanceof MethodInsnNode && this.method.isSignal(((MethodInsnNode) instructions[index]).owner)) {
				return true;
			}
		}
		return false;
	}

}