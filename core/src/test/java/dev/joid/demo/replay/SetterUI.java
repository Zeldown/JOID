package dev.joid.demo.replay;

import javax.vecmath.Vector3f;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.model.ModelNode;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.design.text.TextNodeTest.TextFont;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNodeTest.Checkbox;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNodeTest.Selector;
import dev.joid.lib.ui.node.impl.structure.slider.SliderNodeTest.IntegerSlider;
import dev.joid.lib.ui.node.impl.structure.slider.SliderNodeTest.Thumb;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNodeTest.Switch;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNodeTest.Toggle;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;

@Getter
public class SetterUI extends UI {

	private final TextInfo      info  = TextInfo.create(new TextFont(), 10F);
	private final IntegerSignal step  = IntegerSignal.of(0);
	private final BooleanSignal muted = new BooleanSignal(false);

	private Switch             named;
	private RectNode           rect;
	private TextNode           text;
	private Toggle             toggle;
	private FlexNode           flex;
	private TextNode           state;
	private TextNode           label;
	private Switch             indexed;
	private ModelNode          model;
	private CircleNode         circle;
	private Checkbox           checkbox;
	private Selector           selector;
	private TextFieldNode      field;
	private IntegerSlider      slider;
	private RadarChartData     radar;
	private ProgressNode       progress;
	private BorderNodeEffect   border;
	private ShadowNodeEffect   shadow;
	private ResourcePlayerNode player;

	@Override
	public void init() {
		this.rect = RectNode
		.create(0D, 0D, 10D, 10D)
		.color(this.step.get() > 0 ? Color.GREEN : Color.GRAY)
		.hoveredColor(this.step.get() > 0 ? Color.RED : null)
		.borderColor(this.step.get() > 0 ? Color.BLUE : Color.WHITE)
		.borderStroke(this.step.get() * 2D)
		.borderFill(this.step.get() == 0)
		.x(this.step.get() * 10D)
		.y(5D + this.step.get())
		.width(10D + this.step.get())
		.height(20D - this.step.get())
		.visible(this.step.get() < 5)
		.enabled(this.step.get() < 4)
		.zlevel(this.step.get() * 3D)
		.attach(this);
		this.circle = CircleNode
		.create(0D, 0D, 10D)
		.color(this.step.get() > 0 ? Color.GREEN : Color.GRAY)
		.anchorX(this.step.get() > 0 ? Align.END : Align.START)
		.attach(this);
		this.text = TextNode
		.create(0D, 0D)
		.text(Text.create("Step " + this.step.get(), this.info))
		.mode(this.step.get() > 0 ? TextMode.SPLIT : TextMode.NORMAL)
		.attach(this);
		this.state = TextNode.create(0D, 0D).text(Text.create(this.muted.get() ? "Muted" : "Sound on", this.info)).attach(this);
		this.label = TextNode.create(0D, 0D).text(Text.create("Muted " + this.muted.get(), this.info)).attach(this);
		this.progress = ProgressNode
		.create(0D, 0D, 100D, 10D)
		.progress(this.step.get() / 4F)
		.direction(this.step.get() > 0 ? ProgressDirection.RIGHT_TO_LEFT : ProgressDirection.LEFT_TO_RIGHT)
		.foreground(this.step.get() > 0 ? Color.GREEN : Color.WHITE)
		.attach(this);
		this.player = ResourcePlayerNode
		.create(0D, 0D)
		.volume(1F - this.step.get() / 10F)
		.loop(this.step.get() > 0)
		.location(new Vector3f(this.step.get(), 0F, 1F))
		.attach(this);
		this.model = ModelNode
		.create(0D, 0D, 10D, 10D)
		.size(1D + this.step.get())
		.rotationYaw(this.step.get() * 90D)
		.attach(this);
		this.field = TextFieldNode
		.create(0D, 0D, 100D)
		.info(this.info)
		.<TextFieldNode>text("Name " + this.step.get())
		.<TextFieldNode>placeholder("Type " + this.step.get())
		.<TextFieldNode>maxTextLength(10 + this.step.get())
		.<TextFieldNode>marginLeft(this.step.get() * 2D)
		.attach(this);
		this.checkbox = new Checkbox().checked(this.step.get() > 0).attach(this);
		this.toggle = new Toggle().state("on", 0).toggle(this.step.get() > 0).attach(this);
		this.indexed = new Switch().states("low", "medium", "high").index(this.step.get() % 3).attach(this);
		this.named = new Switch().states("low", "medium", "high").state(this.step.get() > 0 ? "high" : "low").attach(this);
		this.selector = new Selector()
		.values("first", "first", "second", "third")
		.value(this.step.get() > 0 ? "second" : "first")
		.active(this.step.get() > 1)
		.attach(this);
		this.slider = new IntegerSlider().values(1, 9, 1).thumb(new Thumb()).value(1 + this.step.get()).attach(this);
		this.flex = FlexNode
		.vertical(0D, 0D, 100D)
		.margin(this.step.get() * 5D)
		.align(this.step.get() > 0 ? Align.CENTER : null)
		.attach(this);
		this.border = BorderNodeEffect
		.create(Color.WHITE, 1F)
		.width(1F + this.step.get())
		.color(this.step.get() > 0 ? Color.RED : Color.WHITE)
		.fill(this.step.get() == 0);
		this.shadow = ShadowNodeEffect
		.create(Color.BLACK, 2F)
		.offsetX(this.step.get() * 1D)
		.blur(2F + this.step.get());
		this.radar = RadarChartData
		.create("a")
		.value(this.step.get() * 10)
		.label("Label " + this.step.get());
	}

}