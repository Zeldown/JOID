package dev.joid.lib.bridge.render.shader.uniform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniformBlock {

	private static final Pattern ARRAY_LENGTH = Pattern.compile("\\[\\s*(\\w+)\\s*\\]");

	private final ByteBuffer                 data;
	private final Map<String, UniformMember> memberMap;

	public static @NonNull UniformBlock create(final @NonNull List<@NonNull ShaderVariable> variables, final @NonNull String code) {
		int end = 0;
		final Map<String, UniformMember> memberMap = new LinkedHashMap<>();
		for (final ShaderVariable variable : variables) {
			final UniformMember member = UniformMember.create(variable.getName(), UniformType.of(variable.getType()), UniformBlock.getLength(variable, code), end);
			memberMap.put(member.getName(), member);
			end = member.getOffset() + member.getSize();
		}
		return new UniformBlock(ByteBuffer.allocateDirect(Math.max((end + 15) / 16 * 16, 16)).order(ByteOrder.nativeOrder()), memberMap);
	}

	public @NonNull UniformBlock value(final @NonNull String name, final int value) {
		final UniformMember member = this.memberMap.get(name);
		if (member != null) {
			member.value(value);
		}
		return this;
	}

	public @NonNull UniformBlock value(final @NonNull String name, final boolean value) {
		final UniformMember member = this.memberMap.get(name);
		if (member != null) {
			member.value(value);
		}
		return this;
	}

	public @NonNull UniformBlock value(final @NonNull String name, final @NonNull float... values) {
		final UniformMember member = this.memberMap.get(name);
		if (member != null) {
			member.value(values);
		}
		return this;
	}

	public boolean pack() {
		boolean changed = false;
		for (final UniformMember member : this.memberMap.values()) {
			if (!member.isDirty()) {
				continue;
			}

			final int components = member.getType().getComponents();
			final int columns = Math.max(member.getType().getColumns(), 1);
			int index = 0;
			for (int element = 0; element < Math.max(member.getLength(), 1); element++) {
				for (int column = 0; column < columns; column++) {
					for (int component = 0; component < components; component++) {
						this.data.putInt(member.getOffset() + element * member.getArrayStride() + column * member.getMatrixStride() + component * 4, member.getValues().getInt(index * 4));
						index++;
					}
				}
			}

			member.clean();
			changed = true;
		}
		return changed;
	}

	public @NonNull UniformBlock upload(final @NonNull Consumer<@NonNull UniformMember> uploader) {
		for (final UniformMember member : this.memberMap.values()) {
			if (member.isDirty()) {
				uploader.accept(member);
				member.clean();
			}
		}
		return this;
	}

	public int getSize() {
		return this.data.capacity();
	}

	public UniformMember getMember(final @NonNull String name) {
		return this.memberMap.get(name);
	}

	private static int getLength(final ShaderVariable variable, final String code) {
		final Matcher matcher = UniformBlock.ARRAY_LENGTH.matcher(variable.getArray());
		if (!matcher.matches()) {
			return 0;
		}

		final String length = matcher.group(1);
		if (length.chars().allMatch(Character::isDigit)) {
			return Integer.parseInt(length);
		}

		final Matcher define = Pattern.compile("#define\\s+" + length + "\\s+(\\d+)").matcher(code);
		if (!define.find()) {
			throw new IllegalArgumentException("Unable to resolve the length " + length + " of the uniform array " + variable.getName());
		}
		return Integer.parseInt(define.group(1));
	}

}