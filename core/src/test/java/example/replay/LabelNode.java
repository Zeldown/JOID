package example.replay;

import java.util.function.Supplier;

import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.replay.SignalReplayNode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class LabelNode extends SignalReplayNode {

	private Supplier<String> label;

	protected LabelNode() {
		super(0D, 0D);
	}

	public static @NonNull LabelNode create() {
		return new LabelNode();
	}

	public final <T extends LabelNode> @NonNull T label(final String label) {
		return this.label(Signal.from(label));
	}

	public final <T extends LabelNode> @NonNull T label(final @NonNull Supplier<String> label) {
		this.label = label;
		return (T) this;
	}

	public final <T extends LabelNode> @NonNull T title(final String title) {
		return this.label(title);
	}

	public final <T extends LabelNode> @NonNull T caption(final String caption) {
		return super.text(caption);
	}

}