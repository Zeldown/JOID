package dev.joid.demo.replay;

import java.util.Arrays;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.replay.SignalReplayNode;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplayPitfallsUI extends UI {

	private final IntegerSignal clicks = IntegerSignal.of(0);
	private final IntegerSignal other  = IntegerSignal.of(0);
	private final List<String>  names  = Arrays.asList("Ada", "Grace");

	private int     frame   = 0;
	private int     bonus   = 100;
	private boolean compact = true;

	@Override
	public void init() {
		SignalReplayNode.create().text("Next: " + (this.clicks.get() + 1) + ", double: " + this.clicks.get() * 2).attach(this);
		SignalReplayNode.create().color(this.clicks.get() >= 3 ? Color.GREEN : Color.GRAY).attach(this);
		SignalReplayNode.create().shown(this.clicks.get() > 0).attach(this);
		SignalReplayNode.create().text("Clicks + bonus: " + (this.clicks.get() + this.bonus)).attach(this);
		for (final String name : this.names) {
			SignalReplayNode.create().text(name.toUpperCase() + " has " + this.clicks.get() + " clicks").attach(this);
		}
		final int offset = this.bonus / 10;
		SignalReplayNode.create().text("Clicks + offset: " + (this.clicks.get() + offset)).attach(this);
		final int doubled = this.clicks.get() * 2;
		SignalReplayNode.create().text("Doubled: " + doubled).attach(this);
		SignalReplayNode.create().text("A " + this.clicks.get()).attach(this); SignalReplayNode.create().text("B " + this.other.get()).attach(this);
		SignalReplayNode.create().text("" + this.clicks.get()).attach(this); SignalReplayNode.create().text("" + this.other.get()).attach(this);
		(this.compact ? SignalReplayNode.create().text("Skip A " + this.clicks.get()) : SignalReplayNode.create().text("Skip B " + this.other.get())).attach(this);
		SignalReplayNode.create().text(() -> "Frame " + this.frame + ", clicks " + this.clicks.get()).attach(this);
		SignalReplayNode.create().extent(this.clicks.get() * 10D, this.other.get() + 5D).attach(this);
		RectNode.create(0D, 0D, 10D, 10D).color(Signal.from(this.clicks.get() >= 3 ? Color.GREEN : Color.GRAY)).attach(this);
	}

}