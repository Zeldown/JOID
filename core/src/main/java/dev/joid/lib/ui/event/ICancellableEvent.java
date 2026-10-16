package dev.joid.lib.ui.event;

public interface ICancellableEvent {

	public void cancel();
	public boolean isCancelled();

}