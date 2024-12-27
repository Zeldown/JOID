package be.zeldown.joid.lib.ui.node.impl.structure.toggle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ToggleState<T, B> {

	private final T toggle;
	private final B back;

}