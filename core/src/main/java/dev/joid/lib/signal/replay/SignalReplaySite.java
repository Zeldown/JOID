package dev.joid.lib.signal.replay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplaySite {

	private final List<SignalReplaySlice> sliceList;
	private final SignalReplayException   failure;

	public static SignalReplaySite analyze(final SignalReplayClass replayClass, final StackTraceElement caller, final String setter) {
		try {
			final List<SignalReplaySlice> sliceList = new ArrayList<>();
			for (final MethodNode method : replayClass.getNode().methods) {
				if (method.name.equals(caller.getMethodName())) {
					SignalReplaySite.collect(replayClass, method, caller.getLineNumber(), setter, sliceList);
				}
			}

			if (sliceList.isEmpty()) {
				throw new SignalReplayException(SignalReplayFailure.CALL_NOT_FOUND, setter, caller.getLineNumber() >= 0 ? "on this line" : "in this method");
			}
			return new SignalReplaySite(sliceList, null);
		} catch (final SignalReplayException exception) {
			return SignalReplaySite.fail(exception);
		}
	}

	public static SignalReplaySite fail(final SignalReplayException exception) {
		return new SignalReplaySite(Collections.emptyList(), exception);
	}

	public boolean isPassThrough() {
		return this.sliceList.stream().allMatch(slice -> slice.getSignalCount() == 0) && this.sliceList.stream().anyMatch(SignalReplaySlice::isParameter);
	}

	private static void collect(final SignalReplayClass replayClass, final MethodNode method, final int lineNumber, final String setter, final List<SignalReplaySlice> sliceList) {
		SignalReplayMethod replayMethod = null;
		int line = -1;
		for (final AbstractInsnNode instruction : method.instructions) {
			if (instruction instanceof LineNumberNode) {
				line = ((LineNumberNode) instruction).line;
				continue;
			}

			if (!(instruction instanceof MethodInsnNode) || !((MethodInsnNode) instruction).name.equals(setter) || lineNumber >= 0 && line != lineNumber) {
				continue;
			}

			if (replayMethod == null) {
				replayMethod = replayClass.method(method);
			}

			final int index = replayMethod.indexOf(instruction);
			final Frame<SourceValue> frame = replayMethod.getFrames()[index];
			if (frame == null) {
				continue;
			}

			final Type[] argumentTypes = Type.getArgumentTypes(((MethodInsnNode) instruction).desc);
			final List<SignalReplaySlice> callList = new ArrayList<>();
			int end = index;
			for (int argument = argumentTypes.length - 1; argument >= 0; argument--) {
				final SourceValue value = frame.getStack(frame.getStackSize() - argumentTypes.length + argument);
				final int start = replayMethod.expressionStart(value.insns, end);
				callList.add(0, SignalReplaySlice.create(replayMethod, start, end, argumentTypes[argument]));
				end = start;
			}
			sliceList.addAll(callList);
		}
	}

}