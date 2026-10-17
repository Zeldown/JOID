package dev.joid.lib.signal.replay;

import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplayBinding<T> implements Supplier<T> {

	private final SignalReplayCall  call;
	private final SignalReplayMatch match;
	private final List<Signal<?>>   readList;
	private final long              epoch;

	private T       value;
	private boolean started;

	public static <T> SignalReplayBinding<T> create(final SignalReplayCall call, final SignalReplayMatch match, final List<Signal<?>> consumedList, final T value) {
		final SignalReplayBinding<T> binding = new SignalReplayBinding<>(call, match, consumedList, SignalContext.getEpoch());
		binding.value = value;
		return binding;
	}

	@Override
	public T get() {
		if (!this.started) {
			this.started = true;
			if (this.epoch == SignalContext.getEpoch()) {
				this.readKnown();
				return this.value;
			}
		}

		try {
			final Object result = SignalReplayRun.replay(this.match.getSelf(), this.match.isDefinitions(), this.match.getHoleMap(), this.match.getPositionalMap()).run(this.match.getSlice());
			if (result instanceof SignalReplayUnknown || result instanceof SignalReplayParts) {
				throw new SignalReplayException(SignalReplayFailure.REPLAY_FAILED, "a local variable is now used in another way");
			}
			this.value = (T) this.match.getSlice().convert(result);
		} catch (final SignalReplayException exception) {
			this.readKnown();
			SignalReplay.warn(this.call, this.match.getSlice().getSignalNameList(), this.readList.size(), exception.getFailure() == SignalReplayFailure.REPLAY_FAILED ? exception : new SignalReplayException(SignalReplayFailure.REPLAY_FAILED, exception.getMessage()), this.value);
		}
		return this.value;
	}

	private void readKnown() {
		final SignalContext context = SignalContext.current();
		for (final Signal<?> signal : this.readList) {
			context.read(signal);
		}
	}

}