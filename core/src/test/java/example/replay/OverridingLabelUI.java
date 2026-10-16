package example.replay;

import dev.joid.lib.signal.replay.SignalReplayNode;

public class OverridingLabelUI extends BaseLabelUI {

	@Override
	public void init() {
		SignalReplayNode.create().text(super.label() + super.getClicks().get()).attach(this);
		SignalReplayNode.create().text(super.prefix() + super.getClicks().get()).attach(this);
	}

	@Override
	protected String label() {
		return "b";
	}

}