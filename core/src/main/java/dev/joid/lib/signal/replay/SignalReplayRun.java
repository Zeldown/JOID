package dev.joid.lib.signal.replay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import dev.joid.lib.signal.BaseSignal;
import dev.joid.lib.signal.SignalContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public final class SignalReplayRun {

	private final Object                 self;
	private final boolean                matching;
	private final boolean                definitions;
	private final Map<String, String>    holeMap;
	private final List<BaseSignal<?>>        positionalList;
	private final Map<Object, BaseSignal<?>> positionalMap;

	private final List<Group>                      groupList;
	private final List<BaseSignal<?>>                  directList;
	private final Map<SignalReplaySlice, Object[]> definitionCache;

	private boolean         positionalNeeded;
	private List<BaseSignal<?>> currentList;

	private SignalReplayRun(final Object self, final boolean definitions, final boolean matching, final Map<String, String> holeMap, final List<BaseSignal<?>> positionalList, final Map<Object, BaseSignal<?>> positionalMap) {
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

	public static SignalReplayRun match(final Object self, final boolean definitions, final List<BaseSignal<?>> positionalList) {
		return new SignalReplayRun(self, definitions, true, null, positionalList, new HashMap<>());
	}

	public static SignalReplayRun replay(final Object self, final boolean definitions, final Map<String, String> holeMap, final Map<Object, BaseSignal<?>> positionalMap) {
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
		final List<BaseSignal<?>> previousList = this.currentList;
		final List<BaseSignal<?>> readList = new ArrayList<>();
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

	public BaseSignal<?> position(final Object key, final Class<?> owner) {
		if (!this.matching) {
			final BaseSignal<?> signal = this.positionalMap.get(key);
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

		final BaseSignal<?> signal = this.positionalList.get(position);
		final BaseSignal<?> previous = this.positionalMap.put(key, signal);
		if (previous != null && previous != signal) {
			throw new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
		}
		return signal;
	}

	public List<BaseSignal<?>> getDefinitionList() {
		final List<Group> sortedList = new ArrayList<>(this.groupList);
		sortedList.sort(Comparator.comparingInt(group -> group.store));
		final List<BaseSignal<?>> readList = new ArrayList<>();
		for (final Group group : sortedList) {
			readList.addAll(group.readList);
		}
		return readList;
	}

	public Map<String, String> getHoleMap() {
		return this.holeMap != null ? Collections.unmodifiableMap(this.holeMap) : null;
	}

	public List<BaseSignal<?>> getPositionalList() {
		return this.positionalList != null ? Collections.unmodifiableList(this.positionalList) : null;
	}

	public Map<Object, BaseSignal<?>> getPositionalMap() {
		return Collections.unmodifiableMap(this.positionalMap);
	}

	public List<Group> getGroupList() {
		return Collections.unmodifiableList(this.groupList);
	}

	public List<BaseSignal<?>> getDirectList() {
		return Collections.unmodifiableList(this.directList);
	}

	public Map<SignalReplaySlice, Object[]> getDefinitionCache() {
		return Collections.unmodifiableMap(this.definitionCache);
	}

	public List<BaseSignal<?>> getCurrentList() {
		return Collections.unmodifiableList(this.currentList);
	}

	private void flush() {
		if (this.matching) {
			this.currentList.addAll(SignalContext.current().takeReads());
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Group {

		private final int             store;
		private final List<BaseSignal<?>> readList;

	}

}