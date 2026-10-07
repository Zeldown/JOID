package dev.joid.lib.utils.signal.replay;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import dev.joid.lib.utils.signal.SignalContext;
import lombok.Getter;

@Getter
public final class ReplayFrame {

	private static final Map<Integer, String> OPCODE_MAP = new HashMap<>();

	static {
		for (final Field field : Opcodes.class.getFields()) {
			final String name = field.getName();
			if (field.getType() == int.class && name.matches("[A-Z0-9_]+") && !name.matches("(ACC|ASM|V|T|H|F|SOURCE)_.*|V\\d.*|ASM\\d.*")) {
				try {
					ReplayFrame.OPCODE_MAP.putIfAbsent(field.getInt(null), name);
				} catch (final IllegalAccessException exception) {
					continue;
				}
			}
		}
	}

	private final int          depth;
	private final ReplayRun    run;
	private final ReplaySlice  slice;
	private final ReplayMethod method;

	private final int[]     sizes;
	private final Object[]  values;
	private final boolean[] lives;
	private final Object[]  locals;
	private final boolean[] localLives;
	private final boolean[] localStates;

	private int     top;
	private boolean lastLive;
	private boolean controlLive;

	private ReplayFrame(final ReplayRun run, final ReplaySlice slice, final int depth) {
		this.run         = run;
		this.slice       = slice;
		this.method      = slice.getMethod();
		this.depth       = depth;
		this.values      = new Object[this.method.getMethod().maxStack + 2];
		this.lives       = new boolean[this.values.length];
		this.sizes       = new int[this.values.length];
		this.locals      = new Object[this.method.getMethod().maxLocals + 2];
		this.localLives  = new boolean[this.locals.length];
		this.localStates = new boolean[this.locals.length];
	}

	public static ReplayFrame create(final ReplayRun run, final ReplaySlice slice, final int depth) {
		return new ReplayFrame(run, slice, depth);
	}

	public Object execute() {
		final AbstractInsnNode[] instructions = this.method.getInstructions();
		int index = this.slice.getStart();
		while (index < this.slice.getEnd()) {
			try {
				index = this.step(instructions[index], index);
			} catch (final RuntimeException exception) {
				throw exception instanceof ReplayException ? (ReplayException) exception : new ReplayException(ReplayFailure.REPLAY_FAILED, exception);
			}
		}

		if (this.top == 0) {
			throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, "ending without a value");
		}
		return this.pop();
	}

	private int step(final AbstractInsnNode instruction, final int index) {
		final int opcode = instruction.getOpcode();
		switch (opcode) {
			case -1:
			case Opcodes.NOP:
			case Opcodes.CHECKCAST:
				return index + 1;
			case Opcodes.ACONST_NULL:
				this.push(null, false, 1);
				return index + 1;
			case Opcodes.ICONST_M1:
			case Opcodes.ICONST_0:
			case Opcodes.ICONST_1:
			case Opcodes.ICONST_2:
			case Opcodes.ICONST_3:
			case Opcodes.ICONST_4:
			case Opcodes.ICONST_5:
				this.push(opcode - Opcodes.ICONST_0, false, 1);
				return index + 1;
			case Opcodes.LCONST_0:
			case Opcodes.LCONST_1:
				this.push((long) (opcode - Opcodes.LCONST_0), false, 2);
				return index + 1;
			case Opcodes.FCONST_0:
			case Opcodes.FCONST_1:
			case Opcodes.FCONST_2:
				this.push((float) (opcode - Opcodes.FCONST_0), false, 1);
				return index + 1;
			case Opcodes.DCONST_0:
			case Opcodes.DCONST_1:
				this.push((double) (opcode - Opcodes.DCONST_0), false, 2);
				return index + 1;
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH:
				this.push(((IntInsnNode) instruction).operand, false, 1);
				return index + 1;
			case Opcodes.LDC:
				this.constant(((LdcInsnNode) instruction).cst);
				return index + 1;
			case Opcodes.ILOAD:
			case Opcodes.LLOAD:
			case Opcodes.FLOAD:
			case Opcodes.DLOAD:
			case Opcodes.ALOAD:
				this.load((VarInsnNode) instruction, index);
				return index + 1;
			case Opcodes.ISTORE:
			case Opcodes.LSTORE:
			case Opcodes.FSTORE:
			case Opcodes.DSTORE:
			case Opcodes.ASTORE:
				this.store(((VarInsnNode) instruction).var);
				return index + 1;
			case Opcodes.IINC:
				this.increment((IincInsnNode) instruction, index);
				return index + 1;
			case Opcodes.IALOAD:
			case Opcodes.LALOAD:
			case Opcodes.FALOAD:
			case Opcodes.DALOAD:
			case Opcodes.AALOAD:
			case Opcodes.BALOAD:
			case Opcodes.CALOAD:
			case Opcodes.SALOAD:
				this.arrayLoad(opcode);
				return index + 1;
			case Opcodes.IASTORE:
			case Opcodes.LASTORE:
			case Opcodes.FASTORE:
			case Opcodes.DASTORE:
			case Opcodes.AASTORE:
			case Opcodes.BASTORE:
			case Opcodes.CASTORE:
			case Opcodes.SASTORE:
				this.arrayStore();
				return index + 1;
			case Opcodes.POP:
				this.pop();
				return index + 1;
			case Opcodes.POP2:
				this.popSlots(2);
				return index + 1;
			case Opcodes.DUP:
				this.duplicate(1, 0);
				return index + 1;
			case Opcodes.DUP_X1:
				this.duplicate(1, 1);
				return index + 1;
			case Opcodes.DUP_X2:
				this.duplicate(1, 2);
				return index + 1;
			case Opcodes.DUP2:
				this.duplicate(2, 0);
				return index + 1;
			case Opcodes.DUP2_X1:
				this.duplicate(2, 1);
				return index + 1;
			case Opcodes.DUP2_X2:
				this.duplicate(2, 2);
				return index + 1;
			case Opcodes.SWAP:
				this.duplicate(1, 1);
				this.pop();
				return index + 1;
			case Opcodes.IADD:
			case Opcodes.LADD:
			case Opcodes.FADD:
			case Opcodes.DADD:
			case Opcodes.ISUB:
			case Opcodes.LSUB:
			case Opcodes.FSUB:
			case Opcodes.DSUB:
			case Opcodes.IMUL:
			case Opcodes.LMUL:
			case Opcodes.FMUL:
			case Opcodes.DMUL:
			case Opcodes.IDIV:
			case Opcodes.LDIV:
			case Opcodes.FDIV:
			case Opcodes.DDIV:
			case Opcodes.IREM:
			case Opcodes.LREM:
			case Opcodes.FREM:
			case Opcodes.DREM:
			case Opcodes.ISHL:
			case Opcodes.LSHL:
			case Opcodes.ISHR:
			case Opcodes.LSHR:
			case Opcodes.IUSHR:
			case Opcodes.LUSHR:
			case Opcodes.IAND:
			case Opcodes.LAND:
			case Opcodes.IOR:
			case Opcodes.LOR:
			case Opcodes.IXOR:
			case Opcodes.LXOR:
			case Opcodes.LCMP:
			case Opcodes.FCMPL:
			case Opcodes.FCMPG:
			case Opcodes.DCMPL:
			case Opcodes.DCMPG:
				this.binary(opcode);
				return index + 1;
			case Opcodes.INEG:
			case Opcodes.LNEG:
			case Opcodes.FNEG:
			case Opcodes.DNEG:
			case Opcodes.I2L:
			case Opcodes.I2F:
			case Opcodes.I2D:
			case Opcodes.L2I:
			case Opcodes.L2F:
			case Opcodes.L2D:
			case Opcodes.F2I:
			case Opcodes.F2L:
			case Opcodes.F2D:
			case Opcodes.D2I:
			case Opcodes.D2L:
			case Opcodes.D2F:
			case Opcodes.I2B:
			case Opcodes.I2C:
			case Opcodes.I2S:
				this.unary(opcode);
				return index + 1;
			case Opcodes.IFEQ:
			case Opcodes.IFNE:
			case Opcodes.IFLT:
			case Opcodes.IFGE:
			case Opcodes.IFGT:
			case Opcodes.IFLE:
			case Opcodes.IF_ICMPEQ:
			case Opcodes.IF_ICMPNE:
			case Opcodes.IF_ICMPLT:
			case Opcodes.IF_ICMPGE:
			case Opcodes.IF_ICMPGT:
			case Opcodes.IF_ICMPLE:
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ACMPNE:
			case Opcodes.IFNULL:
			case Opcodes.IFNONNULL:
				return this.isTaken(opcode) ? this.jump(((JumpInsnNode) instruction).label, opcode) : index + 1;
			case Opcodes.GOTO:
				return this.jump(((JumpInsnNode) instruction).label, opcode);
			case Opcodes.TABLESWITCH: {
				final TableSwitchInsnNode table = (TableSwitchInsnNode) instruction;
				final int key = ReplayFrame.toInt(this.known());
				return this.jump(key >= table.min && key <= table.max ? table.labels.get(key - table.min) : table.dflt, opcode);
			}
			case Opcodes.LOOKUPSWITCH: {
				final LookupSwitchInsnNode lookup = (LookupSwitchInsnNode) instruction;
				final int position = lookup.keys.indexOf(ReplayFrame.toInt(this.known()));
				return this.jump(position >= 0 ? lookup.labels.get(position) : lookup.dflt, opcode);
			}
			case Opcodes.GETSTATIC:
				this.push(this.fieldValue((FieldInsnNode) instruction, null), false, Type.getType(((FieldInsnNode) instruction).desc).getSize());
				return index + 1;
			case Opcodes.GETFIELD:
				this.getField((FieldInsnNode) instruction);
				return index + 1;
			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKESPECIAL:
			case Opcodes.INVOKESTATIC:
			case Opcodes.INVOKEINTERFACE:
				this.invoke((MethodInsnNode) instruction, index);
				return index + 1;
			case Opcodes.INVOKEDYNAMIC:
				this.invokeDynamic((InvokeDynamicInsnNode) instruction, index);
				return index + 1;
			case Opcodes.NEW: {
				final String type = ((TypeInsnNode) instruction).desc;
				this.push(type.equals("java/lang/StringBuilder") || type.equals("java/lang/StringBuffer") ? ReplayParts.create() : new Object(), false, 1);
				return index + 1;
			}
			case Opcodes.NEWARRAY:
			case Opcodes.ANEWARRAY:
				this.newArray(instruction);
				return index + 1;
			case Opcodes.ARRAYLENGTH: {
				final Object array = this.pop();
				if (ReplayFrame.isUnknown(array)) {
					this.unknown(array, this.lastLive, 1);
				} else {
					this.push(Array.getLength(array), this.lastLive, 1);
				}
				return index + 1;
			}
			case Opcodes.INSTANCEOF: {
				final Object value = this.pop();
				if (ReplayFrame.isUnknown(value)) {
					this.unknown(value, this.lastLive, 1);
				} else {
					this.push(this.method.type(Type.getObjectType(((TypeInsnNode) instruction).desc)).isInstance(value) ? 1 : 0, this.lastLive, 1);
				}
				return index + 1;
			}
			default:
				throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, ReplayFrame.OPCODE_MAP.getOrDefault(opcode, String.valueOf(opcode)));
		}
	}

	private void constant(final Object constant) {
		if (constant instanceof Type) {
			final Type type = (Type) constant;
			if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
				throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, "LDC " + type);
			}
			this.push(this.method.type(type), false, 1);
		} else if (constant instanceof Integer || constant instanceof Float || constant instanceof String) {
			this.push(constant, false, 1);
		} else if (constant instanceof Long || constant instanceof Double) {
			this.push(constant, false, 2);
		} else {
			throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, "LDC " + constant);
		}
	}

	private void load(final VarInsnNode instruction, final int index) {
		final int variable = instruction.var;
		final int size = instruction.getOpcode() == Opcodes.LLOAD || instruction.getOpcode() == Opcodes.DLOAD ? 2 : 1;
		if (this.localStates[variable]) {
			this.push(this.locals[variable], this.localLives[variable], size);
			return;
		}

		if (variable == 0 && !this.method.isStatic()) {
			final Object self = this.run.getSelf();
			this.push(self != null ? self : ReplayUnknown.create("this"), false, 1);
			return;
		}

		final ReplaySlice definition = this.run.isDefinitions() && this.depth < 8 ? this.method.getDefinitionMap().get(instruction) : null;
		final Object[] result = definition != null ? this.run.define(definition, this.depth + 1) : null;
		if (result != null) {
			this.push(result[0], (Boolean) result[1], size);
		} else {
			this.push(ReplayUnknown.create(this.method.describeLocal(variable, index), variable), false, size);
		}
	}

	private void store(final int variable) {
		final Object value = this.pop();
		this.locals[variable] = value;
		this.localLives[variable] = this.lastLive || this.controlLive;
		this.localStates[variable] = true;
	}

	private void increment(final IincInsnNode instruction, final int index) {
		final int variable = instruction.var;
		if (this.localStates[variable] && !ReplayFrame.isUnknown(this.locals[variable])) {
			this.locals[variable] = ReplayFrame.toInt(this.locals[variable]) + instruction.incr;
		} else {
			this.locals[variable] = ReplayUnknown.create(this.method.describeLocal(variable, index));
			this.localStates[variable] = true;
		}
	}

	private void arrayLoad(final int opcode) {
		final Object position = this.pop();
		boolean live = this.lastLive;
		final Object array = this.pop();
		live |= this.lastLive;
		final int size = opcode == Opcodes.LALOAD || opcode == Opcodes.DALOAD ? 2 : 1;
		if (ReplayFrame.isUnknown(array) || ReplayFrame.isUnknown(position)) {
			this.unknown(ReplayFrame.isUnknown(array) ? array : position, live, size);
			return;
		}

		try {
			final Object element = Array.get(array, ReplayFrame.toInt(position));
			this.push(opcode == Opcodes.AALOAD ? element : ReplayFrame.normalize(element), live, size);
		} catch (final RuntimeException exception) {
			throw new ReplayException(ReplayFailure.REPLAY_FAILED, exception);
		}
	}

	private void arrayStore() {
		final Object value = this.pop();
		boolean live = this.lastLive;
		final Object position = this.pop();
		live |= this.lastLive;
		final Object array = this.pop();
		live |= this.lastLive;
		if (ReplayFrame.isUnknown(array)) {
			return;
		}

		if (ReplayFrame.isUnknown(value) || ReplayFrame.isUnknown(position)) {
			if (live || this.controlLive) {
				throw new ReplayException(ReplayFailure.LOCAL_COMBINED, ReplayFrame.describe(ReplayFrame.isUnknown(value) ? value : position));
			}
			this.replace(array, ReplayUnknown.create(ReplayFrame.describe(ReplayFrame.isUnknown(value) ? value : position)), false);
			return;
		}

		try {
			Array.set(array, ReplayFrame.toInt(position), ReplayFrame.convert(value, array.getClass().getComponentType()));
		} catch (final RuntimeException exception) {
			throw new ReplayException(ReplayFailure.REPLAY_FAILED, exception);
		}
		if (live) {
			this.replace(array, array, true);
		}
	}

	private void newArray(final AbstractInsnNode instruction) {
		final Object count = this.pop();
		final boolean live = this.lastLive;
		if (ReplayFrame.isUnknown(count)) {
			this.unknown(count, live, 1);
			return;
		}

		final Class<?> component;
		if (instruction.getOpcode() == Opcodes.ANEWARRAY) {
			component = this.method.type(Type.getObjectType(((TypeInsnNode) instruction).desc));
		} else {
			component = ReplayFrame.primitive(((IntInsnNode) instruction).operand);
		}
		this.push(Array.newInstance(component, ReplayFrame.toInt(count)), live, 1);
	}

	private void binary(final int opcode) {
		final Object right = this.pop();
		boolean live = this.lastLive;
		final Object left = this.pop();
		live |= this.lastLive;
		final int size = ReplayFrame.resultSize(opcode);
		if (ReplayFrame.isUnknown(left) || ReplayFrame.isUnknown(right)) {
			this.unknown(ReplayFrame.isUnknown(left) ? left : right, live, size);
			return;
		}

		try {
			this.push(ReplayFrame.compute(opcode, left, right), live, size);
		} catch (final ArithmeticException exception) {
			throw new ReplayException(ReplayFailure.REPLAY_FAILED, exception);
		}
	}

	private void unary(final int opcode) {
		final Object value = this.pop();
		final boolean live = this.lastLive;
		final int size = opcode == Opcodes.LNEG || opcode == Opcodes.DNEG || opcode == Opcodes.I2L || opcode == Opcodes.I2D || opcode == Opcodes.F2L || opcode == Opcodes.F2D || opcode == Opcodes.L2D || opcode == Opcodes.D2L ? 2 : 1;
		if (ReplayFrame.isUnknown(value)) {
			this.unknown(value, live, size);
			return;
		}

		final Number number = (Number) value;
		switch (opcode) {
			case Opcodes.INEG:
				this.push(-number.intValue(), live, size);
				break;
			case Opcodes.LNEG:
				this.push(-number.longValue(), live, size);
				break;
			case Opcodes.FNEG:
				this.push(-number.floatValue(), live, size);
				break;
			case Opcodes.DNEG:
				this.push(-number.doubleValue(), live, size);
				break;
			case Opcodes.I2L:
			case Opcodes.F2L:
			case Opcodes.D2L:
				this.push(number.longValue(), live, size);
				break;
			case Opcodes.I2F:
			case Opcodes.L2F:
			case Opcodes.D2F:
				this.push(number.floatValue(), live, size);
				break;
			case Opcodes.I2D:
			case Opcodes.L2D:
			case Opcodes.F2D:
				this.push(number.doubleValue(), live, size);
				break;
			case Opcodes.I2B:
				this.push((int) (byte) number.intValue(), live, size);
				break;
			case Opcodes.I2C:
				this.push((int) (char) number.intValue(), live, size);
				break;
			case Opcodes.I2S:
				this.push((int) (short) number.intValue(), live, size);
				break;
			default:
				this.push(number.intValue(), live, size);
				break;
		}
	}

	private boolean isTaken(final int opcode) {
		if (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IFLE) {
			final int value = ReplayFrame.toInt(this.known());
			switch (opcode) {
				case Opcodes.IFEQ:
					return value == 0;
				case Opcodes.IFNE:
					return value != 0;
				case Opcodes.IFLT:
					return value < 0;
				case Opcodes.IFGE:
					return value >= 0;
				case Opcodes.IFGT:
					return value > 0;
				default:
					return value <= 0;
			}
		}

		if (opcode >= Opcodes.IF_ICMPEQ && opcode <= Opcodes.IF_ICMPLE) {
			final int right = ReplayFrame.toInt(this.known());
			final int left = ReplayFrame.toInt(this.known());
			switch (opcode) {
				case Opcodes.IF_ICMPEQ:
					return left == right;
				case Opcodes.IF_ICMPNE:
					return left != right;
				case Opcodes.IF_ICMPLT:
					return left < right;
				case Opcodes.IF_ICMPGE:
					return left >= right;
				case Opcodes.IF_ICMPGT:
					return left > right;
				default:
					return left <= right;
			}
		}

		if (opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE) {
			final Object right = this.known();
			final Object left = this.known();
			return left == right == (opcode == Opcodes.IF_ACMPEQ);
		}
		return this.known() == null == (opcode == Opcodes.IFNULL);
	}

	private int jump(final LabelNode label, final int opcode) {
		final int target = this.method.indexOf(label);
		if (target < this.slice.getStart() || target > this.slice.getEnd()) {
			throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, ReplayFrame.OPCODE_MAP.get(opcode) + " out of the expression");
		}
		return target;
	}

	private Object fieldValue(final FieldInsnNode instruction, final Object target) {
		final Field field = (Field) this.method.member(instruction);
		try {
			final Object value = field.get(target);
			return field.getType().isPrimitive() ? ReplayFrame.normalize(value) : value;
		} catch (final IllegalAccessException | RuntimeException exception) {
			throw new ReplayException(ReplayFailure.REPLAY_FAILED, exception);
		}
	}

	private void getField(final FieldInsnNode instruction) {
		final Object target = this.pop();
		final boolean live = this.lastLive;
		final int size = Type.getType(instruction.desc).getSize();
		if (target instanceof ReplayUnknown) {
			final String description = ((ReplayUnknown) target).getDescription();
			this.unknown(ReplayUnknown.create(description.equals("this") ? "the field " + instruction.name : description), live, size);
			return;
		}
		this.push(this.fieldValue(instruction, target), live, size);
	}

	private void invoke(final MethodInsnNode instruction, final int index) {
		final Type[] argumentTypes = Type.getArgumentTypes(instruction.desc);
		final Type returnType = Type.getReturnType(instruction.desc);
		final Object[] arguments = new Object[argumentTypes.length];
		Object unknown = null;
		boolean live = false;
		for (int position = argumentTypes.length - 1; position >= 0; position--) {
			arguments[position] = this.pop();
			live |= this.lastLive;
			if (unknown == null && ReplayFrame.isUnknown(arguments[position])) {
				unknown = arguments[position];
			}
		}

		if (instruction.getOpcode() != Opcodes.INVOKESTATIC && this.values[this.top - 1] instanceof ReplayParts && (instruction.owner.equals("java/lang/StringBuilder") || instruction.owner.equals("java/lang/StringBuffer"))) {
			this.build(instruction, arguments, argumentTypes, index, live);
			return;
		}

		if (instruction.name.equals("<init>")) {
			final Object target = this.pop();
			live |= this.lastLive;
			if (unknown != null) {
				if (live || this.controlLive) {
					throw new ReplayException(ReplayFailure.LOCAL_COMBINED, ReplayFrame.describe(unknown));
				}
				this.replace(target, ReplayUnknown.create(ReplayFrame.describe(unknown)), false);
				return;
			}

			final Constructor<?> constructor = (Constructor<?>) this.method.member(instruction);
			final long readTotal = SignalContext.current().getReadTotal();
			final Object instance = this.call(() -> constructor.newInstance(ReplayFrame.convert(arguments, constructor.getParameterTypes())));
			this.replace(target, instance, live || SignalContext.current().getReadTotal() != readTotal);
			return;
		}

		Object receiver = null;
		if (instruction.getOpcode() != Opcodes.INVOKESTATIC) {
			receiver = this.pop();
			live |= this.lastLive;
			if (receiver instanceof ReplayUnknown && this.method.isSignal(instruction.owner)) {
				final Object key = ((ReplayUnknown) receiver).getKey();
				receiver = this.run.position(key != null ? key : instruction, this.method.type(Type.getObjectType(instruction.owner)));
			} else if (unknown == null && ReplayFrame.isUnknown(receiver)) {
				unknown = "this".equals(ReplayFrame.describe(receiver)) ? ReplayUnknown.create("the method " + instruction.name + "()") : receiver;
			}
		}

		if (unknown != null) {
			if (returnType.getSort() != Type.VOID) {
				this.unknown(unknown, live, returnType.getSize());
			} else if (live || this.controlLive) {
				throw new ReplayException(ReplayFailure.LOCAL_COMBINED, ReplayFrame.describe(unknown));
			}
			return;
		}

		final Method method = (Method) this.method.member(instruction);
		final Object target = Modifier.isStatic(method.getModifiers()) ? null : receiver;
		final long readTotal = SignalContext.current().getReadTotal();
		final Object result = this.call(() -> method.invoke(target, ReplayFrame.convert(arguments, method.getParameterTypes())));
		if (returnType.getSort() != Type.VOID) {
			this.push(returnType.getSort() == Type.OBJECT || returnType.getSort() == Type.ARRAY ? result : ReplayFrame.normalize(result), live || SignalContext.current().getReadTotal() != readTotal, returnType.getSize());
		}
	}

	private void build(final MethodInsnNode instruction, final Object[] arguments, final Type[] argumentTypes, final int index, final boolean live) {
		final ReplayParts parts = (ReplayParts) this.pop();
		parts.setLive(parts.isLive() || live || this.lastLive || this.controlLive);
		switch (instruction.name) {
			case "<init>":
				if (arguments.length == 1 && argumentTypes[0].getSort() != Type.INT) {
					this.piece(parts, arguments[0], argumentTypes[0], index + ":0");
				}
				return;
			case "append":
				this.piece(parts, arguments[0], argumentTypes[0], index + ":0");
				this.push(parts, parts.isLive(), 1);
				return;
			case "toString":
				this.push(parts.result(), parts.isLive(), 1);
				return;
			default:
				throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, "StringBuilder." + instruction.name);
		}
	}

	private void invokeDynamic(final InvokeDynamicInsnNode instruction, final int index) {
		final String bootstrap = instruction.bsm.getOwner();
		if (bootstrap.equals("java/lang/invoke/LambdaMetafactory")) {
			throw new ReplayException(ReplayFailure.LAMBDA);
		}

		if (!bootstrap.equals("java/lang/invoke/StringConcatFactory")) {
			throw new ReplayException(ReplayFailure.UNSUPPORTED_INSTRUCTION, "INVOKEDYNAMIC " + instruction.name);
		}

		final Type[] argumentTypes = Type.getArgumentTypes(instruction.desc);
		final Object[] arguments = new Object[argumentTypes.length];
		boolean live = false;
		for (int position = argumentTypes.length - 1; position >= 0; position--) {
			arguments[position] = this.pop();
			live |= this.lastLive;
		}

		final ReplayParts parts = ReplayParts.create();
		final String recipe = instruction.bsm.getName().equals("makeConcatWithConstants") ? (String) instruction.bsmArgs[0] : null;
		if (recipe == null) {
			for (int position = 0; position < arguments.length; position++) {
				this.piece(parts, arguments[position], argumentTypes[position], index + ":" + position);
			}
		} else {
			int argument = 0;
			int constant = 1;
			for (final char character : recipe.toCharArray()) {
				if (character == '\u0001') {
					this.piece(parts, arguments[argument], argumentTypes[argument], index + ":" + argument);
					argument++;
				} else if (character == '\u0002') {
					parts.add(String.valueOf(instruction.bsmArgs[constant++]));
				} else {
					parts.add(String.valueOf(character));
				}
			}
		}
		this.push(parts.result(), live || this.controlLive, 1);
	}

	private void piece(final ReplayParts parts, final Object value, final Type type, final String key) {
		if (value instanceof ReplayUnknown) {
			final Map<String, String> holeMap = this.run.getHoleMap();
			if (holeMap != null && holeMap.containsKey(key)) {
				parts.add(holeMap.get(key));
				return;
			}

			if (this.controlLive) {
				throw new ReplayException(ReplayFailure.LOCAL_COMBINED, ReplayFrame.describe(value));
			}
			parts.addHole(key, ReplayFrame.describe(value));
		} else if (value instanceof ReplayParts) {
			parts.add(value);
		} else if (type.getSort() == Type.CHAR) {
			parts.add(String.valueOf((char) ReplayFrame.toInt(value)));
		} else if (type.getSort() == Type.BOOLEAN) {
			parts.add(String.valueOf(ReplayFrame.toInt(value) != 0));
		} else if (value instanceof char[]) {
			parts.add(String.valueOf((char[]) value));
		} else {
			parts.add(String.valueOf(value));
		}
	}

	private Object call(final Invocation invocation) {
		try {
			return invocation.invoke();
		} catch (final InvocationTargetException exception) {
			throw new ReplayException(ReplayFailure.REPLAY_FAILED, exception.getCause());
		} catch (final ReflectiveOperationException | IllegalArgumentException exception) {
			throw new ReplayException(ReplayFailure.REPLAY_FAILED, exception);
		}
	}

	private Object known() {
		final Object value = this.pop();
		if (ReplayFrame.isUnknown(value)) {
			throw new ReplayException(ReplayFailure.LOCAL_CONDITION, ReplayFrame.describe(value));
		}

		this.controlLive |= this.lastLive;
		return value;
	}

	private void unknown(final Object unknown, final boolean live, final int size) {
		if (live || this.controlLive) {
			throw new ReplayException(ReplayFailure.LOCAL_COMBINED, ReplayFrame.describe(unknown));
		}
		this.push(ReplayUnknown.create(ReplayFrame.describe(unknown)), false, size);
	}

	private void push(final Object value, final boolean live, final int size) {
		this.values[this.top] = value;
		this.lives[this.top] = live || this.controlLive;
		this.sizes[this.top] = size;
		this.top++;
	}

	private Object pop() {
		this.top--;
		this.lastLive = this.lives[this.top];
		final Object value = this.values[this.top];
		this.values[this.top] = null;
		return value;
	}

	private void popSlots(final int slots) {
		int removed = 0;
		while (removed < slots) {
			removed += this.sizes[this.top - 1];
			this.pop();
		}
	}

	private void duplicate(final int copied, final int skipped) {
		int copiedCount = 0;
		for (int slots = 0; slots < copied; copiedCount++) {
			slots += this.sizes[this.top - 1 - copiedCount];
		}

		int skippedCount = 0;
		for (int slots = 0; slots < skipped; skippedCount++) {
			slots += this.sizes[this.top - 1 - copiedCount - skippedCount];
		}

		final int base = this.top - copiedCount - skippedCount;
		System.arraycopy(this.values, base, this.values, base + copiedCount, copiedCount + skippedCount);
		System.arraycopy(this.lives, base, this.lives, base + copiedCount, copiedCount + skippedCount);
		System.arraycopy(this.sizes, base, this.sizes, base + copiedCount, copiedCount + skippedCount);
		System.arraycopy(this.values, base + copiedCount + skippedCount, this.values, base, copiedCount);
		System.arraycopy(this.lives, base + copiedCount + skippedCount, this.lives, base, copiedCount);
		System.arraycopy(this.sizes, base + copiedCount + skippedCount, this.sizes, base, copiedCount);
		this.top += copiedCount;
	}

	private void replace(final Object previous, final Object value, final boolean live) {
		for (int position = 0; position < this.top; position++) {
			if (this.values[position] == previous) {
				this.values[position] = value;
				this.lives[position] |= live || this.controlLive;
			}
		}
	}

	private static boolean isUnknown(final Object value) {
		return value instanceof ReplayUnknown || value instanceof ReplayParts && ((ReplayParts) value).hasHoles();
	}

	private static String describe(final Object unknown) {
		if (unknown instanceof ReplayUnknown) {
			return ((ReplayUnknown) unknown).getDescription();
		}
		return unknown instanceof ReplayParts ? ((ReplayParts) unknown).getDescription() : "a local variable";
	}

	private static int resultSize(final int opcode) {
		switch (opcode) {
			case Opcodes.LADD:
			case Opcodes.LSUB:
			case Opcodes.LMUL:
			case Opcodes.LDIV:
			case Opcodes.LREM:
			case Opcodes.LSHL:
			case Opcodes.LSHR:
			case Opcodes.LUSHR:
			case Opcodes.LAND:
			case Opcodes.LOR:
			case Opcodes.LXOR:
			case Opcodes.DADD:
			case Opcodes.DSUB:
			case Opcodes.DMUL:
			case Opcodes.DDIV:
			case Opcodes.DREM:
				return 2;
			default:
				return 1;
		}
	}

	private static Object compute(final int opcode, final Object left, final Object right) {
		final Number a = (Number) left;
		final Number b = (Number) right;
		switch (opcode) {
			case Opcodes.IADD:
				return a.intValue() + b.intValue();
			case Opcodes.ISUB:
				return a.intValue() - b.intValue();
			case Opcodes.IMUL:
				return a.intValue() * b.intValue();
			case Opcodes.IDIV:
				return a.intValue() / b.intValue();
			case Opcodes.IREM:
				return a.intValue() % b.intValue();
			case Opcodes.ISHL:
				return a.intValue() << b.intValue();
			case Opcodes.ISHR:
				return a.intValue() >> b.intValue();
			case Opcodes.IUSHR:
				return a.intValue() >>> b.intValue();
			case Opcodes.IAND:
				return a.intValue() & b.intValue();
			case Opcodes.IOR:
				return a.intValue() | b.intValue();
			case Opcodes.IXOR:
				return a.intValue() ^ b.intValue();
			case Opcodes.LADD:
				return a.longValue() + b.longValue();
			case Opcodes.LSUB:
				return a.longValue() - b.longValue();
			case Opcodes.LMUL:
				return a.longValue() * b.longValue();
			case Opcodes.LDIV:
				return a.longValue() / b.longValue();
			case Opcodes.LREM:
				return a.longValue() % b.longValue();
			case Opcodes.LSHL:
				return a.longValue() << b.intValue();
			case Opcodes.LSHR:
				return a.longValue() >> b.intValue();
			case Opcodes.LUSHR:
				return a.longValue() >>> b.intValue();
			case Opcodes.LAND:
				return a.longValue() & b.longValue();
			case Opcodes.LOR:
				return a.longValue() | b.longValue();
			case Opcodes.LXOR:
				return a.longValue() ^ b.longValue();
			case Opcodes.FADD:
				return a.floatValue() + b.floatValue();
			case Opcodes.FSUB:
				return a.floatValue() - b.floatValue();
			case Opcodes.FMUL:
				return a.floatValue() * b.floatValue();
			case Opcodes.FDIV:
				return a.floatValue() / b.floatValue();
			case Opcodes.FREM:
				return a.floatValue() % b.floatValue();
			case Opcodes.DADD:
				return a.doubleValue() + b.doubleValue();
			case Opcodes.DSUB:
				return a.doubleValue() - b.doubleValue();
			case Opcodes.DMUL:
				return a.doubleValue() * b.doubleValue();
			case Opcodes.DDIV:
				return a.doubleValue() / b.doubleValue();
			case Opcodes.DREM:
				return a.doubleValue() % b.doubleValue();
			case Opcodes.LCMP:
				return Long.compare(a.longValue(), b.longValue());
			case Opcodes.FCMPL:
			case Opcodes.FCMPG:
				return Float.isNaN(a.floatValue()) || Float.isNaN(b.floatValue()) ? opcode == Opcodes.FCMPG ? 1 : -1 : a.floatValue() == b.floatValue() ? 0 : a.floatValue() < b.floatValue() ? -1 : 1;
			default:
				return Double.isNaN(a.doubleValue()) || Double.isNaN(b.doubleValue()) ? opcode == Opcodes.DCMPG ? 1 : -1 : a.doubleValue() == b.doubleValue() ? 0 : a.doubleValue() < b.doubleValue() ? -1 : 1;
		}
	}

	private static Class<?> primitive(final int type) {
		switch (type) {
			case Opcodes.T_BOOLEAN:
				return boolean.class;
			case Opcodes.T_CHAR:
				return char.class;
			case Opcodes.T_FLOAT:
				return float.class;
			case Opcodes.T_DOUBLE:
				return double.class;
			case Opcodes.T_BYTE:
				return byte.class;
			case Opcodes.T_SHORT:
				return short.class;
			case Opcodes.T_INT:
				return int.class;
			default:
				return long.class;
		}
	}

	private static Object normalize(final Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value ? 1 : 0;
		}

		if (value instanceof Character) {
			return (int) (Character) value;
		}
		return value instanceof Byte || value instanceof Short ? ((Number) value).intValue() : value;
	}

	private static Object[] convert(final Object[] arguments, final Class<?>[] types) {
		for (int position = 0; position < arguments.length; position++) {
			arguments[position] = ReplayFrame.convert(arguments[position], types[position]);
		}
		return arguments;
	}

	private static Object convert(final Object value, final Class<?> type) {
		if (!(value instanceof Integer)) {
			return value;
		}

		final int number = (Integer) value;
		if (type == boolean.class || type == Boolean.class) {
			return number != 0;
		}

		if (type == char.class || type == Character.class) {
			return (char) number;
		}

		if (type == byte.class || type == Byte.class) {
			return (byte) number;
		}
		return type == short.class || type == Short.class ? (Object) (short) number : value;
	}

	private static int toInt(final Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value ? 1 : 0;
		}

		if (value instanceof Character) {
			return (Character) value;
		}
		return ((Number) value).intValue();
	}

	@FunctionalInterface
	private interface Invocation {

		public Object invoke() throws ReflectiveOperationException;

	}

}