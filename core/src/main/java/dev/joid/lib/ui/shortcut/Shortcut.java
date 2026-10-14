package dev.joid.lib.ui.shortcut;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.MapMaker;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.resolver.KeyResolver;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shortcut {

	private static final EventType<ShortcutEvent> TRIGGER = EventType.create("onTrigger");

	private static final Map<Object, List<Shortcut>> OWNER_MAP = new MapMaker().weakKeys().makeMap();

	private final Object   binding;
	private final EventBus eventBus;

	private boolean alt;
	private boolean ctrl;
	private boolean shift;
	private boolean overrideFocus;

	private WeakReference<UI>   ui;
	private WeakReference<Node> node;

	private Shortcut(final Object binding) {
		if (!KeyResolver.supports(binding)) {
			JOIDLog.devWarnOnce("Shortcut.binding." + binding.getClass().getName() + "." + binding, () -> "No key resolver knows the shortcut binding " + binding + " of type " + binding.getClass().getName() + ", the shortcut never fires: pass a Key or register an IKeyResolver first");
		}

		this.binding = binding;
		this.eventBus = EventBus.create(() -> "Shortcut");
	}

	public static @NonNull List<@NonNull Shortcut> getShortcutList(final @NonNull UI ui) {
		return Shortcut.list(ui);
	}

	public static @NonNull List<@NonNull Shortcut> getShortcutList(final @NonNull Node node) {
		return Shortcut.list(node);
	}

	public static @NonNull Shortcut create(final @NonNull Object binding) {
		return new Shortcut(binding);
	}

	public @NonNull Shortcut alt(final boolean alt) {
		this.alt = alt;
		return this;
	}

	public @NonNull Shortcut ctrl(final boolean ctrl) {
		this.ctrl = ctrl;
		return this;
	}

	public @NonNull Shortcut shift(final boolean shift) {
		this.shift = shift;
		return this;
	}

	public @NonNull Shortcut overrideFocus(final boolean overrideFocus) {
		this.overrideFocus = overrideFocus;
		return this;
	}

	public @NonNull Shortcut onTrigger(final @NonNull EventListener<ShortcutEvent> listener) {
		return this.onTrigger(EventPriority.NORMAL, listener);
	}

	public @NonNull Shortcut onTrigger(final @NonNull EventPriority priority, final @NonNull EventListener<ShortcutEvent> listener) {
		this.eventBus.listen(Shortcut.TRIGGER, priority, listener);
		return this;
	}

	public void fireTrigger(final @NonNull Key key) {
		if (this.eventBus.has(Shortcut.TRIGGER)) {
			this.eventBus.fire(Shortcut.TRIGGER, ShortcutEvent.create(key, this));
		}
	}

	public @NonNull Shortcut attach(final @NonNull UI ui) {
		this.detach();
		this.ui = new WeakReference<>(ui);
		Shortcut.register(ui, this);
		return this;
	}

	public @NonNull Shortcut attach(final @NonNull Node node) {
		this.detach();
		this.node = new WeakReference<>(node);
		Shortcut.register(node, this);
		return this;
	}

	public @NonNull Shortcut detach() {
		Shortcut.unregister(this.getUi(), this);
		Shortcut.unregister(this.getNode(), this);
		this.ui = null;
		this.node = null;
		return this;
	}

	public boolean isAttached() {
		return this.getUi() != null || this.getNode() != null;
	}

	public boolean isTriggeredBy(final @NonNull Key key) {
		return this.isAttached() && KeyResolver.supports(this.binding) && KeyResolver.resolve(this.binding) == key && this.alt == UI.isAltKeyDown() && this.ctrl == UI.isCtrlKeyDown() && this.shift == UI.isShiftKeyDown();
	}

	public UI getUi() {
		return this.ui != null ? this.ui.get() : null;
	}

	public Node getNode() {
		return this.node != null ? this.node.get() : null;
	}

	private static List<Shortcut> list(final Object owner) {
		final List<Shortcut> list = Shortcut.OWNER_MAP.get(owner);
		return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
	}

	private static void register(final Object owner, final Shortcut element) {
		Shortcut.OWNER_MAP.computeIfAbsent(owner, key -> new CopyOnWriteArrayList<>()).add(element);
	}

	private static void unregister(final Object owner, final Shortcut element) {
		final List<Shortcut> list = owner != null ? Shortcut.OWNER_MAP.get(owner) : null;
		if (list != null) {
			list.remove(element);
		}
	}

}