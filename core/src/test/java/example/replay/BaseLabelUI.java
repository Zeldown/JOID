package example.replay;

import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;

@Getter
public abstract class BaseLabelUI extends UI {

	private final IntegerSignal clicks = IntegerSignal.create(0);

	protected String label() {
		return "a";
	}

	protected String prefix() {
		return "p";
	}

}