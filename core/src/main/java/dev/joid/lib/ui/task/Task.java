package dev.joid.lib.ui.task;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Task {

	private static final EventType<TaskEvent> RUN = EventType.create("onRun");

	private final long     delay;
	private final EventBus eventBus;

	private long period;
	private long nextRun;

	private UI   ui;
	private Node node;

	private Task(final long delay) {
		this.delay = delay;
		this.period = -1L;
		this.eventBus = EventBus.create(() -> "Task");
	}

	public static @NonNull Task create(final long delay) {
		return new Task(delay);
	}

	public @NonNull Task every(final long period) {
		this.period = period;
		return this;
	}

	public @NonNull Task onRun(final @NonNull EventListener<TaskEvent> listener) {
		this.eventBus.listen(Task.RUN, listener);
		return this;
	}

	public void fireRun() {
		if (this.period < 0L) {
			this.detach();
		} else {
			this.nextRun = BridgeHandler.CLOCK.get().nanoTime() + this.period * 1_000_000L;
		}

		if (this.eventBus.has(Task.RUN)) {
			this.eventBus.fire(Task.RUN, TaskEvent.create(this));
		}
	}

	public @NonNull Task attach(final @NonNull UI ui) {
		ui.addTask(this);
		return this;
	}

	public @NonNull Task attach(final @NonNull Node node) {
		node.addTask(this);
		return this;
	}

	public @NonNull Task detach() {
		if (this.ui != null) {
			this.ui.removeTask(this);
		}

		if (this.node != null) {
			this.node.removeTask(this);
		}
		return this;
	}

	public @NonNull Task owner(final UI ui, final Node node) {
		this.ui = ui;
		this.node = node;
		if (this.isAttached()) {
			this.nextRun = BridgeHandler.CLOCK.get().nanoTime() + this.delay * 1_000_000L;
		}
		return this;
	}

	public boolean isDue() {
		return this.isAttached() && BridgeHandler.CLOCK.get().nanoTime() - this.nextRun >= 0L;
	}

	public boolean isAttached() {
		return this.ui != null || this.node != null;
	}

}