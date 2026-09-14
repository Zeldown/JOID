package be.zeldown.joid.lib.ui.core.task;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import lombok.Getter;

@Getter
public class UIScheduledTask {

	private final Runnable task;
	private final long delay;
	private final long period;

	private long lastUpdate;
	private long nextUpdate;

	public UIScheduledTask(final Runnable task, final long delay, final long period) {
		this.task = task;
		this.delay = delay;
		this.period = period;

		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.nextUpdate = this.lastUpdate + this.delay;
	}

	public boolean shouldRun() {
		return BridgeHandler.CLOCK.get().currentTimeMillis() >= this.nextUpdate;
	}

	public boolean execute() {
		this.task.run();
		if (!this.isPeriodic()) {
			return false;
		}

		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.nextUpdate = this.lastUpdate + this.period;
		return true;
	}

	public boolean isPeriodic() {
		return this.period >= 0;
	}

}