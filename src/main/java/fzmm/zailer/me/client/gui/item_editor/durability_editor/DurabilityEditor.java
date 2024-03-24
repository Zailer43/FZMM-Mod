package fzmm.zailer.me.client.gui.item_editor.durability_editor;

import fzmm.zailer.me.builders.DurabilityBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class DurabilityEditor implements IItemEditorScreen {
    private RequestedItem stackRequested = null;
    private List<RequestedItem> requestedItems = null;
    private final DurabilityBuilder builder = DurabilityBuilder.builder();
    private SliderWidget damageSlider;
    private ConfigTextBox damageTextBox;
    private LabelComponent maxDamageLabel;
    private ConfigTextBox percentageTextBox;
    private CheckboxComponent unbreakableCheckbox;
    private boolean disableCallbacks;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.stackRequested = new RequestedItem(
                // ItemStack.isDamageable return false if it has Unbreakable tag
                stack -> stack.getMaxDamage() > 0,
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.durability.item"),
                true
        );

        this.requestedItems = List.of(this.stackRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return DurabilityBuilder.builder()
                .of(Items.GOLDEN_HOE.getDefaultStack())
                .doDamagePercentage(50)
                .get();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.disableCallbacks = true;

        // first row
        this.damageSlider = editorLayout.childById(SliderWidget.class, "damage-slider");
        BaseFzmmScreen.checkNull(this.damageSlider, "fzmm.number-slider", "damage-slider");
        this.damageSlider.valueType(Integer.class);
        this.damageSlider.min(0);
        this.damageSlider.max(100);
        this.damageSlider.decimalPlaces(2);
        this.damageSlider.invertSlider();

        this.damageSlider.onChanged().subscribe(value -> {
            if (!this.disableCallbacks) {
                int damageValue = (int) Math.ceil(this.damageSlider.discreteValue());
                damageValue = this.builder.maxDamage() - damageValue;
                this.builder.damage(damageValue);
                this.updateItemPreview();

                this.disableCallbacks = true;
                this.damageTextBox.text(String.valueOf(damageValue));
                this.percentageTextBox.text(String.valueOf(this.builder.getDamagePercentage(2)));
                this.disableCallbacks = false;
            }
        });

        // second row
        this.damageTextBox = editorLayout.childById(ConfigTextBox.class, "damage");
        BaseFzmmScreen.checkNull(this.damageTextBox, "fzmm.text-option", "damage");
        this.damageTextBox.configureForNumber(Integer.class);
        this.damageTextBox.onChanged().subscribe(value -> {
            if (!this.disableCallbacks) {
                int damageValue = (int) this.damageTextBox.parsedValue();
                this.builder.damage(damageValue);
                this.updateItemPreview();

                this.disableCallbacks = true;
                this.damageSlider.setDiscreteValueWithoutCallback(damageValue);
                this.percentageTextBox.text(String.valueOf(this.builder.getDamagePercentage(2)));
                this.disableCallbacks = false;
            }
        });

        this.maxDamageLabel = editorLayout.childById(LabelComponent.class, "max-damage");
        BaseFzmmScreen.checkNull(this.maxDamageLabel, "label", "max-damage");

        this.percentageTextBox = editorLayout.childById(ConfigTextBox.class, "damage-percentage");
        BaseFzmmScreen.checkNull(this.percentageTextBox, "fzmm.text-option", "damage-percentage");
        this.percentageTextBox.configureForNumber(Double.class);
        this.percentageTextBox.onChanged().subscribe(value -> {
            if (!this.disableCallbacks) {
                double percentageValue = (double) this.percentageTextBox.parsedValue();
                this.builder.damagePercentage(percentageValue);
                this.updateItemPreview();

                int damageValue = this.builder.damage();
                this.disableCallbacks = true;
                this.damageSlider.setDiscreteValueWithoutCallback(damageValue);
                this.damageTextBox.text(String.valueOf(damageValue));
                this.disableCallbacks = false;
            }
        });

        // third row
        this.unbreakableCheckbox = editorLayout.childById(CheckboxComponent.class, "unbreakable");
        BaseFzmmScreen.checkNull(this.unbreakableCheckbox, "checkbox", "unbreakable");
        this.unbreakableCheckbox.onChanged(aBoolean -> {
            if (!this.disableCallbacks) {
                this.builder.unbreakable(aBoolean);
                this.updateItemPreview();
            }
        });

        this.disableCallbacks = false;
        return editorLayout;
    }

    @Override
    public String getId() {
        return "durability";
    }

    @Override
    public void updateItemPreview() {
        this.stackRequested.setStack(this.builder.get());
        this.stackRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.builder.of(stack);

        this.disableCallbacks = true;

        int maxDamage = this.builder.maxDamage();
        int damage = this.builder.damage();

        this.damageSlider.max(Math.max(1, maxDamage));
        this.damageSlider.setDiscreteValueWithoutCallback(damage);
        this.damageTextBox.text(String.valueOf(damage));
        this.maxDamageLabel.text(Text.of(String.valueOf(maxDamage)));
        this.percentageTextBox.text(String.valueOf(this.builder.getDamagePercentage(2)));
        this.unbreakableCheckbox.checked(this.builder.unbreakable());

        this.disableCallbacks = false;
    }
}
