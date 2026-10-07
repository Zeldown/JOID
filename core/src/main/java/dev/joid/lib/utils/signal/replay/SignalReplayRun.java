package dev.joid.lib.utils.signal.replay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public final class SignalReplayRun {

	private final Object                 self;
	private final boolean                matching;
	private final boolean                definitions;
	private final Map<String, String>    holeMap;
	private final List<Signal<?>>        positionalList;
	private final Map<Object, Signal<?>> positionalMap;

	private final List<Group>                      groupList;
	private final List<Signal<?>>                  directList;
	private final Map<SignalReplaySlice, Object[]> definitionCache;

	private boolean         positionalNeeded;
	private List<Signal<?>> currentList;

	private SignalReplayRun(final Object self, final boolean definitions, final boolean matching, final Map<String, String> holeMap, final List<Signal<?>> positionalList, final Map<Object, Signal<?>> positionalMap) {
		this.self            = self;
		this.definitions     = definitions;
		this.matching        = matching;
		this.holeMap         = holeMap;
		this.positionalList  = positionalList;
		this.positionalMap   = positionalMap;
		this.directList      = new ArrayList<>();
		this.groupList       = new ArrayList<>();
		this.definitionCache = new IdentityHashMap<>();
		this.currentList     = this.directList;
	}

	public static SignalReplayRun match(final Object self, final boolean definitions, final List<Signal<?>> positionalList) {
		return new SignalReplayRun(self, definitions, true, null, positionalList, new HashMap<>());
	}

	public static SignalReplayRun replay(final Object self, final boolean definitions, final Map<String, String> holeMap, final Map<Object, Signal<?>> positionalMap) {
		return new SignalReplayRun(self, definitions, false, holeMap, null, positionalMap);
	}

	public Object run(final SignalReplaySlice slice) {
		final Object value = SignalReplayFrame.create(this, slice, 0).execute();
		this.flush();
		return value;
	}

	public Object[] define(final SignalReplaySlice slice, final int depth) {
		if (this.definitionCache.containsKey(slice)) {
			return this.definitionCache.get(slice);
		}

		this.flush();
		final List<Signal<?>> previousList = this.currentList;
		final List<Signal<?>> readList = new ArrayList<>();
		this.currentList = readList;
		Object[] result;
		try {
			final SignalReplayFrame frame = SignalReplayFrame.create(this, slice, depth);
			final Object value = frame.execute();
			this.flush();
			result = value instanceof SignalReplayUnknown || value instanceof SignalReplayParts ? null : new Object[] {value, frame.isLastLive()};
		} catch (final SignalReplayException exception) {
			this.flush();
			result = null;
		} finally {
			this.currentList = previousList;
		}

		if (result != null) {
			this.groupList.add(new Group(slice.getEnd(), readList));
		}
		this.definitionCache.put(slice, result);
		return result;
	}

	public Signal<?> position(final Object key, final Class<?> owner) {
		if (!this.matching) {
			final Signal<?> signal = this.positionalMap.get(key);
			if (signal == null) {
				throw new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
			}
			return signal;
		}

		if (this.positionalList == null) {
			this.positionalNeeded = true;
			throw new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
		}

		this.flush();
		final int position = this.directList.size();
		if (this.currentList != this.directList || position >= this.positionalList.size() || !owner.isInstance(this.positionalList.get(position))) {
			throw new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
		}

		final Signal<?> signal = this.positionalList.get(position);
		final Signal<?> previous = this.positionalMap.put(key, signal);
		if (previous != null && previous != signal) {
			throw new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
		}
		return signal;
	}

	public List<Signal<?>> getDefinitionList() {
		final List<Group> sortedList = new ArrayList<>(this.groupList);
		sortedList.sort(Comparator.comparingInt(group -> group.store));
		final List<Signal<?>> readList = new ArrayList<>();
		for (final Group group : sortedList) {
			readList.addAll(group.readList);
		}
		return readList;
	}

	private void flush() {
		if (this.matching) {
			this.currentList.addAll(SignalContext.current().takeReads());
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Group {

		private final int             store;
		private final List<Signal<?>> readList;

	}

}