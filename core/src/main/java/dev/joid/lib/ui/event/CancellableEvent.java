package dev.joid.lib.ui.event;

public interface CancellableEvent {

	public void cancel();
	public boolean isCancelled();

}