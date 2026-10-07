package dev.joid.demo.replay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.signal.ComputedSignal;
import dev.joid.lib.utils.signal.impl.iterable.ListSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.replay.SignalReplayCaption;
import dev.joid.lib.utils.signal.replay.SignalReplayNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReplayLocalUI extends UI {

	private final IntegerSignal            other      = IntegerSignal.of(100);
	private final List<IntegerSignal>      signalList = new ArrayList<>();
	private final List<ListSignal<String>> listList   = new ArrayList<>();

	private final String scenario;

	private int created;

	@Override
	public void init() {
		switch (this.scenario) {
			case "simple": {
				final IntegerSignal clicks = this.create(0);
				SignalReplayNode.create().text("Clicks: " + clicks.get()).attach(this);
				break;
			}
			case "direct": {
				final IntegerSignal clicks = ReplayCountedSignal.create(0);
				this.signalList.add(clicks);
				SignalReplayNode.create().text("Direct " + clicks.get()).attach(this);
				break;
			}
			case "several": {
				final IntegerSignal left = this.create(1);
				final IntegerSignal right = this.create(2);
				final ComputedSignal<Integer> sum = left.map(value -> value + 10);
				SignalReplayNode.create().text(left.get() + " / " + right.get()).attach(this);
				SignalReplayNode.create().text("Right " + right.get() + ", left " + left.get()).attach(this);
				SignalReplayNode.create().text("Sum " + sum.get()).attach(this);
				break;
			}
			case "field": {
				final IntegerSignal clicks = this.create(1);
				SignalReplayNode.create().text("Total " + (clicks.get() + this.other.get())).attach(this);
				break;
			}
			case "body": {
				final IntegerSignal clicks = this.create(0);
				SignalReplayNode.create().<SignalReplayNode>body(node -> node.text("Body " + clicks.get())).attach(this);
				break;
			}
			case "branch": {
				final ListSignal<String> names = new ListSignal<>(Collections.emptyList());
				this.listList.add(names);
				SignalReplayNode.create().text(names.isEmpty() ? "none" : names.get(0) + " and " + names.get(0)).attach(this);
				break;
			}
			case "subscribed": {
				final IntegerSignal sent = this.create(0);
				final IntegerSignal saved = this.create(0);
				sent.subscribe(value -> {
					saved.set(value);
					return true;
				});
				SignalReplayNode.create().<SignalReplayNode>body(node -> node.text("Saved: " + saved.get())).attach(this);
				break;
			}
			case "stale": {
				final IntegerSignal saved = this.create(0);
				final ListSignal<String> names = new ListSignal<>(Collections.singletonList("Ada"));
				for (final String name : names.get()) {
					SignalReplayNode.create().text(name).attach(this);
				}
				SignalReplayNode.create().caption(SignalReplayCaption.create("Saved " + saved.get())).attach(this);
				break;
			}
			case "loop": {
				for (int index = 0; index < 3; index++) {
					final IntegerSignal row = this.create(index);
					SignalReplayNode.create().text("Row " + row.get()).attach(this);
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