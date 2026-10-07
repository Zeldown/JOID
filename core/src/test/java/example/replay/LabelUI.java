package example.replay;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.replay.ReplayNode;
import lombok.Getter;

@Getter
public class LabelUI extends UI {

	private final IntegerSignal clicks = IntegerSignal.of(0);

	@Override
	public void init() {
		LabelNode.create().label("Label " + this.clicks.get()).attach(this);
		LabelNode.create().title("Title " + this.clicks.get()).attach(this);
		LabelNode.create().caption("Caption " + this.clicks.get()).attach(this);
		ReplayNode.titled("Titled " + this.clicks.get()).attach(this);
		ReplayNode.create().prefixed("Prefixed " + this.clicks.get()).attach(this);
		LabelNode.create().label(Signal.from("Direct " + this.clicks.get())).attach(this);
	}

}