package be.zeldown.joid.lib.ui.node.impl.structure.chart;

import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;
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

	@Override
	public final void drawSkeleton(final double mouseX, final double mouseY) {
		super.draw(mouseX, mouseY);
	}

	/* [ RadarChartNode ] */
	public final boolean isLoaded() {
		return this.isMounted() && this.dataList.size() >= 3 && !this.dataList.stream().anyMatch(DATA::isEmpty);
	}

	public final Number getAverage() {
		return this.dataList.stream().mapToDouble(data -> data.getValue().doubleValue()).average().orElse(0);
	}

	public final Number getMin() {
		final double min = this.dataList.stream().mapToDouble(data -> data.getValue().doubleValue()).min().orElse(0);
		final double max = this.dataList.stream().mapToDouble(data -> data.getValue().doubleValue()).max().orElse(0);
		return min == max ? 0 : min;
	}

	public final Number getMax() {
		final double min = this.dataList.stream().mapToDouble(data -> data.getValue().doubleValue()).min().orElse(0);
		final double max = this.dataList.stream().mapToDouble(data -> data.getValue().doubleValue()).max().orElse(0);
		return min == max ? max == 0 ? 1 : max * 2 : max;
	}

	public final <T extends RadarChartNode<DATA>> @NonNull T data(final @NonNull DATA data) {
		this.dataList.add(data);
		return (T) this;
	}

	@Getter
	public static class RadarChartData {

		private String label;
		private Number value;

		protected RadarChartData(final @NonNull String label) {
			this(label, null);
		}

		protected RadarChartData(final @NonNull String label, final Number value) {
			this.label = label;
			this.value = value;
		}

		public static @NonNull RadarChartData create(final @NonNull String label) {
			return new RadarChartData(label);
		}

		public static @NonNull RadarChartData create(final @NonNull String label, final Number value) {
			return new RadarChartData(label, value);
		}

		public boolean isEmpty() {
			return this.value == null;
		}

		public final <T extends RadarChartData> T value(final Number value) {
			this.value = value;
			return (T) this;
		}

		public final <T extends RadarChartData> T label(final String label) {
			this.label = label;
			return (T) this;
		}

		public final <T extends RadarChartData> @NonNull T clear() {
			this.value = null;
			return (T) this;
		}

	}

}