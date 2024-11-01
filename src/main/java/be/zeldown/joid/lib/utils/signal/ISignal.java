package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

/**
 * The `ISignal` interface in Java represents a signal with a generic type, allowing subscribers to receive
 * updates when the signal value changes.
 */
public interface ISignal<T> {

	/**
	 * The `getOrDefault` function returns the value associated with a key if it exists, or a default
	 * value if the key is not present.
	 * 
	 * @return The `getOrDefault()` method is likely returning a value of type T. The specific value being
	 * returned would depend on the implementation of the method and the context in which it is being
	 * called.
	 */
	T getOrDefault();

	/**
	 * The "reset" function in Java resets the state of an object or system.
	 */
	void reset();

	/**
	 * The function "set" in Java sets a value of type T as final.
	 * 
	 * @param value The `set` method takes a parameter `value` of type `T`, which is a generic type. This
	 * means that `value` can be of any data type, and it is marked as `final`, indicating that it cannot
	 * be reassigned within the method.
	 */
	void set(final T value);

	/**
	 * This Java function subscribes a non-null signal subscriber to receive non-null values of type T.
	 * 
	 * @param subscriber The `subscriber` parameter is an object of type `SignalSubscriber` that must not
	 * be null. It is used to subscribe to signals of type `T`.
	 */

	void subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);
	/**
	 * This Java function unsubscribes a non-null SignalSubscriber from a signal.
	 * 
	 * @param subscriber The `subscriber` parameter is an object of type `SignalSubscriber` that is not
	 * allowed to be null. It represents the subscriber that you want to unsubscribe from a signal or
	 * event.
	 */
	void unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);

	/**
	 * The function "publish" does not take any parameters and does not return any value.
	 */
	void publish();

	/**
	 * The function "silent" in Java does not take any parameters and does not return any value.
	 */
	void silent();

	/**
	 * The function isPresent() in Java returns a boolean value indicating whether a certain condition or
	 * element is present.
	 * 
	 * @return A boolean value is being returned.
	 */
	boolean isPresent();

}