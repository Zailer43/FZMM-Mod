//package fzmm.zailer.me.compat.symbolChat.font;
//
//import io.wispforest.owo.ui.base.BaseComponent;
//import io.wispforest.owo.ui.core.AnimatableProperty;
//import io.wispforest.owo.ui.core.Positioning;
//import io.wispforest.owo.ui.core.Sizing;
//import net.minecraft.client.gui.widget.ClickableWidget;
//import net.minecraft.client.util.math.MatrixStack;
//
//public class FontSelectionDropDownComponent extends BaseComponent{
//
//    private final ClickableWidget selectionDropDown;
//
//    public FontSelectionDropDownComponent(Object fontSelectionDropDown) {
//        super();
//        this.selectionDropDown = (net.replaceitem.symbolchat.gui.widget.FontSelectionDropDownWidget) fontSelectionDropDown;
//    }
//
//    @Override
//    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
//        if (this.isExpanded())
//            this.selectionDropDown.render(matrices, mouseX, mouseY, delta);
//    }
//
//    @Override
//    public boolean onMouseDown(double mouseX, double mouseY, int button) {
//        if (this.isExpanded() && this.selectionDropDown.visible && this.selectionDropDown.mouseClicked(mouseX, mouseY, button))
//            return true;
//        return super.onMouseDown(mouseX, mouseY, button);
//    }
//
//    @Override
//    protected int determineHorizontalContentSize(Sizing sizing) {
//        return net.replaceitem.symbolchat.gui.SymbolSelectionPanel.WIDTH;
//    }
//
//    @Override
//    protected int determineVerticalContentSize(Sizing sizing) {
//        return net.replaceitem.symbolchat.gui.SymbolSelectionPanel.HEIGHT;
//    }
//
//    @Override
//    public AnimatableProperty<Positioning> positioning() {
//        return AnimatableProperty.of(Positioning.relative(0, 0));
//    }
//
//    public boolean isExpanded() {
//        return ((net.replaceitem.symbolchat.gui.widget.FontSelectionDropDownWidget) (this.selectionDropDown)).expanded;
//    }
//
//    public void setExpanded(boolean value) {
//        ((net.replaceitem.symbolchat.gui.widget.FontSelectionDropDownWidget) (this.selectionDropDown)).expanded = value;
//    }
//
//    public boolean isVisible() {
//        return this.selectionDropDown.visible;
//    }
//
//    public void setVisible(boolean value) {
//        this.selectionDropDown.visible = value;
//    }
//}
