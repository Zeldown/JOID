package dev.joid.lib.ui.core;

import java.util.concurrent.atomic.AtomicInteger;

import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@UIDataDebug(profiler = false)
public final class HotReloadUI extends UI {

	private final AtomicInteger inits;

	@Override
	public void init() {
		this.inits.incrementAndGet();
	}

}