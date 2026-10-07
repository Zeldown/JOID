package dev.joid.lib.utils.signal.replay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.joid.lib.utils.signal.Signal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayMatch {

	private final ReplaySlice                      slice;
	private final Object                           self;
	private final boolean                          definitions;
	private final Map<String, String>              holeMap;
	private final Map<Object, Signal<?>>           positionalMap;
	private final boolean[]                        consumed;

	public static ReplayMatch create(final ReplaySlice slice, final ReplayRun run, final Map<String, String> holeMap, final boolean[] consumed) {
		return new ReplayMatch(slice, run.getSelf(), run.isDefinitions(), holeMap, run.getPositionalMap(), consumed);
	}

	public List<Signal<?>> getReadList(final List<Signal<?>> readList) {
		final List<Signal<?>> consumedList = new ArrayList<>();
		for (int index = 0; index < readList.size(); index++) {
			if (this.consumed[index]) {
				consumedList.add(readList.get(index));
			}
		}
		return consumedList;
	}

	public List<Signal<?>> getRemainingList(final List<Signal<?>> readList) {
		final List<Signal<?>> remainingList = new ArrayList<>();
		for (int index = 0; index < readList.size(); index++) {
			if (!this.consumed[index]) {
				remainingList.add(readList.get(index));
			}
		}
		return remainingList;
	}

}