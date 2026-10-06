package dev.joid.lib.utils.signal.replay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayParts {

	private final List<Object> partList = new ArrayList<>();

	private boolean live;

	public static ReplayParts create() {
		return new ReplayParts();
	}

	public void add(final Object part) {
		if (part instanceof ReplayParts) {
			this.partList.addAll(((ReplayParts) part).partList);
		} else {
			this.partList.add(part);
		}
	}

	public void addHole(final String key, final String description) {
		this.partList.add(new Hole(key, description));
	}

	public boolean hasHoles() {
		return this.getDescription() != null;
	}

	public String getDescription() {
		for (final Object part : this.partList) {
			if (part instanceof Hole) {
				return ((Hole) part).description;
			}
		}
		return null;
	}

	public Object result() {
		if (this.hasHoles()) {
			return this;
		}

		final StringBuilder builder = new StringBuilder();
		for (final Object part : this.partList) {
			builder.append(part);
		}
		return builder.toString();
	}

	public Map<String, String> solve(final String expected) {
		final StringBuilder regex = new StringBuilder("(?s)");
		final List<String> keyList = new ArrayList<>();
		for (final Object part : this.partList) {
			if (part instanceof Hole) {
				regex.append("(.*?)");
				keyList.add(((Hole) part).key);
			} else {
				regex.append(Pattern.quote(String.valueOf(part)));
			}
		}

		final Matcher matcher = Pattern.compile(regex.toString()).matcher(expected);
		if (!matcher.matches()) {
			return null;
		}

		final Map<String, String> holeMap = new HashMap<>();
		for (int index = 0; index < keyList.size(); index++) {
			holeMap.putIfAbsent(keyList.get(index), matcher.group(index + 1));
		}
		return holeMap;
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Hole {

		private final String key;
		private final String description;

	}

}