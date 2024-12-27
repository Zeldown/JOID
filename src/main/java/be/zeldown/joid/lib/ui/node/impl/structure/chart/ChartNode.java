package be.zeldown.joid.lib.ui.node.impl.structure.chart;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartAxis.XChartAxis;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartAxis.YChartAxis;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class ChartNode extends Node {

	private XChartAxis xAxis;
	private YChartAxis yAxis;

	protected ChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public final void drawSkeleton(final double mouseX, final double mouseY) {
		super.draw(mouseX, mouseY);
	}

	/* [ ChartData ] */
	public final boolean isLoaded() {
		return this.isMounted() && this.xAxis != null && this.yAxis != null && !this.getDataMap().isEmpty() && !this.getDataMap().values().stream().anyMatch(ChartData::isEmpty);
	}

	public final @NonNull Set<String> getLabels() {
		return this.xAxis.getLabelSet();
	}

	public final @NonNull Map<String, ChartData> getDataMap() {
		return this.xAxis.getDataMap();
	}

	public final ChartData getData(final String data) {
		return this.xAxis.getDataMap().get(data);
	}

	public final Number getAverage(final String data) {
		return this.getData(data).getAverage();
	}

	public final Number getAverage() {
		return this.getDataMap().values().stream().mapToDouble(daya -> daya.getAverage().doubleValue()).average().orElse(0);
	}

	public final Number getMin(final String data) {
		final double min = this.getData(data).getMin().doubleValue();
		final double max = this.getData(data).getMax().doubleValue();
		return min == max ? 0 : min;
	}

	public final Number getMin() {
		final double min = this.getDataMap().values().stream().mapToDouble(data -> data.getMin().doubleValue()).min().orElse(0);
		final double max = this.getDataMap().values().stream().mapToDouble(data -> data.getMax().doubleValue()).max().orElse(0);
		return min == max ? 0 : min;
	}

	public final Number getMax(final String data) {
		final double min = this.getData(data).getMin().doubleValue();
		final double max = this.getData(data).getMax().doubleValue();
		return min == max ? max == 0 ? 1 : max * 2 : max;
	}

	public final Number getMax() {
		final double min = this.getDataMap().values().stream().mapToDouble(data -> data.getMin().doubleValue()).min().orElse(0);
		final double max = this.getDataMap().values().stream().mapToDouble(data -> data.getMax().doubleValue()).max().orElse(0);
		return min == max ? max == 0 ? 1 : max * 2 : max;
	}

	/* [ ChartAxis ] */
	public final <T extends ChartNode> @NonNull T axis(final @NonNull XChartAxis x, final @NonNull YChartAxis y) {
		this.xAxis = x;
		this.yAxis = y;
		return (T) this;
	}

	public final <T extends ChartNode> @NonNull T axis(final @NonNull XChartAxis x) {
		this.xAxis = x;
		return (T) this;
	}

	public final <T extends ChartNode> @NonNull T axis(final @NonNull YChartAxis y) {
		this.yAxis = y;
		return (T) this;
	}

	public final <T extends ChartNode> @NonNull T data(final @NonNull String dataName, final @NonNull ChartData data) {
		if (this.xAxis == null) {
			throw new IllegalStateException("You must set the X axis before adding data");
		}

		this.xAxis.data(dataName, data);
		return (T) this;
	}

	public final <T extends ChartNode> @NonNull T remove(final @NonNull String data) {
		if (this.xAxis == null) {
			throw new IllegalStateException("You must set the X axis before removing data");
		}

		this.xAxis.remove(data);
		return (T) this;
	}

	public static class ChartAxis {

		private final String name;

		protected ChartAxis(final @NonNull String name) {
			this.name = name;
		}

		public static @NonNull XChartAxis x(final @NonNull String name, final @NonNull String... labels) {
			return new XChartAxis(name, labels);
		}

		public static @NonNull YChartAxis y(final @NonNull String name) {
			return new YChartAxis(name);
		}

		public String getName() {
			return this.name;
		}

		@Getter
		public static final class XChartAxis extends ChartAxis {

			private final Set<String> labelSet;
			private final Map<String, ChartData> dataMap;

			protected XChartAxis(final @NonNull String name, final @NonNull String... labels) {
				super(name);

				this.labelSet = new LinkedHashSet<>();
				Collections.addAll(this.labelSet, labels);
				this.dataMap = new HashMap<>();
			}

			public final ChartData get(final @NonNull String data) {
				return this.dataMap.get(data);
			}

			public final @NonNull XChartAxis labelSet(final @NonNull String... labels) {
				this.labelSet.clear();
				Collections.addAll(this.labelSet, labels);
				return this;
			}

			public final @NonNull XChartAxis labelSet(final @NonNull Set<@NonNull String> labelSet) {
				if (!(labelSet instanceof LinkedHashSet)) {
					throw new IllegalArgumentException("labelSet must be an instance of LinkedHashSet");
				}

				this.labelSet.clear();
				this.labelSet.addAll(labelSet);
				return this;
			}

			public final @NonNull XChartAxis data(final @NonNull String dataName, final @NonNull ChartData data) {
				this.dataMap.put(dataName, data);
				return this;
			}

			public final @NonNull XChartAxis remove(final @NonNull String data) {
				this.dataMap.remove(data);
				return this;
			}

		}

		@Getter
		public static final class YChartAxis extends ChartAxis {

			private String prefix;
			private String suffix;

			protected YChartAxis(final @NonNull String name) {
				super(name);
			}

			public final @NonNull YChartAxis prefix(final @NonNull String prefix) {
				this.prefix = prefix;
				return this;
			}

			public final @NonNull YChartAxis suffix(final @NonNull String suffix) {
				this.suffix = suffix;
				return this;
			}

		}

	}

	@Getter
	public static class ChartData {

		private Map<String, Number> dataMap;

		protected ChartData() {
			this(new HashMap<>());
		}

		protected ChartData(final Map<@NonNull String, @NonNull Number> dataMap) {
			this.dataMap = dataMap;
		}

		public static @NonNull ChartData create() {
			return new ChartData();
		}

		public static @NonNull ChartData create(final Map<@NonNull String, @NonNull Number> dataMap) {
			return new ChartData(dataMap);
		}

		public boolean isEmpty() {
			return this.dataMap == null || this.dataMap.isEmpty();
		}

		public Number get(final String label) {
			return this.dataMap.get(label);
		}

		public boolean has(final String label) {
			return this.dataMap.containsKey(label);
		}

		public Number getAverage() {
			return this.dataMap.values().stream().mapToDouble(Number::doubleValue).average().orElse(0);
		}

		public Number getMin() {
			return this.dataMap.values().stream().mapToDouble(Number::doubleValue).min().orElse(0);
		}

		public Number getMax() {
			return this.dataMap.values().stream().mapToDouble(Number::doubleValue).max().orElse(0);
		}

		public final <T extends ChartData> T add(final @NonNull String label, final Number data) {
			this.dataMap.put(label, data);
			return (T) this;
		}

		public final <T extends ChartData> @NonNull T remove(final @NonNull String label) {
			this.dataMap.remove(label);
			return (T) this;
		}

		public final <T extends ChartData> @NonNull T dataMap(final Map<@NonNull String, @NonNull Number> dataMap) {
			this.dataMap = dataMap;
			return (T) this;
		}

	}

}