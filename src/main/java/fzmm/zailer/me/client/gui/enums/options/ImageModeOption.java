//package fzmm.zailer.me.client.gui.enums.options;
//
//import fi.dy.masa.malilib.config.IConfigOptionListEntry;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.text.Text;
//
//public enum ImageModeOption {
//    URL("url"),
//    PATH("path");
//
//    private final String name;
//
//    ImageModeOption(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public String getStringValue() {
//        return this.name;
//    }
//
//    @Override
//    public String getDisplayName() {
//        return Text.translatable("fzmm.gui.option.imageMode." + this.name).getString();
//    }
//
//    @Override
//    public IConfigOptionListEntry cycle(boolean forward) {
//        return this == URL ? PATH : URL;
//    }
//
//    @Override
//    public IConfigOptionListEntry fromString(String value) {
//        for (ImageModeOption option : ImageModeOption.values()) {
//            if (option.getStringValue().equalsIgnoreCase(value)) {
//                return option;
//            }
//        }
//
//        return URL;
//    }
//
//    public int getWidth() {
//        return MinecraftClient.getInstance().textRenderer.getWidth(this.getDisplayName());
//    }
//}