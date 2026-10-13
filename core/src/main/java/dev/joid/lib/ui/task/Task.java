package dev.joid.lib.ui.task;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Task {

	private final long                      delay;
	private final List<Consumer<TaskEvent>> runList;

	private long period;
	private long nextRun;

	private UI   ui;
	private Node node;

	private Task(final long delay) {
		this.delay = delay;
		this.period = -1L;
		this.runList = new CopyOnWriteArrayList<>();
	}

	public static @NonNull Task create(final long delay) {
		return new Task(delay);
	}

	public @NonNull Task every(final long period) {
		this.period = period;
		return this;
	}

	public @NonNull Task onRun(final @NonNull Consumer<@NonNull TaskEvent> listener) {
		this.runList.add(listener);
		return this;
	}

	public void fireRun() {
		if (this.period < 0L) {
			this.detach();
		} else {
			this.nextRun = BridgeHandler.CLOCK.get().currentTimeMillis() + this.period;
		}

		final TaskEvent event = new TaskEvent(this);
		this.runList.forEach(listener -> listener.accept(event));
	}

	public @NonNull Task attach(final @NonNull UI ui) {
		this.detach();
		this.ui = ui;
		this.nextRun = BridgeHandler.CLOCK.get().currentTimeMillis() + this.delay;
		ui.getTaskList().add(this);
		return this;
	}

	public @NonNull Task attach(final @NonNull Node node) {
		this.detach();
		this.node = node;
		this.nextRun = BridgeHandler.CLOCK.get().currentTimeMillis() + this.delay;
		node.getTaskList().add(this);
		return this;
	}

	public @NonNull Task detach() {
		if (this.ui != null) {
			this.ui.getTaskList().remove(this);
			this.ui = null;
		}

		if (this.node != null) {
			this.node.getTaskList().remove(this);
			this.node = null;
		}
		return this;
	}

	public boolean isDue() {
		return this.isAttached() && BridgeHandler.CLOCK.get().currentTimeMillis() >= this.nextRun;
	}

	public boolean isAttached() {
		return this.ui != null || this.node != null;
	}

}