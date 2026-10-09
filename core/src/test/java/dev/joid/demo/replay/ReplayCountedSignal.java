package dev.joid.demo.replay;

import java.util.concurrent.atomic.AtomicInteger;

import dev.joid.lib.signal.impl.primitive.IntegerSignal;

public class ReplayCountedSignal extends IntegerSignal {

	public static final AtomicInteger CREATED = new AtomicInteger();

	protected ReplayCountedSignal(final int value) {
		super(value);
		ReplayCountedSignal.CREATED.incrementAndGet();
	}

	public static ReplayCountedSignal create(final int value) {
		return new ReplayCountedSignal(value);
	}

}