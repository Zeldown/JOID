package dev.joid.lib.signal.replay;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.signal.ISignalReplayRemapper;
import dev.joid.lib.signal.ISignal;
import lombok.Getter;

@Getter
public final class SignalReplayMethod {

	private final boolean              isStatic;
	private final MethodNode           method;
	private final Frame<SourceValue>[] frames;
	private final SignalReplayClass    replayClass;
	private final AbstractInsnNode[]   instructions;

	private final Map<String, Class<?>>                    classMap;
	private final Map<AbstractInsnNode, Object>            memberMap;
	private final Map<AbstractInsnNode, SignalReplaySlice> definitionMap;

	private SignalReplayMethod(final SignalReplayClass replayClass, final MethodNode method) throws AnalyzerException {
		this.replayClass   = replayClass;
		this.method        = method;
		this.isStatic      = (method.access & Opcodes.ACC_STATIC) != 0;
		this.frames        = new Analyzer<>(new SourceInterpreter()).analyze(replayClass.getNode().name, method);
		this.instructions  = method.instructions.toArray();
		this.memberMap     = new ConcurrentHashMap<>();
		this.classMap      = new ConcurrentHashMap<>();
		this.definitionMap = new IdentityHashMap<>();
		this.define();
	}

	public static SignalReplayMethod create(final SignalReplayClass replayClass, final MethodNode method) throws AnalyzerException {
		return new SignalReplayMethod(replayClass, method);
	}

	public int indexOf(final AbstractInsnNode instruction) {
		return this.method.instructions.indexOf(instruction);
	}

	public int expressionStart(final Collection<AbstractInsnNode> sourceList, final int end) {
		final Set<AbstractInsnNode> closure = Collections.newSetFromMap(new IdentityHashMap<>());
		final Deque<AbstractInsnNode> work = new ArrayDeque<>(sourceList);
		int start = end;
		boolean changed = true;
		while (changed) {
			while (!work.isEmpty()) {
				final AbstractInsnNode instruction = work.pop();
				if (!closure.add(instruction)) {
					continue;
				}

				final int index = this.indexOf(instruction);
				start = Math.min(start, index);
				final Frame<SourceValue> before = this.frames[index];
				if (before == null) {
					continue;
				}

				final int consumed = SignalReplayMethod.popCount(instruction, before);
				for (int depth = 0; depth < consumed && depth < before.getStackSize(); depth++) {
					work.addAll(before.getStack(before.getStackSize() - 1 - depth).insns);
				}
			}

			changed = false;
			for (int index = 0; index < start; index++) {
				final AbstractInsnNode instruction = this.instructions[index];
				if (instruction instanceof JumpInsnNode && !closure.contains(instruction)) {
					final int target = this.indexOf(((JumpInsnNode) instruction).label);
					if (target > start && target <= end) {
						work.add(instruction);
						changed = true;
					}
				}
			}
		}
		return start;
	}

	public String localName(final int variable, final int index) {
		if (this.method.localVariables == null) {
			return null;
		}

		for (final LocalVariableNode local : this.method.localVariables) {
			if (local.index == variable && this.indexOf(local.start) <= index && index < this.indexOf(local.end)) {
				return local.name;
			}
		}
		return null;
	}

	public String describeLocal(final int variable, final int index) {
		if (variable == 0 && !this.isStatic) {
			return "this";
		}

		final String name = this.localName(variable, index);
		return name == null ? "a local variable" : "the local variable " + name;
	}

	public String signalName(final int index) {
		final AbstractInsnNode instruction = this.instructions[index];
		final Frame<SourceValue> frame = this.frames[index];
		if (!(instruction instanceof MethodInsnNode) || frame == null || instruction.getOpcode() == Opcodes.INVOKESTATIC) {
			return null;
		}

		final int argumentCount = Type.getArgumentTypes(((MethodInsnNode) instruction).desc).length;
		final SourceValue receiver = frame.getStack(frame.getStackSize() - 1 - argumentCount);
		if (receiver.insns.size() != 1) {
			return null;
		}

		final AbstractInsnNode source = receiver.insns.iterator().next();
		if (source instanceof FieldInsnNode) {
			return ((FieldInsnNode) source).name;
		}

		if (source instanceof VarInsnNode) {
			return this.localName(((VarInsnNode) source).var, this.indexOf(source));
		}

		if (source instanceof MethodInsnNode) {
			return ((MethodInsnNode) source).name + "()";
		}
		return null;
	}

	public boolean isSignal(final String owner) {
		try {
			return ISignal.class.isAssignableFrom(this.type(Type.getObjectType(owner)));
		} catch (final SignalReplayException exception) {
			return false;
		}
	}

	public boolean isParameter(final int variable) {
		return variable >= (this.isStatic ? 0 : 1) && variable < (Type.getArgumentsAndReturnSizes(this.method.desc) >> 2) - (this.isStatic ? 1 : 0);
	}

	public Object member(final AbstractInsnNode instruction) {
		Object member = this.memberMap.get(instruction);
		if (member == null) {
			member = this.resolve(instruction);
			this.memberMap.put(instruction, member);
		}
		return member;
	}

	public Class<?> type(final Type type) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return boolean.class;
			case Type.CHAR:
				return char.class;
			case Type.BYTE:
				return byte.class;
			case Type.SHORT:
				return short.class;
			case Type.INT:
				return int.class;
			case Type.LONG:
				return long.class;
			case Type.FLOAT:
				return float.class;
			case Type.DOUBLE:
				return double.class;
			case Type.VOID:
				return void.class;
			case Type.ARRAY:
				Class<?> array = this.type(type.getElementType());
				for (int dimension = 0; dimension < type.getDimensions(); dimension++) {
					array = Array.newInstance(array, 0).getClass();
				}
				return array;
			default:
				return this.load(type.getInternalName());
		}
	}

	private Class<?> load(final String internalName) {
		Class<?> loaded = this.classMap.get(internalName);
		if (loaded == null) {
			final String name = BridgeHandler.SIGNAL_REPLAY.get().mapClass(internalName).replace('/', '.');
			try {
				loaded = Class.forName(name, false, this.replayClass.getLoader());
			} catch (final ClassNotFoundException | LinkageError exception) {
				throw new SignalReplayException(SignalReplayFailure.MEMBER_NOT_FOUND, "the class " + name);
			}
			this.classMap.put(internalName, loaded);
		}
		return loaded;
	}

	private Object resolve(final AbstractInsnNode instruction) {
		final ISignalReplayRemapper remapper = BridgeHandler.SIGNAL_REPLAY.get();
		if (instruction instanceof FieldInsnNode) {
			final FieldInsnNode field = (FieldInsnNode) instruction;
			final Class<?> owner = this.type(Type.getObjectType(field.owner));
			final String name = remapper.mapField(field.owner, field.name, field.desc);
			for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
				try {
					return this.access(type.getDeclaredField(name));
				} catch (final NoSuchFieldException exception) {
					continue;
				}
			}

			try {
				return this.access(owner.getField(name));
			} catch (final NoSuchFieldException exception) {
				throw new SignalReplayException(SignalReplayFailure.MEMBER_NOT_FOUND, "the field " + owner.getName() + "." + name);
			}
		}

		final MethodInsnNode call = (MethodInsnNode) instruction;
		final Type[] argumentTypes = Type.getArgumentTypes(call.desc);
		final Class<?>[] parameterTypes = new Class<?>[argumentTypes.length];
		for (int index = 0; index < parameterTypes.length; index++) {
			parameterTypes[index] = this.type(argumentTypes[index]);
		}

		final Class<?> owner = this.type(Type.getObjectType(call.owner));
		final String name = remapper.mapMethod(call.owner, call.name, call.desc);
		if (call.name.equals("<init>")) {
			try {
				return this.access(owner.getDeclaredConstructor(parameterTypes));
			} catch (final NoSuchMethodException exception) {
				throw new SignalReplayException(SignalReplayFailure.MEMBER_NOT_FOUND, "a constructor of " + owner.getName());
			}
		}

		for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
			try {
				return this.access(type.getDeclaredMethod(name, parameterTypes));
			} catch (final NoSuchMethodException exception) {
				continue;
			}
		}

		try {
			return this.access(owner.getMethod(name, parameterTypes));
		} catch (final NoSuchMethodException exception) {
			throw new SignalReplayException(SignalReplayFailure.MEMBER_NOT_FOUND, "the method " + owner.getName() + "." + name + "(...)");
		}
	}

	private <T extends AccessibleObject & Member> T access(final T member) {
		if (Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
			return member;
		}

		try {
			member.setAccessible(true);
		} catch (final RuntimeException exception) {
			final String name = member.getDeclaringClass().getName();
			throw new SignalReplayException(SignalReplayFailure.ACCESS_REFUSED, name + "." + member.getName(), name.substring(0, Math.max(0, name.lastIndexOf('.'))));
		}
		return member;
	}

	private void define() {
		for (int index = 0; index < this.instructions.length; index++) {
			final AbstractInsnNode instruction = this.instructions[index];
			final int opcode = instruction.getOpcode();
			if (opcode < Opcodes.ILOAD || opcode > Opcodes.ALOAD || this.frames[index] == null || !this.isStatic && ((VarInsnNode) instruction).var == 0) {
				continue;
			}

			final SourceValue local = this.frames[index].getLocal(((VarInsnNode) instruction).var);
			if (local.insns.size() != 1) {
				continue;
			}

			final AbstractInsnNode store = local.insns.iterator().next();
			final int end = this.indexOf(store);
			if (store.getOpcode() < Opcodes.ISTORE || store.getOpcode() > Opcodes.ASTORE || this.isInLoop(end)) {
				continue;
			}

			final Frame<SourceValue> frame = this.frames[end];
			final int start = this.expressionStart(frame.getStack(frame.getStackSize() - 1).insns, end);
			if (store.getOpcode() != Opcodes.ASTORE || this.isValue(((VarInsnNode) instruction).var, index) || this.isPure(start, end)) {
				this.definitionMap.put(instruction, SignalReplaySlice.create(this, start, end, null));
			}
		}
	}

	private boolean isInLoop(final int index) {
		for (int next = index + 1; next < this.instructions.length; next++) {
			final AbstractInsnNode instruction = this.instructions[next];
			if (instruction instanceof JumpInsnNode && this.indexOf(((JumpInsnNode) instruction).label) <= index) {
				return true;
			}
		}
		return false;
	}

	private boolean isPure(final int start, final int end) {
		for (int index = start; index < end; index++) {
			final AbstractInsnNode instruction = this.instructions[index];
			if (instruction instanceof MethodInsnNode) {
				final String owner = ((MethodInsnNode) instruction).owner;
				final Type returnType = Type.getReturnType(((MethodInsnNode) instruction).desc);
				if (!owner.startsWith("java/") && !this.isSignal(owner) || returnType.getSort() == Type.OBJECT && this.isSignal(returnType.getInternalName())) {
					return false;
				}
			} else if (instruction.getOpcode() == Opcodes.NEW) {
				if (!((TypeInsnNode) instruction).desc.startsWith("java/")) {
					return false;
				}
			} else if (instruction instanceof InvokeDynamicInsnNode) {
				if (!((InvokeDynamicInsnNode) instruction).bsm.getOwner().equals("java/lang/invoke/StringConcatFactory")) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isValue(final int variable, final int index) {
		if (this.method.localVariables == null) {
			return false;
		}

		for (final LocalVariableNode local : this.method.localVariables) {
			if (local.index == variable && this.indexOf(local.start) <= index && index < this.indexOf(local.end)) {
				final Type type = Type.getType(local.desc);
				if (type.getSort() != Type.OBJECT) {
					return false;
				}

				try {
					final Class<?> loaded = this.type(type);
					return loaded == String.class || loaded.isEnum() || Number.class.isAssignableFrom(loaded) && loaded.getName().startsWith("java.lang.") || loaded == Boolean.class || loaded == Character.class;
				} catch (final SignalReplayException exception) {
					return false;
				}
			}
		}
		return false;
	}

	private static int popCount(final AbstractInsnNode instruction, final Frame<SourceValue> before) {
		final int opcode = instruction.getOpcode();
		if (instruction instanceof MethodInsnNode) {
			return Type.getArgumentTypes(((MethodInsnNode) instruction).desc).length + (opcode == Opcodes.INVOKESTATIC ? 0 : 1);
		}

		if (instruction instanceof InvokeDynamicInsnNode) {
			return Type.getArgumentTypes(((InvokeDynamicInsnNode) instruction).desc).length;
		}

		if (instruction instanceof MultiANewArrayInsnNode) {
			return ((MultiANewArrayInsnNode) instruction).dims;
		}

		if (opcode >= Opcodes.POP2 && opcode <= Opcodes.DUP2_X2) {
			final int slots = opcode == Opcodes.DUP ? 1 : opcode == Opcodes.POP2 || opcode == Opcodes.DUP_X1 || opcode == Opcodes.DUP2 ? 2 : opcode == Opcodes.DUP2_X2 ? 4 : 3;
			int count = 0;
			for (int covered = 0; covered < slots && count < before.getStackSize(); count++) {
				covered += before.getStack(before.getStackSize() - 1 - count).getSize();
			}
			return count;
		}

		if (opcode >= Opcodes.IASTORE && opcode <= Opcodes.SASTORE) {
			return 3;
		}

		if (opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD || opcode == Opcodes.SWAP || opcode == Opcodes.PUTFIELD || opcode >= Opcodes.IADD && opcode <= Opcodes.DREM || opcode >= Opcodes.ISHL && opcode <= Opcodes.LXOR || opcode >= Opcodes.LCMP && opcode <= Opcodes.DCMPG || opcode >= Opcodes.IF_ICMPEQ && opcode <= Opcodes.IF_ACMPNE) {
			return 2;
		}

		if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE || opcode == Opcodes.POP || opcode >= Opcodes.INEG && opcode <= Opcodes.DNEG || opcode >= Opcodes.I2L && opcode <= Opcodes.I2S || opcode >= Opcodes.IFEQ && opcode <= Opcodes.IFLE || opcode >= Opcodes.TABLESWITCH && opcode <= Opcodes.ARETURN) {
			return 1;
		}
		return opcode == Opcodes.PUTSTATIC || opcode == Opcodes.GETFIELD || opcode >= Opcodes.NEWARRAY && opcode <= Opcodes.MONITOREXIT || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL ? 1 : 0;
	}

}