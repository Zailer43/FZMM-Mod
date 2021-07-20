/*package fzmm.zailer.me.client.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

public class ItemsScreen extends FzmmBaseScreen {
	private ItemOptionsListWidget optionsList;
	private ItemStack itemStack;

	protected ItemsScreen(ItemStack item) {
		super(new LiteralText("Items"));
		this.itemStack = item;
	}

	protected void init() {
		super.init();

		this.optionsList = new ItemOptionsListWidget(this.client, this.width / 2, this.height, 40, this.height - 100, 20);
	}

//	public void resize(MinecraftClient client, int width, int height) {
//		this.init(client, width, height);
//	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		this.optionsList.render(matrices, mouseX, mouseY, delta);
	}

}
*/