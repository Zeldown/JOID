package dev.joid.lib.bridge.render.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StencilState {

	public static final StencilState DISABLED = new StencilState(false, StencilFunction.ALWAYS, 0, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP);

	private final boolean          enabled;
	private final StencilFunction  function;
	private final int              reference;
	private final int              mask;
	private final StencilOperation fail;
	private final StencilOperation depthFail;
	private final StencilOperation pass;

	public static @NonNull StencilState create(final @NonNull StencilFunction function, final int reference, final int mask, final @NonNull StencilOperation fail, final @NonNull StencilOperation depthFail, final @NonNull StencilOperation pass) {
		return new StencilState(true, function, reference, mask, fail, depthFail, pass);
	}

}