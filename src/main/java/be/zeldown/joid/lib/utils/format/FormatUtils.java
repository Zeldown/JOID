package be.zeldown.joid.lib.utils.format;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import lombok.NonNull;

public final class FormatUtils {

	private static final NavigableMap<Long, String> SUFFIX_MAP = new TreeMap<>();

	static {
		FormatUtils.SUFFIX_MAP.put(1_000L, "k");
		FormatUtils.SUFFIX_MAP.put(1_000_000L, "M");
		FormatUtils.SUFFIX_MAP.put(1_000_000_000L, "B");
		FormatUtils.SUFFIX_MAP.put(1_000_000_000_000L, "T");
		FormatUtils.SUFFIX_MAP.put(1_000_000_000_000_000L, "P");
		FormatUtils.SUFFIX_MAP.put(1_000_000_000_000_000_000L, "E");
	}

	public static @NonNull String formatNumber(final long value) {
		if (value == Long.MIN_VALUE) {
			return FormatUtils.formatNumber(Long.MIN_VALUE + 1);
		}

		if (value < 0) {
			return "-" + FormatUtils.formatNumber(-value);
		}

		if (value < 1000) {
			return Long.toString(value);
		}

		final Entry<Long, String> e = FormatUtils.SUFFIX_MAP.floorEntry(value);
		final Long divideBy = e.getKey();
		final String suffix = e.getValue();

		final long truncated = value / (divideBy / 10);
		final boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
	}

}