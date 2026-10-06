package dev.joid.lib.utils.signal.replay;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.objectweb.asm.tree.AbstractInsnNode;

import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayBinding<T> implements Supplier<T> {

	private final ReplayCall                       call;
	private final ReplaySlice                      slice;
	private final Object                           self;
	private final boolean                          definitions;
	private final Map<String, String>              holeMap;
	private final Map<AbstractInsnNode, Signal<?>> positionalMap;
	private final List<Signal<?>>                  readList;
	private final long                             epoch;

	private T       value;
	private boolean started;
	private boolean warned;

	public static <T> ReplayBinding<T> create(final ReplayCall call, final ReplayMatch match, final List<Signal<?>> readList, final T value) {
		final ReplayBinding<T> binding = new ReplayBinding<>(call, match.getSlice(), match.getSelf(), match.isDefinitions(), match.getHoleMap(), match.getPositionalMap(), match.getReadList(readList), SignalContext.getEpoch());
		binding.value = value;
		return binding;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get() {
		if (!this.started) {
			this.started = true;
			if (this.epoch == SignalContext.getEpoch()) {
				final SignalContext context = SignalContext.current();
				for (final Signal<?> signal : this.readList) {
					context.read(signal);
				}
				return this.value;
			}
		}

		try {
			final Object result = ReplayRun.replay(this.self, this.definitions, this.holeMap, this.positionalMap).run(this.slice);
			if (result instanceof ReplayUnknown || result instanceof ReplayParts) {
				throw new ReplayException(ReplayFailure.REPLAY_FAILED, "a local variable is now used in another way");
			}
			this.value = (T) this.slice.convert(result);
		} catch (final ReplayException exception) {
			if (!this.warned) {
				this.warned = true;
				SignalReplay.warn(this.call, this.slice.getSignalNameList(), this.readList.size(), exception.getFailure() == ReplayFailure.REPLAY_FAILED ? exception : new ReplayException(ReplayFailure.REPLAY_FAILED, exception.getMessage()), this.value);
			}
		}
		return this.value;
	}

}