package dev.joid.lib.utils.signal.replay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;

import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public final class ReplayRun {

	private final Object                           self;
	private final boolean                          matching;
	private final boolean                          definitions;
	private final Map<String, String>              holeMap;
	private final List<Signal<?>>                  positionalList;
	private final Map<AbstractInsnNode, Signal<?>> positionalMap;

	private final List<Group>                groupList;
	private final List<Signal<?>>            directList;
	private final Map<ReplaySlice, Object[]> definitionCache;

	private boolean         positionalNeeded;
	private List<Signal<?>> currentList;

	private ReplayRun(final Object self, final boolean definitions, final boolean matching, final Map<String, String> holeMap, final List<Signal<?>> positionalList, final Map<AbstractInsnNode, Signal<?>> positionalMap) {
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

	public static ReplayRun match(final Object self, final boolean definitions, final List<Signal<?>> positionalList) {
		return new ReplayRun(self, definitions, true, null, positionalList, new IdentityHashMap<>());
	}

	public static ReplayRun replay(final Object self, final boolean definitions, final Map<String, String> holeMap, final Map<AbstractInsnNode, Signal<?>> positionalMap) {
		return new ReplayRun(self, definitions, false, holeMap, null, positionalMap);
	}

	public Object run(final ReplaySlice slice) {
		final Object value = ReplayFrame.create(this, slice, 0).execute();
		this.flush();
		return value;
	}

	public Object[] define(final ReplaySlice slice, final int depth) {
		if (this.definitionCache.containsKey(slice)) {
			return this.definitionCache.get(slice);
		}

		this.flush();
		final List<Signal<?>> previousList = this.currentList;
		final List<Signal<?>> readList = new ArrayList<>();
		this.currentList = readList;
		Object[] result;
		try {
			final ReplayFrame frame = ReplayFrame.create(this, slice, depth);
			final Object value = frame.execute();
			this.flush();
			result = value instanceof ReplayUnknown || value instanceof ReplayParts ? null : new Object[] {value, frame.isLastLive()};
		} catch (final ReplayException exception) {
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

	public Signal<?> position(final AbstractInsnNode instruction, final Class<?> owner) {
		if (!this.matching) {
			final Signal<?> signal = this.positionalMap.get(instruction);
			if (signal == null) {
				throw new ReplayException(ReplayFailure.SIGNALS_DIFFER);
			}
			return signal;
		}

		if (this.positionalList == null) {
			this.positionalNeeded = true;
			throw new ReplayException(ReplayFailure.SIGNALS_DIFFER);
		}

		this.flush();
		final int position = this.directList.size();
		if (this.currentList != this.directList || position >= this.positionalList.size() || !owner.isInstance(this.positionalList.get(position))) {
			throw new ReplayException(ReplayFailure.SIGNALS_DIFFER);
		}

		final Signal<?> signal = this.positionalList.get(position);
		this.positionalMap.put(instruction, signal);
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