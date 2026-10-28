package dev.joid.lib.signal.replay;

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

import dev.joid.internal.JOIDLog;
import dev.joid.internal.signal.replay.SignalReplayCall;
import dev.joid.internal.signal.replay.SignalReplayClass;
import dev.joid.internal.signal.replay.SignalReplayException;
import dev.joid.internal.signal.replay.SignalReplayFailure;
import dev.joid.internal.signal.replay.SignalReplayMatch;
import dev.joid.internal.signal.replay.SignalReplayParts;
import dev.joid.internal.signal.replay.SignalReplayRun;
import dev.joid.internal.signal.replay.SignalReplaySite;
import dev.joid.internal.signal.replay.SignalReplaySlice;
import dev.joid.internal.signal.replay.SignalReplayUnknown;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplay {

	private static final ThreadLocal<State>             STATE   = ThreadLocal.withInitial(State::new);
	private static final Map<String, SignalReplaySite>  SITES   = new ConcurrentHashMap<>();
	private static final Map<String, SignalReplayClass> CLASSES = new ConcurrentHashMap<>();

	public static <T> Supplier<T> replay(final T value) {
		final State state = SignalReplay.STATE.get();
		if (state.replaying) {
			return null;
		}

		final int serial = ++state.serial;
		final SignalContext context = SignalContext.current();
		if (!context.hasReads()) {
			return null;
		}

		final List<Signal<?>> readList = context.takeReads();
		final int stale = Math.min(state.staleCount, readList.size());
		state.staleCount = 0;
		final ComputedSignal<?> observer = context.observe(null);
		state.replaying = true;
		try {
			final SignalReplayCall call = SignalReplay.locate(new Throwable().getStackTrace(), state);
			if (call == null) {
				state.putBack(readList);
				return null;
			}
			return SignalReplay.resolve(value, readList, call, state, stale, serial);
		} catch (final RuntimeException | LinkageError exception) {
			SignalReplay.warn(exception, value);
			return null;
		} finally {
			state.replaying = false;
			context.observe(observer);
		}
	}

	public static void run(final Object owner, final Object parent, final Runnable runnable) {
		SignalReplay.enter(owner, parent);
		try {
			runnable.run();
		} finally {
			SignalReplay.exit();
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

	public static void warn(final SignalReplayCall call, final List<String> nameList, final int readCount, final SignalReplayException exception, final Object value) {
		JOIDLog.devWarnOnce("SignalReplay." + call.getKey(), () -> call.describe(nameList, readCount) + " but cannot follow it: " + exception.getMessage() + ". The value stays \"" + value + "\". " + exception.getAdvice());
	}

	private static void warn(final Throwable exception, final Object value) {
		JOIDLog.devWarnOnce("SignalReplay.error." + exception.getClass().getName(), () -> "The replay of a signal read failed on an internal error, the value \"" + value + "\" stays fixed", exception);
	}

	private static void enter(final Object owner, final Object parent) {
		final State state = SignalReplay.STATE.get();
		if (state.ownerList.isEmpty()) {
			state.clear();
		}

		state.ownerList.add(owner);
		state.ownerList.add(parent);
		state.tracingList.add(SignalContext.current().tracing(true));
	}

	private static void exit() {
		final State state = SignalReplay.STATE.get();
		state.ownerList.remove(state.ownerList.size() - 1);
		state.ownerList.remove(state.ownerList.size() - 1);
		SignalContext.current().tracing(state.tracingList.remove(state.tracingList.size() - 1));
		if (state.ownerList.isEmpty()) {
			state.clear();
		}
	}

	private static <T> Supplier<T> resolve(final T value, final List<Signal<?>> readList, final SignalReplayCall call, final State state, final int stale, final int serial) {
		final Object self = state.findOwner(call.getCaller().getClassName());
		final SignalReplaySite site = SignalReplay.site(call, self);
		if (site.getFailure() != null) {
			SignalReplay.warn(call, Collections.emptyList(), readList.size(), site.getFailure(), value);
			return null;
		}

		final Claim claim = state.expand(site, readList, serial);
		if (claim != null) {
			final List<SignalReplayMatch> matchList = new ArrayList<>();
			if (SignalReplay.reclaim(value, claim, call, site, self, matchList)) {
				return matchList.isEmpty() ? null : SignalReplay.bind(value, claim, call, state, matchList);
			}
		}

		final List<SignalReplayMatch> matchList = new ArrayList<>();
		SignalReplayException failure = null;
		SignalReplaySlice failedSlice = null;
		for (final SignalReplaySlice slice : site.getSliceList()) {
			try {
				matchList.add(SignalReplay.attempt(slice, self, value, readList));
			} catch (final SignalReplayException exception) {
				if (failedSlice == null || SignalReplay.rank(slice, exception) > SignalReplay.rank(failedSlice, failure)) {
					failure = exception;
					failedSlice = slice;
				}
			}
		}

		SignalReplay.prefer(matchList);
		if (matchList.isEmpty()) {
			if (failedSlice.getSignalCount() == 0 && (!failedSlice.hasLambda() || failedSlice.isEnclosed())) {
				state.putBack(readList);
			} else if (stale > 0) {
				final List<Signal<?>> freshList = readList.subList(stale, readList.size());
				final Supplier<T> supplier = freshList.isEmpty() ? null : SignalReplay.resolve(value, freshList, call, state, 0, serial);
				state.putBack(readList.subList(0, stale));
				state.widen(serial, readList.subList(0, stale));
				return supplier;
			} else {
				SignalReplay.warn(call, failedSlice.getSignalNameList(), readList.size(), failedSlice.getSignalCount() == 0 ? new SignalReplayException(SignalReplayFailure.LAMBDA) : failure, value);
			}
			return null;
		}

		for (final SignalReplayMatch match : matchList) {
			if (!match.getSlice().isEquivalent(matchList.get(0).getSlice()) && !match.getReadList(readList).isEmpty()) {
				SignalReplay.warn(call, match.getSlice().getSignalNameList(), readList.size(), new SignalReplayException(SignalReplayFailure.AMBIGUOUS_CALL, call.getSetter()), value);
				return null;
			}
		}

		return SignalReplay.bind(value, new Claim(serial, new boolean[readList.size()], null, readList), call, state, matchList);
	}

	private static boolean reclaim(final Object value, final Claim claim, final SignalReplayCall call, final SignalReplaySite site, final Object self, final List<SignalReplayMatch> matchList) {
		SignalReplayException failure = null;
		SignalReplaySlice failedSlice = null;
		for (final SignalReplaySlice slice : site.getSliceList()) {
			try {
				final SignalReplayMatch match = SignalReplay.attempt(slice, self, value, claim.readList);
				if (claim.isOpenedBy(match)) {
					matchList.add(match);
				}
			} catch (final SignalReplayException exception) {
				if (slice.encloses(claim.slice) && slice.getSignalCount() > claim.getConsumedCount() && (failedSlice == null || SignalReplay.rank(slice, exception) > SignalReplay.rank(failedSlice, failure))) {
					failure = exception;
					failedSlice = slice;
				}
			}
		}

		SignalReplay.prefer(matchList);
		for (final SignalReplayMatch match : matchList) {
			if (!match.getSlice().isEquivalent(matchList.get(0).getSlice())) {
				matchList.clear();
				return false;
			}
		}

		if (matchList.isEmpty() && failedSlice != null) {
			SignalReplay.warn(call, failedSlice.getSignalNameList(), claim.readList.size(), failure, value);
		}
		return !matchList.isEmpty() || failedSlice != null;
	}

	private static <T> Supplier<T> bind(final T value, final Claim claim, final SignalReplayCall call, final State state, final List<SignalReplayMatch> matchList) {
		final SignalReplayMatch match = state.pick(call.getKey(), matchList);
		final boolean[] taken = new boolean[claim.readList.size()];
		final List<Signal<?>> remainingList = new ArrayList<>();
		for (int index = 0; index < taken.length; index++) {
			taken[index] = claim.consumed[index] || match.getConsumed()[index];
			if (!taken[index]) {
				remainingList.add(claim.readList.get(index));
			}
		}

		state.putBack(remainingList);
		state.claim(new Claim(claim.serial, taken, match.getSlice(), claim.readList));
		final List<Signal<?>> consumedList = match.getReadList(claim.readList);
		return consumedList.isEmpty() ? null : new SignalReplayBinding<>(call, match, consumedList, value);
	}

	private static SignalReplayCall locate(final StackTraceElement[] stack, final State state) {
		for (int index = 2; index < stack.length && index < 18; index++) {
			if (SignalReplay.isRuntime(stack[index].getClassName())) {
				return null;
			}

			final SignalReplayCall call = SignalReplayCall.of(stack[index], stack[index - 1].getMethodName());
			final SignalReplaySite site = SignalReplay.site(call, state.findOwner(call.getCaller().getClassName()));
			if (!site.isPassThrough()) {
				return SignalReplay.isLibrary(stack[index].getClassName()) ? null : call;
			}
		}
		return null;
	}

	private static SignalReplaySite site(final SignalReplayCall call, final Object self) {
		final String name = call.getCaller().getClassName();
		final SignalReplayClass replayClass = SignalReplay.CLASSES.get(name);
		if (replayClass != null && replayClass.isStale()) {
			SignalReplay.CLASSES.remove(name, replayClass);
			SignalReplay.SITES.keySet().removeIf(key -> key.startsWith(name + "#"));
		}
		return SignalReplay.SITES.computeIfAbsent(call.getKey(), key -> SignalReplay.analyze(call, self));
	}

	private static SignalReplaySite analyze(final SignalReplayCall call, final Object self) {
		final StackTraceElement caller = call.getCaller();
		try {
			final SignalReplayClass replayClass = SignalReplay.CLASSES.computeIfAbsent(caller.getClassName(), name -> {
				final Class<?> type = SignalReplay.typeOf(self, name);
				return SignalReplayClass.read(name, type, type != null ? type.getClassLoader() : SignalReplay.class.getClassLoader());
			});
			return SignalReplaySite.analyze(replayClass, caller, call.getSetter());
		} catch (final SignalReplayException exception) {
			return SignalReplaySite.fail(exception);
		}
	}

	private static SignalReplayMatch attempt(final SignalReplaySlice slice, final Object self, final Object value, final List<Signal<?>> readList) {
		final SignalReplayRun run = SignalReplayRun.match(self, true, null);
		try {
			return SignalReplay.attempt(slice, run, value, readList);
		} catch (final SignalReplayException exception) {
			if (run.getDefinitionCache().isEmpty()) {
				throw exception;
			}
			return SignalReplay.attempt(slice, SignalReplayRun.match(self, false, null), value, readList);
		}
	}

	private static SignalReplayMatch attempt(final SignalReplaySlice slice, final SignalReplayRun run, final Object value, final List<Signal<?>> readList) {
		try {
			return SignalReplay.check(slice, run, run.run(slice), value, readList);
		} catch (final SignalReplayException exception) {
			SignalContext.current().clearReads();
			if (!run.isPositionalNeeded()) {
				throw SignalReplay.translate(exception);
			}
		}

		SignalReplayException failure = new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
		for (int count = 1; count <= Math.min(readList.size(), slice.getSignalCount()); count++) {
			final SignalReplayRun positional = SignalReplayRun.match(run.getSelf(), run.isDefinitions(), readList.subList(readList.size() - count, readList.size()));
			try {
				return SignalReplay.check(slice, positional, positional.run(slice), value, readList);
			} catch (final SignalReplayException exception) {
				SignalContext.current().clearReads();
				failure = SignalReplay.translate(exception);
			}
		}
		throw failure;
	}

	private static SignalReplayMatch check(final SignalReplaySlice slice, final SignalReplayRun run, final Object result, final Object value, final List<Signal<?>> readList) {
		Map<String, String> holeMap = Collections.emptyMap();
		if (result instanceof SignalReplayUnknown) {
			throw new SignalReplayException(SignalReplayFailure.LOCAL_COMBINED, ((SignalReplayUnknown) result).getDescription());
		}

		if (result instanceof SignalReplayParts) {
			holeMap = value instanceof String ? ((SignalReplayParts) result).solve((String) value) : null;
			if (holeMap == null) {
				throw new SignalReplayException(SignalReplayFailure.VALUE_DIFFERS, "a text that does not fit");
			}
		} else {
			final Object converted = slice.convert(result);
			if (!SignalReplay.isSame(converted, value)) {
				throw new SignalReplayException(SignalReplayFailure.VALUE_DIFFERS, "\"" + converted + "\"");
			}
		}

		boolean[] consumed = SignalReplay.consume(readList, run.getDirectList(), run.getDefinitionList(), Collections.emptyList());
		final boolean followed = consumed == null && !slice.getFollowingList().isEmpty();
		if (followed) {
			final List<Signal<?>> followingList = SignalReplay.follow(slice, run);
			consumed = followingList != null ? SignalReplay.consume(readList, run.getDirectList(), run.getDefinitionList(), followingList) : null;
		}

		if (consumed == null) {
			throw new SignalReplayException(SignalReplayFailure.SIGNALS_DIFFER);
		}
		return SignalReplayMatch.of(slice, run, holeMap, consumed, followed);
	}

	private static List<Signal<?>> follow(final SignalReplaySlice slice, final SignalReplayRun run) {
		final List<Signal<?>> followingList = new ArrayList<>();
		try {
			for (final SignalReplaySlice following : slice.getFollowingList()) {
				final SignalReplayRun followingRun = SignalReplayRun.match(run.getSelf(), run.isDefinitions(), null);
				followingRun.run(following);
				followingList.addAll(followingRun.getDirectList());
			}
		} catch (final SignalReplayException exception) {
			SignalContext.current().clearReads();
			return null;
		}
		return followingList;
	}

	private static boolean[] consume(final List<Signal<?>> readList, final List<Signal<?>> directList, final List<Signal<?>> definitionList, final List<Signal<?>> followingList) {
		final int offset = readList.size() - directList.size() - followingList.size();
		if (offset < 0) {
			return null;
		}

		for (int index = 0; index < followingList.size(); index++) {
			if (readList.get(offset + directList.size() + index) != followingList.get(index)) {
				return null;
			}
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

	private static void prefer(final List<SignalReplayMatch> matchList) {
		if (matchList.stream().anyMatch(match -> !match.isFollowed())) {
			matchList.removeIf(SignalReplayMatch::isFollowed);
		}
	}

	private static SignalReplayException translate(final SignalReplayException exception) {
		return exception.getFailure() == SignalReplayFailure.REPLAY_FAILED ? new SignalReplayException(SignalReplayFailure.VALUE_DIFFERS, exception.getArguments()) : exception;
	}

	private static int rank(final SignalReplaySlice slice, final SignalReplayException exception) {
		final int weight = exception.getFailure() == SignalReplayFailure.VALUE_DIFFERS || exception.getFailure() == SignalReplayFailure.SIGNALS_DIFFER ? 1 : 2;
		return slice.getSignalCount() > 0 ? weight + 10 : weight;
	}

	private static boolean isLibrary(final String name) {
		return name.startsWith("dev.joid.") && !name.startsWith("dev.joid.showcase.") && !name.contains(".demo.");
	}

	private static boolean isRuntime(final String name) {
		return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.") || name.startsWith("jdk.") || name.startsWith("com.sun.");
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

	private static boolean startsWith(final List<Signal<?>> readList, final List<Signal<?>> prefixList) {
		if (readList.size() < prefixList.size()) {
			return false;
		}

		for (int index = 0; index < prefixList.size(); index++) {
			if (readList.get(index) != prefixList.get(index)) {
				return false;
			}
		}
		return true;
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

		private final List<Claim>                         claimList   = new ArrayList<>();
		private final List<Object>                        ownerList   = new ArrayList<>();
		private final List<Boolean>                       tracingList = new ArrayList<>();
		private final Map<String, Set<SignalReplaySlice>> usedMap     = new HashMap<>();

		private int     serial;
		private int     staleCount;
		private boolean replaying;

		private void clear() {
			SignalContext.current().clearReads();
			this.claimList.clear();
			this.staleCount = 0;
		}

		private void putBack(final List<Signal<?>> readList) {
			SignalContext.current().putBackReads(readList);
			this.staleCount += readList.size();
		}

		private void claim(final Claim claim) {
			if (!this.claimList.isEmpty() && this.claimList.get(this.claimList.size() - 1).serial != claim.serial - 1) {
				this.claimList.clear();
			}
			this.claimList.add(claim);
		}

		private void widen(final int serial, final List<Signal<?>> staleList) {
			final int last = this.claimList.size() - 1;
			final Claim claim = last >= 0 ? this.claimList.get(last) : null;
			if (claim == null || claim.serial != serial) {
				return;
			}

			final List<Signal<?>> readList = new ArrayList<>(staleList);
			readList.addAll(claim.readList);
			final boolean[] consumed = new boolean[readList.size()];
			System.arraycopy(claim.consumed, 0, consumed, staleList.size(), claim.consumed.length);
			this.claimList.set(last, new Claim(serial, consumed, claim.slice, readList));
		}

		private Claim expand(final SignalReplaySite site, final List<Signal<?>> readList, final int serial) {
			List<Signal<?>> expandedList = readList;
			boolean[] claimed = new boolean[readList.size()];
			SignalReplaySlice slice = null;
			int expected = serial - 1;
			for (int index = this.claimList.size() - 1; index >= 0; index--) {
				final Claim claim = this.claimList.get(index);
				final List<Signal<?>> remainingList = claim.getRemainingList();
				if (claim.serial != expected || !site.encloses(claim.slice) || !SignalReplay.startsWith(expandedList, remainingList)) {
					break;
				}

				final List<Signal<?>> nextList = new ArrayList<>(claim.readList);
				nextList.addAll(expandedList.subList(remainingList.size(), expandedList.size()));
				final boolean[] nextClaimed = new boolean[nextList.size()];
				int position = 0;
				for (int read = 0; read < claim.readList.size(); read++) {
					if (claim.consumed[read]) {
						nextClaimed[read] = true;
					} else {
						nextClaimed[read] = claimed[position++];
					}
				}
				System.arraycopy(claimed, remainingList.size(), nextClaimed, claim.readList.size(), claimed.length - remainingList.size());
				expandedList = nextList;
				claimed = nextClaimed;
				slice = slice != null ? slice : claim.slice;
				expected--;
			}
			return expandedList != readList ? new Claim(serial, claimed, slice, expandedList) : null;
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

		private SignalReplayMatch pick(final String key, final List<SignalReplayMatch> matchList) {
			final Set<SignalReplaySlice> usedSet = this.usedMap.computeIfAbsent(key, ignored -> Collections.newSetFromMap(new IdentityHashMap<>()));
			for (final SignalReplayMatch match : matchList) {
				if (usedSet.add(match.getSlice())) {
					return match;
				}
			}

			usedSet.clear();
			usedSet.add(matchList.get(0).getSlice());
			return matchList.get(0);
		}

	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Claim {

		private final int               serial;
		private final boolean[]         consumed;
		private final SignalReplaySlice slice;
		private final List<Signal<?>>   readList;

		private int getConsumedCount() {
			int count = 0;
			for (final boolean read : this.consumed) {
				if (read) {
					count++;
				}
			}
			return count;
		}

		private List<Signal<?>> getRemainingList() {
			final List<Signal<?>> remainingList = new ArrayList<>();
			for (int index = 0; index < this.readList.size(); index++) {
				if (!this.consumed[index]) {
					remainingList.add(this.readList.get(index));
				}
			}
			return remainingList;
		}

		private boolean isOpenedBy(final SignalReplayMatch match) {
			for (int index = 0; index < this.consumed.length; index++) {
				if (match.getConsumed()[index] && !this.consumed[index]) {
					return true;
				}
			}
			return false;
		}

	}

}