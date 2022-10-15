//package fzmm.zailer.me.client.gui.wrapper;
//
//import fi.dy.masa.malilib.config.IConfigBase;
//import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
//import net.minecraft.text.Text;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//public class OptionWrapper extends ConfigOptionWrapper {
//    @Nullable private String[] translationValues;
//
//    private OptionWrapper(IConfigBase config) {
//        super(config);
//        this.translationValues = null;
//    }
//
//    public OptionWrapper(String label) {
//        super(label);
//        this.translationValues = new String[0];
//    }
//
//    @Override
//    public String getLabel() {
//        if (super.getLabel() == null)
//            return "";
//
//        Text translate = this.translationValues == null ?
//                Text.translatable(super.getLabel()) :
//                Text.translatable(super.getLabel(), (Object[]) this.translationValues);
//
//        return translate.getString();
//    }
//
//    public void setTranslationValues(String... translationValues) {
//        this.translationValues = translationValues;
//    }
//
//    public static List<ConfigOptionWrapper> createFor(Collection<? extends IConfigBase> configs) {
//        List<ConfigOptionWrapper> list = new ArrayList<>();
//
//        for (IConfigBase config : configs)
//            list.add(new OptionWrapper(config));
//
//        return list;
//    }
//}
