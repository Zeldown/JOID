package be.zeldown.joid.lib.resource.dto.decoder;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import lombok.NonNull;

public interface IResourceDecoder {

	public void init(final @NonNull ResourceData resource);
	public void prepare(final @NonNull ResourceData resource);
	public void decode(final @NonNull ResourceData resource);
	public void upload(final @NonNull ResourceData resource);
	public void bind(final @NonNull ResourceData resource);
	public void clear(final @NonNull ResourceData resource);

}