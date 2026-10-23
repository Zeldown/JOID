package dev.joid.lib.ui.core;

import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

@UIDataDebug(profiler = false, hotreload = false)
public class NodeUI extends UI {

	private final Node[] nodes;

	public NodeUI(final @NonNull Node... nodes) {
		this.nodes = nodes;
	}

	@Override
	public void init() {
		super.add(this.nodes);
	}

}