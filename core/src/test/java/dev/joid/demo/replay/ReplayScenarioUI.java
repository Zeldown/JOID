package dev.joid.demo.replay;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.replay.ReplayNode;
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
				ReplayNode.create().text("v" + this.clicks.get()).attach(this); ReplayNode.create().text("v" + Math.abs(this.clicks.get())).attach(this);
				break;
			case "unsupported":
				ReplayNode.create().text("v" + (this.last = this.clicks.get())).attach(this);
				break;
			case "lambda":
				ReplayNode.create().text("v" + this.apply(() -> 2) + this.clicks.get()).attach(this);
				break;
			case "combined":
				for (int index = 0; index < 1; index++) {
					ReplayNode.create().text("Row " + (this.clicks.get() + index)).attach(this);
				}
				break;
			case "condition":
				for (final String name : this.names) {
					ReplayNode.create().text(name.isEmpty() ? "none" : "n" + this.clicks.get()).attach(this);
				}
				break;
			case "differs":
				ReplayNode.create().text("n" + this.next() + " " + this.clicks.get()).attach(this);
				break;
			case "signals":
				ReplayNode.create().text("s" + this.alternate().get()).attach(this);
				break;
			case "access":
				ReplayNode.create().text("p" + this.secret() + this.clicks.get()).attach(this);
				break;
			case "later":
				ReplayNode.create().text("i" + this.names.get(this.clicks.get())).attach(this);
				break;
			case "silent":
				this.clicks.get();
				ReplayNode.create().text("plain").attach(this);
				ReplayNode.create().text(() -> "lambda " + this.clicks.get()).attach(this);
				break;
			case "body":
				ReplayNode.create().<ReplayNode>body(node -> node.text("Body " + this.clicks.get())).attach(this);
				break;
			case "update":
				ReplayNode.create().<ReplayNode>onUpdate(node -> {
					if (this.count++ == 0) {
						node.text("Update " + this.clicks.get());
					}
				}).attach(this);
				break;
			case "anonymous":
				new Runnable() {

					@Override
					public void run() {
						ReplayNode.create().text("Inner " + ReplayScenarioUI.this.clicks.get()).attach(ReplayScenarioUI.this);
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