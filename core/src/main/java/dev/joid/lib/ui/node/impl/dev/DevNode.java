package dev.joid.lib.ui.node.impl.dev;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;
import lombok.Getter;
import lombok.NonNull;

public final class DevNode extends Node {

	private static final Color RED         = new Color(239, 57, 38).lock();
	private static final Color ACTION      = new Color(57, 120, 255).lock();

	private static final Color BLACK       = new Color(23, 23, 25).lock();
	private static final Color WHITE       = new Color(250, 250, 250).lock();
	private static final Color LIGHT_BLACK = new Color(56, 56, 62).lock();
	private static final Color LIGHT_WHITE = new Color(161, 161, 170).lock();

	private static final Color[] GRID_COLORS = {DevNode.ACTION, DevNode.RED};

	private final BooleanSignal gridSignal;
	private final BooleanSignal inspectSignal;

	private final Signal<Node>         inspectedNode;
	private final ComputedSignal<Node> lockedNode;
	private final BooleanSignal        inspectedNodeLocked;

	@Getter
	private final TweenAnimator reloadAnimator;

	private double inspectX;
	private double inspectY;
	private double inspectWidth;
	private double inspectHeight;
	private double targetInspectX;
	private double targetInspectY;
	private double targetInspectWidth;
	private double targetInspectHeight;

	private int gridColorIndex;

	private DevNode(final double x, final double y) {
		super(x, y, 275, 53);
		super.anchor(Align.END);

		this.inspectSignal = BooleanSignal.create(true);
		this.gridSignal = BooleanSignal.create(false);

		this.inspectedNode = Signal.create(null);
		this.inspectedNodeLocked = BooleanSignal.create();
		this.lockedNode = Signal.from(() -> this.inspectedNodeLocked.get() ? this.inspectedNode.get() : null);

		this.reloadAnimator = TweenAnimator.create(0F);

		Shortcut.create(Key.R).onTrigger(event -> super.getUi().reload()).attach(this);
		Shortcut.create(Key.I).onTrigger(event -> this.toggleInspect()).attach(this);
		Shortcut.create(Key.ENTER).onTrigger(event -> this.inspectParent()).attach(this);
		Shortcut.create(Key.NUMPAD_ENTER).onTrigger(event -> this.inspectParent()).attach(this);
		Shortcut.create(Key.G).onTrigger(event -> this.toggleGrid()).attach(this);
	}

	public static @NonNull DevNode create(final double x, final double y) {
		return new DevNode(x, y);
	}

	@Override
	public void init(final @NonNull UI ui) {
		super.clearChildren();
		final double lockedWidth = super.getDefaultWidth() + 200D;
		final double lockedHeight = 220D;

		FlexNode
		.horizontal(15, 0, super.getDefaultHeight() - 30)
		.margin(10)
		.body(flex -> {
			ResourceNode
			.create(0, 0, 24, 24)
			.resource(Resource.of("assets/dev/textures/icons/inspect.png"))
			.color(this.inspectSignal.map(inspect -> inspect ? DevNode.ACTION : DevNode.WHITE))
			.hoveredColor(this.inspectSignal.map(inspect -> (inspect ? DevNode.ACTION : DevNode.WHITE).darker(0.3F)))
			.onClick(event -> this.toggleInspect())
			.hover(() -> "[I] Inspect")
			.attach(flex);

			ResourceNode
			.create(0, 0, 24, 24)
			.resource(Resource.of("assets/dev/textures/icons/reload.png"))
			.color(() -> DevNode.WHITE.to(DevNode.ACTION, this.reloadAnimator.get()))
			.hoveredColor(() -> DevNode.WHITE.to(DevNode.ACTION, this.reloadAnimator.get()).darker(0.3F))
			.onClick(event -> {
				event.getNode().getUi().reload();
			})
			.hover(() -> "[R] Reload")
			.attach(flex);

			ResourceNode
			.create(0, 0, 24, 24)
			.resource(Resource.of("assets/dev/textures/icons/grid.png"))
			.color(this.gridSignal.map(grid -> grid ? DevNode.ACTION : DevNode.WHITE))
			.hoveredColor(this.gridSignal.map(grid -> (grid ? DevNode.ACTION : DevNode.WHITE).darker(0.3F)))
			.onClick(event -> this.toggleGrid())
			.hover(() -> "[G] Grid")
			.attach(flex);
		})
		.y(() -> super.getHeight() - super.getDefaultHeight() + 15D)
		.attach(this);

		RectNode
		.create(122, 0, 2, super.getDefaultHeight())
		.color(DevNode.LIGHT_BLACK)
		.y(() -> super.getHeight() - super.getDefaultHeight())
		.attach(this);

		RectNode
		.create(0, lockedHeight, lockedWidth, 2)
		.color(DevNode.LIGHT_BLACK)
		.visible(this.inspectedNodeLocked)
		.attach(this);

		ContainerNode
		.create(0, 0, lockedWidth, lockedHeight)
		.body(node -> {
			final Node inspectedNode = this.lockedNode.peek();
			if (inspectedNode == null) {
				return;
			}

			RectNode
			.create(0, 53, node.getWidth(), 2)
			.color(DevNode.LIGHT_BLACK)
			.attach(node);

			FlexNode
			.horizontal(10, 0, 53)
			.margin(15)
			.body(flex -> {
				TextNode
				.create(0, flex.dh(2))
				.text(Text.create(JOIDLog.nameOf(inspectedNode.getClass()), TextInfo.create(DevFont.MONTSERRAT, FontWeight.SEMI_BOLD, 17, DevNode.WHITE)).alignX(Align.START).alignY(Align.CENTER))
				.anchorY(Align.CENTER)
				.attach(flex);

				CircleNode
				.create(0, flex.dh(2) - 3.5D, 7)
				.color(DevNode.LIGHT_WHITE)
				.attach(flex);

				TextNode
				.create(0, flex.dh(2))
				.text(Text.create(String.format(Locale.ROOT, "%.2f", inspectedNode.getRenderTime() / 1000000F) + "ms", TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 15, DevNode.LIGHT_WHITE)).alignX(Align.START).alignY(Align.CENTER))
				.anchorY(Align.CENTER)
				.attach(flex);
			})
			.attach(node);

			ContainerNode
			.create(0, 53, lockedWidth, lockedHeight - 53)
			.overflow(OverflowProperty.SCROLL)
			.body(container -> {
				FlexNode
				.vertical(10, 10, container.aw(-20))
				.body(scroll -> {
					final TextInfo categoryInfo = TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 15, DevNode.LIGHT_WHITE);
					final TextInfo labelInfo = TextInfo.create(DevFont.MONTSERRAT, FontWeight.SEMI_BOLD, 15, DevNode.ACTION.brighter(0.3F));
					final TextInfo valueInfo = TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 15, DevNode.ACTION);

					TextNode
					.create(0, 0)
					.text(Text.create("Bounds", categoryInfo))
					.attach(scroll);

					final Object[][] bounds = {
						{"position", inspectedNode.getPosition()},
						{"x", inspectedNode.getX() + " / " + inspectedNode.getAbsoluteX() + " (" + inspectedNode.getDefaultX() + ")"},
						{"y", inspectedNode.getY() + " / " + inspectedNode.getAbsoluteY() + " (" + inspectedNode.getDefaultY() + ")"},
						{"width", inspectedNode.getWidth() + " (" + inspectedNode.getDefaultWidth() + ")"},
						{"height", inspectedNode.getHeight() + " (" + inspectedNode.getDefaultHeight() + ")"}
					};
					for (final Object[] bound : bounds) {
						TextNode
						.create(10D, 0D)
						.text(Text.create(bound[0] + ": ", labelInfo).add(Text.create(bound[1], valueInfo)))
						.attach(scroll);
					}

					if (!inspectedNode.getEventBus().isEmpty()) {
						RectNode
						.create(0, 0, 0, 5)
						.color(Color.TRANSPARENT)
						.attach(scroll);

						TextNode
						.create(0, 0)
						.text(Text.create("Listeners", categoryInfo))
						.attach(scroll);

						for (final EventType<?> type : inspectedNode.getEventBus().getTypes()) {
							TextNode
							.create(10, 0)
							.text(Text.create(type.getName() + ": ", labelInfo).add(Text.create(inspectedNode.getEventBus().count(type), valueInfo)))
							.attach(scroll);
						}
					}

					RectNode
					.create(0, 0, 0, 5)
					.color(Color.TRANSPARENT)
					.attach(scroll);

					TextNode
					.create(0, 0)
					.text(Text.create("Hierarchy", categoryInfo))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("hierarchy: ", labelInfo).add(Text.create(inspectedNode.getHierarchy(), valueInfo)))
					.onClick(event -> {
						if (inspectedNode.getParent() != null) {
							this.inspectedNode.set(inspectedNode.getParent());
							this.inspectedNodeLocked.set(true);
						}
					})
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("children: ", labelInfo).add(Text.create(inspectedNode.getChildren().size(), valueInfo)))
					.attach(scroll);

					for (final Node child : inspectedNode.getChildren()) {
						TextNode
						.create(20, 0)
						.text(Text.create(JOIDLog.nameOf(child.getClass()), valueInfo))
						.onClick(event -> {
							this.inspectedNode.set(child);
							this.inspectedNodeLocked.set(true);
						})
						.attach(scroll);
					}

					final List<Field> fields = new ArrayList<>();

					Class<?> clazz = inspectedNode.getClass();
					while (clazz != null) {
						if (clazz.equals(Node.class)) {
							break;
						}

						for (final Field field : clazz.getDeclaredFields()) {
							if (Modifier.isStatic(field.getModifiers())) {
								continue;
							}

							fields.add(field);
						}

						clazz = clazz.getSuperclass();
					}

					if (!fields.isEmpty()) {
						RectNode
						.create(0, 0, 0, 5)
						.color(Color.TRANSPARENT)
						.attach(scroll);

						TextNode
						.create(0, 0)
						.text(Text.create("Properties", categoryInfo))
						.attach(scroll);

						for (final Field field : fields) {
							try {
								field.setAccessible(true);
								final Object raw = field.get(inspectedNode);
								final Object value = raw instanceof Supplier ? SignalContext.current().untracked((Supplier<?>) raw) : raw;

								TextNode
								.create(10, 0)
								.text(Text.create(field.getName() + ": ", labelInfo).add(Text.create(value == null ? "null" : value, valueInfo)))
								.mode(TextMode.SPLIT)
								.width(scroll.aw(-20))
								.attach(scroll);
							} catch (final Exception e) {
								JOIDLog.warn("The dev panel cannot read the field " + field.getName() + " of " + JOIDLog.nameOf(inspectedNode.getClass()) + " (" + e + "), the panel leaves it out", e);
							}
						}
					}
				})
				.attach(container);
			})
			.attach(node);
		})
		.watch(this.lockedNode, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
		.visible(this.inspectedNodeLocked)
		.attach(this);

		TextNode
		.create(0, 0)
		.text(Text.create(() -> String.format(Locale.ROOT, "%.0f fps", super.getUi().getFps()), TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 17, DevNode.WHITE)).align(Align.CENTER))
		.anchor(Align.CENTER)
		.x(() -> 123D + super.aw(-123D) / 2D)
		.y(() -> super.getHeight() - super.getDefaultHeight() / 2D)
		.attach(this);
	}

	@Override
	public void update() {
		final boolean locked = this.inspectedNodeLocked.peek();
		super.layoutSize(super.getDefaultWidth() + (locked ? 200D : 0D), super.getDefaultHeight() + (locked ? 220D : 0D));
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.inspectSignal.peek()) {
			if (!this.inspectedNodeLocked.peek()) {
				this.inspectedNode.set(null);
				final List<Node> nodeList = super.getUi().getNodeListAt(mouseX, mouseY);
				if (!nodeList.isEmpty() && !(nodeList.get(0) instanceof DevNode) && !this.isAncestorOf(nodeList.get(0))) {
					this.inspectedNode.set(nodeList.get(UI.isCtrlKeyDown() ? nodeList.size() - 1 : 0));
				}
			}

			if (this.inspectedNode.peek() != null) {
				this.targetInspectX = this.inspectedNode.peek().getAbsoluteX();
				this.targetInspectY = this.inspectedNode.peek().getAbsoluteY();
				this.targetInspectWidth = this.inspectedNode.peek().getWidth();
				this.targetInspectHeight = this.inspectedNode.peek().getHeight();
			}

			this.inspectX = super.getUi().lerpByFramerate(this.inspectX, this.targetInspectX, 1D / 6D, 0.5D);
			this.inspectY = super.getUi().lerpByFramerate(this.inspectY, this.targetInspectY, 1D / 6D, 0.5D);
			this.inspectWidth = super.getUi().lerpByFramerate(this.inspectWidth, this.targetInspectWidth, 1D / 6D, 0.5D);
			this.inspectHeight = super.getUi().lerpByFramerate(this.inspectHeight, this.targetInspectHeight, 1D / 6D, 0.5D);

			if (this.inspectedNode.peek() != null) {
				this.drawInfoBox(this.inspectX, this.inspectY, this.inspectWidth, this.inspectHeight, this.inspectedNode.peek());
			}
		}

		if (this.gridSignal.peek()) {
			final Color gridColor = DevNode.GRID_COLORS[this.gridColorIndex];
			final TextInfo gridInfo = TextInfo.create(DevFont.MONTSERRAT, 15, gridColor).shadow().shadow(Color.BLACK.withAlpha(0.7F));
			DrawUtils.SHAPE.drawLine(gridColor, 3F, Point.of(UIView.DESIGN_WIDTH / 2D - 1D, 0), Point.of(UIView.DESIGN_WIDTH / 2D + 1D, UIView.DESIGN_HEIGHT));
			DrawUtils.SHAPE.drawLine(gridColor, 3F, Point.of(0, UIView.DESIGN_HEIGHT / 2D - 1D), Point.of(UIView.DESIGN_WIDTH, UIView.DESIGN_HEIGHT / 2D + 1D));

			DrawUtils.SHAPE.drawRect(0, mouseY, mouseX, 3, gridColor);
			DrawUtils.TEXT.drawText(mouseX / 2, mouseY + 5, Text.create((int) mouseX + "px", gridInfo).alignX(Align.CENTER));

			DrawUtils.SHAPE.drawRect(mouseX, mouseY, UIView.DESIGN_WIDTH - mouseX, 3, gridColor);
			DrawUtils.TEXT.drawText(mouseX + (UIView.DESIGN_WIDTH - mouseX) / 2, mouseY + 5, Text.create((int) (UIView.DESIGN_WIDTH - mouseX) + "px", gridInfo).alignX(Align.CENTER));

			DrawUtils.SHAPE.drawRect(mouseX, 0, 3, mouseY, gridColor);
			DrawUtils.TEXT.drawText(mouseX + 5, mouseY / 2, Text.create((int) mouseY + "px", gridInfo));

			DrawUtils.SHAPE.drawRect(mouseX, mouseY, 3, UIView.DESIGN_HEIGHT - mouseY, gridColor);
			DrawUtils.TEXT.drawText(mouseX + 5, mouseY + (UIView.DESIGN_HEIGHT - mouseY) / 2, Text.create((int) (UIView.DESIGN_HEIGHT - mouseY) + "px", gridInfo));
		}

		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), DevNode.BLACK, 10F);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed()) {
			return;
		}

		if (this.inspectSignal.peek() && this.inspectedNode.peek() != null) {
			if (event.getButton().isLeft() && !this.inspectedNodeLocked.peek()) {
				this.inspectedNodeLocked.set(true);
				event.consume();
			} else if (event.getButton().isRight() && this.inspectedNodeLocked.peek()) {
				this.inspectedNodeLocked.set(false);
				event.consume();
			}
		}

		if (event.getButton().isRight() && this.gridSignal.peek()) {
			this.gridColorIndex = (this.gridColorIndex + 1) % DevNode.GRID_COLORS.length;
		}
	}

	private void toggleInspect() {
		this.inspectSignal.set(!this.inspectSignal.peek());
		this.inspectedNode.set(null);
		this.inspectedNodeLocked.set(false);
	}

	private void toggleGrid() {
		this.gridSignal.set(!this.gridSignal.peek());
	}

	private void inspectParent() {
		if (this.inspectedNode.peek() == null || this.inspectedNode.peek().getParent() == null) {
			return;
		}

		this.inspectedNode.set(this.inspectedNode.peek().getParent());
		this.inspectedNodeLocked.set(true);
	}

	private boolean isAncestorOf(final Node node) {
		for (Node parent = node.getParent(); parent != null; parent = parent.getParent()) {
			if (parent == this) {
				return true;
			}
		}
		return false;
	}

	private void drawInfoBox(final double x, final double y, final double width, final double height, final Node node) {
		DrawUtils.SHAPE.drawRect(x, y, width, height, DevNode.ACTION.withAlpha(0.3F));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.of(x, y), Point.of(x + width, y));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.of(x, y), Point.of(x, y + height));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.of(x + width, y), Point.of(x + width, y + height));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.of(x, y + height), Point.of(x + width, y + height));

		final TextInfo textInfo = TextInfo.create(DevFont.MONTSERRAT, 15F, DevNode.WHITE).shadow().shadow(Color.BLACK.withAlpha(0.7F));
		final Text nodeNameText = Text.create(JOIDLog.nameOf(node.getClass()), textInfo);
		final Text nodeTimeText = Text.create(String.format(Locale.ROOT, "%.2f", node.getRenderTime() / 1000000F) + "ms", textInfo);

		final double margin = 15D;
		final double padding = 10D;

		final double infoBoxX = x;
		final double infoBoxY = y - 35D < 0D ? y + height + 5D : y - 35D;
		final double infoBoxWidth = margin + padding * 2D + nodeNameText.getWidth() + nodeTimeText.getWidth();

		DrawUtils.SHAPE.drawRoundedRect(infoBoxX, infoBoxY, infoBoxWidth, 30D, DevNode.BLACK.withAlpha(0.8F), 5F);

		double ox = infoBoxX + padding;
		ox += DrawUtils.TEXT.drawText(ox, infoBoxY + 15D, nodeNameText.alignY(Align.CENTER)).getWidth() + margin;
		DrawUtils.SHAPE.drawCircle(ox - margin / 2D + 1D, infoBoxY + 15D, DevNode.WHITE, 2D);
		DrawUtils.TEXT.drawText(ox, infoBoxY + 15D, nodeTimeText.alignY(Align.CENTER));
	}

}