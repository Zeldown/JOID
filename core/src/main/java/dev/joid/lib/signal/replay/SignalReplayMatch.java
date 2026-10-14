package dev.joid.lib.signal.replay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.joid.lib.signal.BaseSignal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplayMatch {

	private final SignalReplaySlice      slice;
	private final Object                 self;
	private final boolean                definitions;
	private final Map<String, String>    holeMap;
	private final Map<Object, BaseSignal<?>> positionalMap;
	private final boolean[]              consumed;

	public static SignalReplayMatch create(final SignalReplaySlice slice, final SignalReplayRun run, final Map<String, String> holeMap, final boolean[] consumed) {
		return new SignalReplayMatch(slice, run.getSelf(), run.isDefinitions(), holeMap, run.getPositionalMap(), consumed);
	}

	public List<BaseSignal<?>> getReadList(final List<BaseSignal<?>> readList) {
		final List<BaseSignal<?>> consumedList = new ArrayList<>();
		for (int index = 0; index < readList.size(); index++) {
			if (this.consumed[index]) {
				consumedList.add(readList.get(index));
			}
		}
		return consumedList;
	}

	public List<BaseSignal<?>> getRemainingList(final List<BaseSignal<?>> readList) {
		final List<BaseSignal<?>> remainingList = new ArrayList<>();
		for (int index = 0; index < readList.size(); index++) {
			if (!this.consumed[index]) {
				remainingList.add(readList.get(index));
			}
		}
		return remainingList;
	}

	public Map<String, String> getHoleMap() {
		return this.holeMap != null ? Collections.unmodifiableMap(this.holeMap) : null;
	}

	public Map<Object, BaseSignal<?>> getPositionalMap() {
		return Collections.unmodifiableMap(this.positionalMap);
	}

}