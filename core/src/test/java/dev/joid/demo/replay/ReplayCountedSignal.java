package dev.joid.demo.replay;

import java.util.concurrent.atomic.AtomicInteger;

import dev.joid.lib.signal.impl.primitive.IntegerSignal;

public class ReplayCountedSignal extends IntegerSignal {

	public static final AtomicInteger CREATED = new AtomicInteger();

	public ReplayCountedSignal(final int value) {
		super(value);
		ReplayCountedSignal.CREATED.incrementAndGet();
	}

}