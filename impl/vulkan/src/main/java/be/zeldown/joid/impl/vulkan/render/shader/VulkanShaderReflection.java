package be.zeldown.joid.impl.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import be.zeldown.joid.impl.vulkan.render.shader.uniform.VulkanUniformBlock;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.VulkanUniformMember;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanShaderReflection {

	private static final int OP_NAME               = 5;
	private static final int OP_MEMBER_NAME        = 6;
	private static final int OP_TYPE_BOOL          = 20;
	private static final int OP_TYPE_INT           = 21;
	private static final int OP_TYPE_FLOAT         = 22;
	private static final int OP_TYPE_VECTOR        = 23;
	private static final int OP_TYPE_MATRIX        = 24;
	private static final int OP_TYPE_SAMPLED_IMAGE = 27;
	private static final int OP_TYPE_ARRAY         = 28;
	private static final int OP_TYPE_STRUCT        = 30;
	private static final int OP_TYPE_POINTER       = 32;
	private static final int OP_CONSTANT           = 43;
	private static final int OP_VARIABLE           = 59;
	private static final int OP_DECORATE           = 71;
	private static final int OP_MEMBER_DECORATE    = 72;

	private static final int DECORATION_ARRAY_STRIDE  = 6;
	private static final int DECORATION_MATRIX_STRIDE = 7;
	private static final int DECORATION_BINDING       = 33;
	private static final int DECORATION_OFFSET        = 35;

	private static final int STORAGE_UNIFORM_CONSTANT = 0;
	private static final int STORAGE_UNIFORM          = 2;

	private final Map<Integer, VulkanUniformBlock> blockMap;
	private final Map<String, Integer>             samplerMap;

	private final Map<Integer, String>               nameMap;
	private final Map<Integer, Map<Integer, String>> memberNameMap;
	private final Map<Integer, Map<Integer, int[]>>  memberDecorationMap;
	private final Map<Integer, Integer>              bindingMap;
	private final Map<Integer, Integer>              arrayStrideMap;
	private final Map<Integer, int[]>                typeMap;
	private final Map<Integer, int[]>                structMap;
	private final Map<Integer, Integer>              pointerMap;
	private final Map<Integer, int[]>                variableMap;
	private final Map<Integer, Integer>              constantMap;
	private final Set<Integer>                       sampledImageSet;

	private VulkanShaderReflection(final ByteBuffer module) {
		this.blockMap            = new HashMap<>();
		this.samplerMap          = new HashMap<>();
		this.nameMap             = new HashMap<>();
		this.memberNameMap       = new HashMap<>();
		this.memberDecorationMap = new HashMap<>();
		this.bindingMap          = new HashMap<>();
		this.arrayStrideMap      = new HashMap<>();
		this.typeMap             = new HashMap<>();
		this.structMap           = new HashMap<>();
		this.pointerMap          = new HashMap<>();
		this.variableMap         = new HashMap<>();
		this.constantMap         = new HashMap<>();
		this.sampledImageSet     = new HashSet<>();

		this.parse(module.duplicate().order(ByteOrder.LITTLE_ENDIAN).asIntBuffer());
		this.resolve();
	}

	public static @NonNull VulkanShaderReflection reflect(final @NonNull ByteBuffer module) {
		return new VulkanShaderReflection(module);
	}

	private void parse(final IntBuffer words) {
		int index = 5;
		while (index < words.limit()) {
			final int opcode = words.get(index) & 0xFFFF;
			final int length = words.get(index) >>> 16;
			switch (opcode) {
			case OP_NAME:
				this.nameMap.put(words.get(index + 1), VulkanShaderReflection.readString(words, index + 2, index + length));
				break;
			case OP_MEMBER_NAME:
				this.memberNameMap.computeIfAbsent(words.get(index + 1), key -> new HashMap<>()).put(words.get(index + 2), VulkanShaderReflection.readString(words, index + 3, index + length));
				break;
			case OP_TYPE_BOOL:
				this.typeMap.put(words.get(index + 1), new int[] {opcode, 32, 0});
				break;
			case OP_TYPE_INT:
			case OP_TYPE_FLOAT:
			case OP_TYPE_VECTOR:
			case OP_TYPE_MATRIX:
			case OP_TYPE_ARRAY:
				this.typeMap.put(words.get(index + 1), new int[] {opcode, words.get(index + 2), words.get(index + 3)});
				break;
			case OP_TYPE_SAMPLED_IMAGE:
				this.sampledImageSet.add(words.get(index + 1));
				break;
			case OP_TYPE_STRUCT:
				final int[] members = new int[length - 2];
				for (int i = 0; i < members.length; i++) {
					members[i] = words.get(index + 2 + i);
				}
				this.structMap.put(words.get(index + 1), members);
				break;
			case OP_TYPE_POINTER:
				this.pointerMap.put(words.get(index + 1), words.get(index + 3));
				break;
			case OP_CONSTANT:
				this.constantMap.put(words.get(index + 2), words.get(index + 3));
				break;
			case OP_VARIABLE:
				this.variableMap.put(words.get(index + 2), new int[] {words.get(index + 1), words.get(index + 3)});
				break;
			case OP_DECORATE:
				if (words.get(index + 2) == VulkanShaderReflection.DECORATION_BINDING) {
					this.bindingMap.put(words.get(index + 1), words.get(index + 3));
				} else if (words.get(index + 2) == VulkanShaderReflection.DECORATION_ARRAY_STRIDE) {
					this.arrayStrideMap.put(words.get(index + 1), words.get(index + 3));
				}
				break;
			case OP_MEMBER_DECORATE:
				final int[] decoration = this.memberDecorationMap.computeIfAbsent(words.get(index + 1), key -> new HashMap<>()).computeIfAbsent(words.get(index + 2), key -> new int[2]);
				if (words.get(index + 3) == VulkanShaderReflection.DECORATION_OFFSET) {
					decoration[0] = words.get(index + 4);
				} else if (words.get(index + 3) == VulkanShaderReflection.DECORATION_MATRIX_STRIDE) {
					decoration[1] = words.get(index + 4);
				}
				break;
			default:
				break;
			}
			index += Math.max(1, length);
		}
	}

	private void resolve() {
		for (final Map.Entry<Integer, int[]> variable : this.variableMap.entrySet()) {
			final Integer binding = this.bindingMap.get(variable.getKey());
			final Integer type = this.pointerMap.get(variable.getValue()[0]);
			if (binding == null || type == null) {
				continue;
			}

			if (variable.getValue()[1] == VulkanShaderReflection.STORAGE_UNIFORM_CONSTANT && this.sampledImageSet.contains(type)) {
				this.samplerMap.put(this.nameMap.get(variable.getKey()), binding);
			} else if (variable.getValue()[1] == VulkanShaderReflection.STORAGE_UNIFORM && this.structMap.containsKey(type)) {
				this.blockMap.put(binding, this.createBlock(binding, type));
			}
		}
	}

	private VulkanUniformBlock createBlock(final int binding, final int struct) {
		final int[] memberTypes = this.structMap.get(struct);
		final Map<Integer, String> memberNames = this.memberNameMap.getOrDefault(struct, new HashMap<>());
		final Map<Integer, int[]> memberDecorations = this.memberDecorationMap.getOrDefault(struct, new HashMap<>());

		int size = 0;
		for (int i = 0; i < memberTypes.length; i++) {
			final int[] decoration = memberDecorations.getOrDefault(i, new int[2]);
			size = Math.max(size, decoration[0] + this.getSize(memberTypes[i], decoration[1]));
		}

		final VulkanUniformBlock block = VulkanUniformBlock.create(binding, size);
		for (int i = 0; i < memberTypes.length; i++) {
			final int[] decoration = memberDecorations.getOrDefault(i, new int[2]);
			block.getMemberMap().put(memberNames.get(i), new VulkanUniformMember(block.getData(), decoration[0], this.arrayStrideMap.getOrDefault(memberTypes[i], 0), decoration[1]));
		}
		return block;
	}

	private int getSize(final int type, final int matrixStride) {
		final int[] definition = this.typeMap.get(type);
		if (definition == null) {
			return 0;
		}

		switch (definition[0]) {
		case OP_TYPE_BOOL:
		case OP_TYPE_INT:
		case OP_TYPE_FLOAT:
			return definition[1] / 8;
		case OP_TYPE_VECTOR:
			return this.getSize(definition[1], 0) * definition[2];
		case OP_TYPE_MATRIX:
			return matrixStride * definition[2];
		case OP_TYPE_ARRAY:
			return this.arrayStrideMap.getOrDefault(type, 0) * this.constantMap.getOrDefault(definition[2], 0);
		default:
			return 0;
		}
	}

	private static String readString(final IntBuffer words, final int start, final int end) {
		final StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			for (int shift = 0; shift < 32; shift += 8) {
				final int character = words.get(i) >>> shift & 0xFF;
				if (character == 0) {
					return builder.toString();
				}
				builder.append((char) character);
			}
		}
		return builder.toString();
	}

}