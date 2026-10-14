package dev.joid.lib.ui.core.transition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Transition {

	@NonNull private final TransitionStep in;
	@NonNull private final TransitionStep out;

}