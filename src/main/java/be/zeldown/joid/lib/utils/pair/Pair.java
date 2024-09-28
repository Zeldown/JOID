package be.zeldown.joid.lib.utils.pair;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Pair<T> {

	private final T left;
	private final T right;

}