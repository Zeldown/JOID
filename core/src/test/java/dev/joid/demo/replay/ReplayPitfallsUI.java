package dev.joid.demo.replay;

import java.util.Arrays;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.replay.ReplayNode;
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
		ReplayNode.create().text("Next: " + (this.clicks.get() + 1) + ", double: " + this.clicks.get() * 2).attach(this);
		ReplayNode.create().color(this.clicks.get() >= 3 ? Color.GREEN : Color.GRAY).attach(this);
		ReplayNode.create().shown(this.clicks.get() > 0).attach(this);
		ReplayNode.create().text("Clicks + bonus: " + (this.clicks.get() + this.bonus)).attach(this);
		for (final String name : this.names) {
			ReplayNode.create().text(name.toUpperCase() + " has " + this.clicks.get() + " clicks").attach(this);
		}
		final int offset = this.bonus / 10;
		ReplayNode.create().text("Clicks + offset: " + (this.clicks.get() + offset)).attach(this);
		final int doubled = this.clicks.get() * 2;
		ReplayNode.create().text("Doubled: " + doubled).attach(this);
		ReplayNode.create().text("A " + this.clicks.get()).attach(this); ReplayNode.create().text("B " + this.other.get()).attach(this);
		ReplayNode.create().text("" + this.clicks.get()).attach(this); ReplayNode.create().text("" + this.other.get()).attach(this);
		(this.compact ? ReplayNode.create().text("Skip A " + this.clicks.get()) : ReplayNode.create().text("Skip B " + this.other.get())).attach(this);
		ReplayNode.create().text(() -> "Frame " + this.frame + ", clicks " + this.clicks.get()).attach(this);
		ReplayNode.create().extent(this.clicks.get() * 10D, this.other.get() + 5D).attach(this);
		RectNode.create(0D, 0D, 10D, 10D).color(Signal.from(this.clicks.get() >= 3 ? Color.GREEN : Color.GRAY)).attach(this);
	}

}