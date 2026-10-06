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
		final StackTraceElement[] stack = new Throwable().getStackTrace();
		int index = 0;
		while (index < stack.length && SignalReplay.isLibrary(stack[index].getClassName())) {
			index++;
		}

		if (index == 0 || index >= stack.length || SignalReplay.isRuntime(stack[index].getClassName())) {
			context.putBackReads(readList);
			return null;
		}

		final ComputedSignal<?> observer = context.observe(null);
		state.replaying = true;
		try {
			return SignalReplay.resolve(value, readList, ReplayCall.create(stack[index], stack[index - 1].getMethodName()), state);
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
		state.ownerList.add(owner);
		state.ownerList.add(parent);
	}

	public static void exit() {
		final List<Object> ownerList = SignalReplay.STATE.get().ownerList;
		ownerList.remove(ownerList.size() - 1);
		ownerList.remove(ownerList.size() - 1);
	}

	public static void reset() {
		SignalContext.current().clearReads();
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

	private static <T> Supplier<T> resolve(final T value, final List<Signal<?>> readList, final ReplayCall call, final State state) {
		final Object self = state.findOwner(call.getCaller().getClassName());
		final ReplaySite site = SignalReplay.SITES.computeIfAbsent(call.getKey(), key -> SignalReplay.analyze(call, self));
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
				SignalContext.current().putBackReads(readList);
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
		SignalContext.current().putBackReads(match.getRemainingList(readList));
		final List<Signal<?>> consumedList = match.getReadList(readList);
		return consumedList.isEmpty() ? null : ReplayBinding.create(call, match, readList, value);
	}

	private static ReplaySite analyze(final ReplayCall call, final Object self) {
		final StackTraceElement caller = call.getCaller();
		try {
			final ReplayClass replayClass = SignalReplay.CLASSES.computeIfAbsent(caller.getClassName(), name -> ReplayClass.read(name, SignalReplay.loaderOf(self, name)));
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

	private static ClassLoader loaderOf(final Object self, final String name) {
		if (self != null) {
			for (Class<?> type = self.getClass(); type != null; type = type.getSuperclass()) {
				if (type.getName().equals(name)) {
					return type.getClassLoader();
				}
			}
		}
		return SignalReplay.class.getClassLoader();
	}

	private static final class State {

		private final List<Object>                  ownerList = new ArrayList<>();
		private final Map<String, Set<ReplaySlice>> usedMap   = new HashMap<>();

		private boolean replaying;

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