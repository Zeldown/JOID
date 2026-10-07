package dev.joid.demo.replay;

import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.replay.SignalReplayNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayHiddenFixture {

	public static void run(final SignalReplayNode node, final IntegerSignal clicks) {
		node.text("Hidden " + clicks.get());
	}

}