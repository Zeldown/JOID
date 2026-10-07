package dev.joid.demo.replay;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.signal.ComputedSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.replay.ReplayNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReplayLocalUI extends UI {

	private final IntegerSignal       other      = IntegerSignal.of(100);
	private final List<IntegerSignal> signalList = new ArrayList<>();

	private final String scenario;

	private int created;

	@Override
	public void init() {
		switch (this.scenario) {
			case "simple": {
				final IntegerSignal clicks = this.create(0);
				ReplayNode.create().text("Clicks: " + clicks.get()).attach(this);
				break;
			}
			case "direct": {
				final IntegerSignal clicks = ReplayCountedSignal.create(0);
				this.signalList.add(clicks);
				ReplayNode.create().text("Direct " + clicks.get()).attach(this);
				break;
			}
			case "several": {
				final IntegerSignal left = this.create(1);
				final IntegerSignal right = this.create(2);
				final ComputedSignal<Integer> sum = left.map(value -> value + 10);
				ReplayNode.create().text(left.get() + " / " + right.get()).attach(this);
				ReplayNode.create().text("Right " + right.get() + ", left " + left.get()).attach(this);
				ReplayNode.create().text("Sum " + sum.get()).attach(this);
				break;
			}
			case "field": {
				final IntegerSignal clicks = this.create(1);
				ReplayNode.create().text("Total " + (clicks.get() + this.other.get())).attach(this);
				break;
			}
			case "body": {
				final IntegerSignal clicks = this.create(0);
				ReplayNode.create().<ReplayNode>body(node -> node.text("Body " + clicks.get())).attach(this);
				break;
			}
			case "loop": {
				for (int index = 0; index < 3; index++) {
					final IntegerSignal row = this.create(index);
					ReplayNode.create().text("Row " + row.get()).attach(this);
				}
				break;
			}
			default:
				break;
		}
	}

	private IntegerSignal create(final int value) {
		this.created++;
		final IntegerSignal signal = IntegerSignal.of(value);
		this.signalList.add(signal);
		return signal;
	}

}