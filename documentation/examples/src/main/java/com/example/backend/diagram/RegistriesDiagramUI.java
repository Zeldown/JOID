package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class RegistriesDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(510, 215, 900, 650).attach(this);

		super.label(20, 20, 860, 40, "Registered by Backend.register()").attach(backdrop);
		super.box(40, 70, 260, 120, "UI", "by default: WindowUIBridge").attach(backdrop);
		super.subject(320, 70, 260, 120, "WINDOW", "your window bridge").attach(backdrop);
		super.subject(600, 70, 260, 120, "RENDER", "your render bridge").attach(backdrop);
		super.box(40, 210, 260, 120, "AUDIO", "optional").attach(backdrop);
		super.box(320, 210, 260, 120, "THREAD", "FrameThreadBridge").attach(backdrop);

		super.label(20, 370, 860, 40, "Registered by JOID before any backend").attach(backdrop);
		super.box(40, 420, 260, 120, "CLOCK", "SystemClockBridge").attach(backdrop);
		super.box(320, 420, 260, 120, "THREAD", "DirectThreadBridge").attach(backdrop);
		super.box(600, 420, 260, 120, "SIGNAL_REPLAY", "identity").attach(backdrop);

		super.label(20, 560, 860, 70, "get() returns the bridge of the highest priority: only UI uses all of its bridges").attach(backdrop);
	}

}