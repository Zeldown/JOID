package dev.joid.internal.signal.replay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.joid.lib.signal.Signal;
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
	private final Map<Object, Signal<?>> positionalMap;
	private final boolean[]              consumed;
	private final boolean                followed;

	public static SignalReplayMatch of(final SignalReplaySlice slice, final SignalReplayRun run, final Map<String, String> holeMap, final boolean[] consumed, final boolean followed) {
		return new SignalReplayMatch(slice, run.getSelf(), run.isDefinitions(), holeMap, run.getPositionalMap(), consumed, followed);
	}

	public List<Signal<?>> getReadList(final List<Signal<?>> readList) {
		final List<Signal<?>> selectedList = new ArrayList<>();
		for (int index = 0; index < readList.size(); index++) {
			if (this.consumed[index]) {
				selectedList.add(readList.get(index));
			}
		}
		return selectedList;
	}

}