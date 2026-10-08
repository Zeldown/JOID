package dev.joid.impl.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpirvBlockLayout {

	private static final int OP_NAME            = 5;
	private static final int OP_MEMBER_NAME     = 6;
	private static final int OP_TYPE_STRUCT     = 30;
	private static final int OP_DECORATE        = 71;
	private static final int OP_MEMBER_DECORATE = 72;

	private static final int DECORATION_OFFSET        = 35;
	private static final int DECORATION_ARRAY_STRIDE  = 6;
	private static final int DECORATION_MATRIX_STRIDE = 7;

	public static @NonNull Map<@NonNull String, int[]> read(final @NonNull ByteBuffer module, final @NonNull String block) {
		final IntBuffer words = module.duplicate().order(module.order()).asIntBuffer();
		final Map<Integer, String> nameMap = new HashMap<>();
		final Map<Integer, int[]> structMap = new HashMap<>();
		final Map<Integer, Integer> arrayStrideMap = new HashMap<>();
		final Map<Integer, Map<Integer, String>> memberNameMap = new HashMap<>();
		final Map<Integer, Map<Integer, int[]>> memberDecorationMap = new HashMap<>();

		int index = 5;
		while (index < words.limit()) {
			final int opcode = words.get(index) & 0xFFFF;
			final int length = words.get(index) >>> 16;
			if (opcode == SpirvBlockLayout.OP_NAME) {
				nameMap.put(words.get(index + 1), SpirvBlockLayout.readString(words, index + 2, index + length));
			} else if (opcode == SpirvBlockLayout.OP_MEMBER_NAME) {
				memberNameMap.computeIfAbsent(words.get(index + 1), key -> new HashMap<>()).put(words.get(index + 2), SpirvBlockLayout.readString(words, index + 3, index + length));
			} else if (opcode == SpirvBlockLayout.OP_TYPE_STRUCT) {
				final int[] members = new int[length - 2];
				for (int i = 0; i < members.length; i++) {
					members[i] = words.get(index + 2 + i);
				}
				structMap.put(words.get(index + 1), members);
			} else if (opcode == SpirvBlockLayout.OP_DECORATE && words.get(index + 2) == SpirvBlockLayout.DECORATION_ARRAY_STRIDE) {
				arrayStrideMap.put(words.get(index + 1), words.get(index + 3));
			} else if (opcode == SpirvBlockLayout.OP_MEMBER_DECORATE) {
				final int[] decoration = memberDecorationMap.computeIfAbsent(words.get(index + 1), key -> new HashMap<>()).computeIfAbsent(words.get(index + 2), key -> new int[2]);
				if (words.get(index + 3) == SpirvBlockLayout.DECORATION_OFFSET) {
					decoration[0] = words.get(index + 4);
				} else if (words.get(index + 3) == SpirvBlockLayout.DECORATION_MATRIX_STRIDE) {
					decoration[1] = words.get(index + 4);
				}
			}
			index += Math.max(1, length);
		}

		final Map<String, int[]> layoutMap = new LinkedHashMap<>();
		for (final Map.Entry<Integer, int[]> struct : structMap.entrySet()) {
			if (!block.equals(nameMap.get(struct.getKey()))) {
				continue;
			}

			for (int i = 0; i < struct.getValue().length; i++) {
				final int[] decoration = memberDecorationMap.get(struct.getKey()).getOrDefault(i, new int[2]);
				layoutMap.put(memberNameMap.get(struct.getKey()).get(i), new int[] {decoration[0], arrayStrideMap.getOrDefault(struct.getValue()[i], 0), decoration[1]});
			}
		}
		return layoutMap;
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