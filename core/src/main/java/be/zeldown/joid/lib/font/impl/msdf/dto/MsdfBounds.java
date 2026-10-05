package be.zeldown.joid.lib.font.impl.msdf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class MsdfBounds {

	private final float left;
	private final float bottom;
	private final float right;
	private final float top;

}