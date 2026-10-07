package dev.joid.lib.utils.signal.replay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import dev.joid.internal.JOID;
import dev.joid.lib.utils.signal.ComputedSignal;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplay {

	private static final Set<String>              WARNINGS = ConcurrentHashMap.newKeySet();
	private static final ThreadLocal<State>       STATE    = ThreadLocal.withInitial(State::new);
	private static final Map<String, ReplaySite>  SITES    = new ConcurrentHashMap<>();
	private static final Map<String, ReplayClass> CLASSES  = new ConcurrentHashMap<>();

	public static <T> Supplier<T> replay(final T value) {
		final SignalContext context = SignalContext.current();
		if (!context.hasReads()) {
			return null;
		}

		final State state = SignalReplay.STATE.get();
		if (state.replaying) {
			return null;
		}

		final List<Signal<?>> readList = context.takeReads();
		final int stale = Math.min(state.staleCount, readList.size());
		state.staleCount = 0;
		final ComputedSignal<?> observer = context.observe(null);
		state.replaying = true;
		try {
			final ReplayCall call = SignalReplay.locate(new Throwable().getStackTrace(), state);
			if (call == null) {
				state.putBack(readList);
				return null;
			}
			return SignalReplay.resolve(value, readList, call, state, stale);
		} catch (final RuntimeException | LinkageError exception) {
			return null;
		} finally {
			state.replaying = false;
			context.observe(observer);
		}
	}

	public static void enter(final Object owner) {
		SignalReplay.enter(owner, null);
	}

	public static void enter(final Object owner, final Object parent) {
		final State state = SignalReplay.STATE.get();
		if (state.ownerList.isEmpty()) {
			state.clear();
		}

		state.ownerList.add(owner);
		state.ownerList.add(parent);
		state.tracingList.add(SignalContext.current().tracing(true));
	}

	public static void exit() {
		final State state = SignalReplay.STATE.get();
		state.ownerList.remove(state.ownerList.size() - 1);
		state.ownerList.remove(state.ownerList.size() - 1);
		SignalContext.current().tracing(state.tracingList.remove(state.tracingList.size() - 1));
		if (state.ownerList.isEmpty()) {
			state.clear();
		}
	}

	public static void reset() {
		SignalReplay.STATE.get().clear();
		SignalReplay.STATE.get().usedMap.clear();
	}

	public static void clear() {
		SignalReplay.CLASSES.clear();
		SignalReplay.SITES.clear();
	}

	public static void warn(final ReplayCall call, final List<String> nameList, final int readCount, final ReplayException exception, final Object value) {
		if (!JOID.inst().isDevMode() || !SignalReplay.WARNINGS.add(call.getKey())) {
			return;
		}
		System.err.println("[JOID] " + call.describe(nameList, readCount) + " but cannot follow it: " + exception.getMessage() + ". The value stays \"" + value + "\". " + exception.getAdvice());
	}

	private static <T> Supplier<T> resolve(final T value, final List<Signal<?>> readList, final ReplayCall call, final State state, final int stale) {
		final Object self = state.findOwner(call.getCaller().getClassName());
		final ReplaySite site = SignalReplay.site(call, self);
		if (site.getFailure() != null) {
			SignalReplay.warn(call, Collections.emptyList(), readList.size(), site.getFailure(), value);
			return null;
		}

		final List<ReplayMatch> matchList = new ArrayList<>();
		ReplayException failure = null;
		ReplaySlice failedSlice = null;
		for (final ReplaySlice slice : site.getSliceList()) {
			try {
				matchList.add(SignalReplay.attempt(slice, self, value, readList));
			} catch (final ReplayException exception) {
				if (failedSlice == null || SignalReplay.rank(slice, exception) > SignalReplay.rank(failedSlice, failure)) {
					failure = exception;
					failedSlice = slice;
				}
			}
		}

		if (matchList.isEmpty()) {
			if (failedSlice.getSignalCount() == 0) {
				state.putBack(readList);
			} else if (stale > 0) {
				final List<Signal<?>> freshList = new ArrayList<>(readList.subList(stale, readList.size()));
				final Supplier<T> supplier = freshList.isEmpty() ? null : SignalReplay.resolve(value, freshList, call, state, 0);
				state.putBack(new ArrayList<>(readList.subList(0, stale)));
				return supplier;
			} else {
				SignalReplay.warn(call, failedSlice.getSignalNameList(), readList.size(), failure, value);
			}
			return null;
		}

		for (final ReplayMatch match : matchList) {
			if (!match.getSlice().isEquivalent(matchList.get(0).getSlice()) && !match.getReadList(readList).isEmpty()) {
				SignalReplay.warn(call, match.getSlice().getSignalNameList(), readList.size(), new ReplayException(ReplayFailure.AMBIGUOUS_CALL, call.getSetter()), value);
				return null;
			}
		}

		final ReplayMatch match = state.pick(call.getKey(), matchList);
		state.putBack(match.getRemainingList(readList));
		final List<Signal<?>> consumedList = match.getReadList(readList);
		return consumedList.isEmpty() ? null : ReplayBinding.create(call, match, readList, value);
	}

	private static ReplayCall locate(final StackTraceElement[] stack, final State state) {
		for (int index = 2; index < stack.length && index < 18; index++) {
			if (SignalReplay.isRuntime(stack[index].getClassName())) {
				return null;
			}

			final ReplayCall call = ReplayCall.create(stack[index], stack[index - 1].getMethodName());
			final ReplaySite site = SignalReplay.site(call, state.findOwner(call.getCaller().getClassName()));
			if (!site.isPassThrough()) {
				return SignalReplay.isLibrary(stack[index].getClassName()) ? null : call;
			}
		}
		return null;
	}

	private static ReplaySite site(final ReplayCall call, final Object self) {
		final String name = call.getCaller().getClassName();
		final ReplayClass replayClass = SignalReplay.CLASSES.get(name);
		if (replayClass != null && replayClass.isStale()) {
			SignalReplay.CLASSES.remove(name, replayClass);
			SignalReplay.SITES.keySet().removeIf(key -> key.startsWith(name + "#"));
		}
		return SignalReplay.SITES.computeIfAbsent(call.getKey(), key -> SignalReplay.analyze(call, self));
	}

	private static ReplaySite analyze(final ReplayCall call, final Object self) {
		final StackTraceElement caller = call.getCaller();
		try {
			final ReplayClass replayClass = SignalReplay.CLASSES.computeIfAbsent(caller.getClassName(), name -> {
				final Class<?> type = SignalReplay.typeOf(self, name);
				return ReplayClass.read(name, type, type != null ? type.getClassLoader() : SignalReplay.class.getClassLoader());
			});
			return ReplaySite.analyze(replayClass, caller, call.getSetter());
		} catch (final ReplayException exception) {
			return ReplaySite.fail(exception);
		}
	}

	private static ReplayMatch attempt(final ReplaySlice slice, final Object self, final Object value, final List<Signal<?>> readList) {
		final ReplayRun run = ReplayRun.match(self, true, null);
		try {
			return SignalReplay.attempt(slice, run, value, readList);
		} catch (final ReplayException exception) {
			if (run.getDefinitionCache().isEmpty()) {
				throw exception;
			}
			return SignalReplay.attempt(slice, ReplayRun.match(self, false, null), value, readList);
		}
	}

	private static ReplayMatch attempt(final ReplaySlice slice, final ReplayRun run, final Object value, final List<Signal<?>> readList) {
		try {
			return SignalReplay.check(slice, run, run.run(slice), value, readList);
		} catch (final ReplayException exception) {
			SignalContext.current().clearReads();
			if (!run.isPositionalNeeded()) {
				throw SignalReplay.translate(exception);
			}
		}

		ReplayException failure = new ReplayException(ReplayFailure.SIGNALS_DIFFER);
		for (int count = 1; count <= Math.min(readList.size(), slice.getSignalCount()); count++) {
			final ReplayRun positional = ReplayRun.match(run.getSelf(), run.isDefinitions(), readList.subList(readList.size() - count, readList.size()));
			try {
				return SignalReplay.check(slice, positional, positional.run(slice), value, readList);
			} catch (final ReplayException exception) {
				SignalContext.current().clearReads();
				failure = SignalReplay.translate(exception);
			}
		}
		throw failure;
	}

	private static ReplayMatch check(final ReplaySlice slice, final ReplayRun run, final Object result, final Object value, final List<Signal<?>> readList) {
		Map<String, String> holeMap = Collections.emptyMap();
		if (result instanceof ReplayUnknown) {
			throw new ReplayException(ReplayFailure.LOCAL_COMBINED, ((ReplayUnknown) result).getDescription());
		}

		if (result instanceof ReplayParts) {
			holeMap = value instanceof String ? ((ReplayParts) result).solve((String) value) : null;
			if (holeMap == null) {
				throw new ReplayException(ReplayFailure.VALUE_DIFFERS, "a text that does not fit");
			}
		} else {
			final Object converted = slice.convert(result);
			if (!SignalReplay.isSame(converted, value)) {
				throw new ReplayException(ReplayFailure.VALUE_DIFFERS, "\"" + converted + "\"");
			}
		}

		final boolean[] consumed = SignalReplay.consume(readList, run.getDirectList(), run.getDefinitionList());
		if (consumed == null) {
			throw new ReplayException(ReplayFailure.SIGNALS_DIFFER);
		}
		return ReplayMatch.create(slice, run, holeMap, consumed);
	}

	private static boolean[] consume(final List<Signal<?>> readList, final List<Signal<?>> directList, final List<Signal<?>> definitionList) {
		final int offset = readList.size() - directList.size();
		if (offset < 0) {
			return null;
		}

		final boolean[] consumed = new boolean[readList.size()];
		for (int index = 0; index < directList.size(); index++) {
			if (readList.get(offset + index) != directList.get(index)) {
				return null;
			}
			consumed[offset + index] = true;
		}

		int position = offset - 1;
		for (int index = definitionList.size() - 1; index >= 0; index--) {
			while (position >= 0 && readList.get(position) != definitionList.get(index)) {
				position--;
			}

			if (position < 0) {
				return null;
			}
			consumed[position--] = true;
		}
		return consumed;
	}

	private static ReplayException translate(final ReplayException exception) {
		return exception.getFailure() == ReplayFailure.REPLAY_FAILED ? new ReplayException(ReplayFailure.VALUE_DIFFERS, exception.getArguments()) : exception;
	}

	private static int rank(final ReplaySlice slice, final ReplayException exception) {
		final int weight = exception.getFailure() == ReplayFailure.VALUE_DIFFERS || exception.getFailure() == ReplayFailure.SIGNALS_DIFFER ? 1 : 2;
		return slice.getSignalCount() > 0 ? weight + 10 : weight;
	}

	private static boolean isSame(final Object value, final Object expected) {
		if (Objects.equals(value, expected)) {
			return true;
		}

		if (value == null || expected == null || value.getClass() != expected.getClass()) {
			return false;
		}

		try {
			return value.getClass().getMethod("equals", Object.class).getDeclaringClass() == Object.class;
		} catch (final NoSuchMethodException exception) {
			return false;
		}
	}

	private static boolean isLibrary(final String name) {
		return name.startsWith("dev.joid.") && !name.startsWith("dev.joid.demo.") && !name.contains(".demo.");
	}

	private static boolean isRuntime(final String name) {
		return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.") || name.startsWith("jdk.") || name.startsWith("com.sun.");
	}

	private static Class<?> typeOf(final Object self, final String name) {
		if (self != null) {
			for (Class<?> type = self.getClass(); type != null; type = type.getSuperclass()) {
				if (type.getName().equals(name)) {
					return type;
				}
			}
		}

		for (final ClassLoader loader : new ClassLoader[] {Thread.currentThread().getContextClassLoader(), SignalReplay.class.getClassLoader()}) {
			try {
				return Class.forName(name, false, loader);
			} catch (final ClassNotFoundException | LinkageError | RuntimeException exception) {
				continue;
			}
		}
		return null;
	}

	private static final class State {

		private final List<Object>                  ownerList   = new ArrayList<>();
		private final List<Boolean>                 tracingList = new ArrayList<>();
		private final Map<String, Set<ReplaySlice>> usedMap     = new HashMap<>();

		private int     staleCount;
		private boolean replaying;

		private void clear() {
			SignalContext.current().clearReads();
			this.staleCount = 0;
		}

		private void putBack(final List<Signal<?>> readList) {
			SignalContext.current().putBackReads(readList);
			this.staleCount += readList.size();
		}

		private Object findOwner(final String name) {
			for (int index = this.ownerList.size() - 1; index >= 0; index--) {
				final Object owner = this.ownerList.get(index);
				for (Class<?> type = owner != null ? owner.getClass() : null; type != null; type = type.getSuperclass()) {
					if (type.getName().equals(name)) {
						return owner;
					}
				}
			}
			return null;
		}

		private ReplayMatch pick(final String key, final List<ReplayMatch> matchList) {
			final Set<ReplaySlice> usedSet = this.usedMap.computeIfAbsent(key, ignored -> Collections.newSetFromMap(new IdentityHashMap<>()));
			for (final ReplayMatch match : matchList) {
				if (usedSet.add(match.getSlice())) {
					return match;
				}
			}

			usedSet.clear();
			usedSet.add(matchList.get(0).getSlice());
			return matchList.get(0);
		}

	}

}