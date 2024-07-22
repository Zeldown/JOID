package be.zeldown.joid.lib.utils.tuple;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Tuple<F, S> {

	private final F first;
	private final S second;

}