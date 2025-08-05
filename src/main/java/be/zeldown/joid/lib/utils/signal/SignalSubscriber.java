package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

@FunctionalInterface
public interface SignalSubscriber<T> {

	/**
	 * The update function in Java takes a non-null value and returns a boolean indicating whether the
	 * update was successful.
	 *
	 * @param value The parameter `value` is of type `T`, which is a generic type. The `@NonNull`
	 * annotation indicates that the value cannot be null. The method `update` takes this value as a
	 * parameter and returns a boolean indicating whether the update was successful or not.
	 * @return A boolean value is being returned.
	 */
	boolean update(final @NonNull T value);

}