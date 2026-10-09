package dev.joid.demo.replay;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.replay.SignalReplayNode;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReplayScenarioUI extends UI {

	private final IntegerSignal clicks = IntegerSignal.of(0);
	private final IntegerSignal other  = IntegerSignal.of(0);
	private final List<String>  names  = Collections.singletonList("Ada");

	private final String scenario;

	private int     last;
	private int     count;
	private boolean flip;

	@Override
	public void init() {
		switch (this.scenario) {
			case "ambiguous":
				SignalReplayNode.create().text("v" + this.clicks.get()).attach(this); SignalReplayNode.create().text("v" + Math.abs(this.clicks.get())).attach(this);
				break;
			case "unsupported":
				SignalReplayNode.create().text("v" + (this.last = this.clicks.get())).attach(this);
				break;
			case "lambda":
				SignalReplayNode.create().text("v" + this.apply(() -> 2) + this.clicks.get()).attach(this);
				break;
			case "combined":
				for (int index = 0; index < 1; index++) {
					SignalReplayNode.create().text("Row " + (this.clicks.get() + index)).attach(this);
				}
				break;
			case "condition":
				for (final String name : this.names) {
					SignalReplayNode.create().text(name.isEmpty() ? "none" : "n" + this.clicks.get()).attach(this);
				}
				break;
			case "differs":
				SignalReplayNode.create().text("n" + this.next() + " " + this.clicks.get()).attach(this);
				break;
			case "signals":
				SignalReplayNode.create().text("s" + this.alternate().get()).attach(this);
				break;
			case "access":
				SignalReplayNode.create().text("p" + this.secret() + this.clicks.get()).attach(this);
				break;
			case "later":
				SignalReplayNode.create().text("i" + this.names.get(this.clicks.get())).attach(this);
				break;
			case "silent":
				this.clicks.get();
				SignalReplayNode.create().text("plain").attach(this);
				SignalReplayNode.create().text(() -> "lambda " + this.clicks.get()).attach(this);
				break;
			case "body":
				SignalReplayNode.create().<SignalReplayNode>body(node -> node.text("Body " + this.clicks.get())).attach(this);
				break;
			case "update":
				SignalReplayNode.create().<SignalReplayNode>onUpdate(node -> {
					if (this.count++ == 0) {
						node.text("Update " + this.clicks.get());
					}
				}).attach(this);
				break;
			case "anonymous":
				new Runnable() {

					@Override
					public void run() {
						SignalReplayNode.create().text("Inner " + ReplayScenarioUI.this.clicks.get()).attach(ReplayScenarioUI.this);
					}

				}.run();
				break;
			default:
				break;
		}
	}

	private int apply(final Supplier<Integer> supplier) {
		return supplier.get();
	}

	private int next() {
		return ++this.count;
	}

	private int secret() {
		return 7;
	}

	private Signal<Integer> alternate() {
		this.flip = !this.flip;
		return this.flip ? this.clicks : this.other;
	}

}