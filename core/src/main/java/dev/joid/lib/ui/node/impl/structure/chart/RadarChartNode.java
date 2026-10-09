package dev.joid.lib.ui.node.impl.structure.chart;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class RadarChartNode<DATA extends RadarChartData> extends Node {

	private final List<DATA> dataList;

	protected RadarChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.dataList = new LinkedList<>();
	}

	public final boolean isLoaded() {
		return this.isMounted() && this.dataList.size() >= 3 && !this.dataList.stream().anyMatch(DATA::isEmpty);
	}

	public final Number getMax() {
		final double min = this.dataList.stream().filter(data -> !data.isEmpty()).mapToDouble(data -> data.getValue().doubleValue()).min().orElse(0);
		final double max = this.dataList.stream().filter(data -> !data.isEmpty()).mapToDouble(data -> data.getValue().doubleValue()).max().orElse(0);
		return max == 0 ? 1 : min == max ? max * 2 : max;
	}

	public final Number getMin() {
		final double min = this.dataList.stream().filter(data -> !data.isEmpty()).mapToDouble(data -> data.getValue().doubleValue()).min().orElse(0);
		final double max = this.dataList.stream().filter(data -> !data.isEmpty()).mapToDouble(data -> data.getValue().doubleValue()).max().orElse(0);
		return min == max ? 0 : min;
	}

	public final Number getAverage() {
		return this.dataList.stream().filter(data -> !data.isEmpty()).mapToDouble(data -> data.getValue().doubleValue()).average().orElse(0);
	}

	public final <T extends RadarChartNode<DATA>> @NonNull T data(final @NonNull DATA data) {
		this.dataList.add(data);
		return (T) this;
	}

	public final <T extends RadarChartNode<DATA>> @NonNull T remove(final @NonNull DATA data) {
		this.dataList.remove(data);
		return (T) this;
	}

	public final <T extends RadarChartNode<DATA>> @NonNull T clear() {
		this.dataList.clear();
		return (T) this;
	}

	public static class RadarChartData {

		private Supplier<String> label;
		private Supplier<? extends Number> value;

		protected RadarChartData(final @NonNull String label) {
			this(label, null);
		}

		protected RadarChartData(final @NonNull String label, final Number value) {
			this.label = () -> label;
			this.value = () -> value;
		}

		public static @NonNull RadarChartData create(final @NonNull String label) {
			return new RadarChartData(label);
		}

		public static @NonNull RadarChartData create(final @NonNull String label, final Number value) {
			return new RadarChartData(label, value);
		}

		public boolean isEmpty() {
			return this.value.get() == null;
		}

		public final String getLabel() {
			return this.label.get();
		}

		public final Number getValue() {
			return this.value.get();
		}

		public final <T extends RadarChartData> @NonNull T value(final Number value) {
			return this.value(Signal.from(value));
		}

		public final <T extends RadarChartData> @NonNull T value(final @NonNull Supplier<? extends Number> value) {
			this.value = value;
			return (T) this;
		}

		public final <T extends RadarChartData> @NonNull T label(final String label) {
			return this.label(Signal.from(label));
		}

		public final <T extends RadarChartData> @NonNull T label(final @NonNull Supplier<String> label) {
			this.label = label;
			return (T) this;
		}

		public final <T extends RadarChartData> @NonNull T clear() {
			this.value = () -> null;
			return (T) this;
		}

	}

}