package example.replay;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.replay.SignalReplayNode;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class LabelNode extends SignalReplayNode {

	private Supplier<String> name;

	protected LabelNode() {
		super(0D, 0D);
	}

	public static @NonNull LabelNode create() {
		return new LabelNode();
	}

	public final <T extends LabelNode> @NonNull T name(final String name) {
		return this.name(Signal.from(name));
	}

	public final <T extends LabelNode> @NonNull T name(final @NonNull Supplier<String> name) {
		this.name = name;
		return (T) this;
	}

	public final <T extends LabelNode> @NonNull T title(final String title) {
		return this.name(title);
	}

	public final <T extends LabelNode> @NonNull T caption(final String caption) {
		return super.text(caption);
	}

}