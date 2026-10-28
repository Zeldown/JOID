package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class RegistriesDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(325, 200, 1270, 520).attach(this);

		super.label(20, 20, 1230, 40, "Registered by Backend.register()").attach(backdrop);
		super.box(20, 70, 230, 120, "UI", "by default: WindowUIBridge").attach(backdrop);
		super.subject(270, 70, 230, 120, "WINDOW", "your window bridge").attach(backdrop);
		super.subject(520, 70, 230, 120, "RENDER", "your render bridge").attach(backdrop);
		super.box(770, 70, 230, 120, "AUDIO", "optional").attach(backdrop);
		super.box(1020, 70, 230, 120, "THREAD", "FrameThreadBridge").attach(backdrop);

		super.label(20, 230, 1230, 40, "Registered by JOID before any backend").attach(backdrop);
		super.box(20, 280, 230, 120, "CLOCK", "SystemClockBridge").attach(backdrop);
		super.box(270, 280, 230, 120, "THREAD", "DirectThreadBridge").attach(backdrop);
		super.box(520, 280, 230, 120, "SIGNAL_REPLAY", "identity").attach(backdrop);

		super.label(20, 430, 1230, 70, "get() returns the bridge of the highest priority: only UI uses all of its bridges").attach(backdrop);
	}

}