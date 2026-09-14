package be.zeldown.joid.lib.ui.core.task;

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

		this.lastUpdate = System.currentTimeMillis();
		this.nextUpdate = this.lastUpdate + this.delay;
	}

	public boolean shouldRun() {
		return System.currentTimeMillis() >= this.nextUpdate;
	}

	public boolean execute() {
		this.task.run();
		if (!this.isPeriodic()) {
			return false;
		}

		this.lastUpdate = System.currentTimeMillis();
		this.nextUpdate = this.lastUpdate + this.period;
		return true;
	}

	public boolean isPeriodic() {
		return this.period >= 0;
	}

}