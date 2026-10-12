package dev.joid.demo.ui.option.popup;

import dev.joid.demo.ui.core.transition.impl.DemoSlideTransition;

public class UIDemoSlidePopup extends UIDemoOptionPopup {

	public UIDemoSlidePopup() {
		super.setTransition(new DemoSlideTransition());
	}

}