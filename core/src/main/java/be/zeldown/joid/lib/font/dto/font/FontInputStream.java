package be.zeldown.joid.lib.font.dto.font;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class FontInputStream {

	@NonNull private final InputStream data;
	@NonNull private final InputStream texture;

}