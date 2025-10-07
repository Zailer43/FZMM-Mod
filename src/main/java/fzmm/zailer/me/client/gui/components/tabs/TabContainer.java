package fzmm.zailer.me.client.gui.components.tabs;

import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.history.IMemento;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.function.Consumer;

//TODO: add simple tab: all tabs share the same layout, this can be useful in Banner Editor and History
public class TabContainer extends EFlowLayout {
    private final HashMap<String, Component> tabs = new HashMap<>();
    private final EFlowLayout tabsView; // components of tabs, simplify childById
    private final List<ITab> parsedTabs = new ArrayList<>();
    private EFlowLayout contentLayout;
    @Nullable
    private ELabelComponent labelComponent;
    private String selectedTab;

    public TabContainer(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
        this.tabsView = EContainers.horizontalFlow(Sizing.content(), Sizing.content());
        this.gap(4);
    }

    @Override
    public <T extends Component> T childById(@NotNull Class<T> expectedClass, @NotNull String id) {
        T result = this.tabsView.childById(expectedClass, id);
        if (result != null) return result;

        return super.childById(expectedClass, id);
    }

    public void selectTab() {
        this.selectTab(this.selectedTab().getId());
    }

    public void selectTab(ITab tab) {
        this.selectTab(tab.getId());
    }

    private void selectTab(String id) {
        Component tabComponent = this.tabs.get(id);
        if (tabComponent == null) throw new NullPointerException("Tab '" + id + "' of '" + this.id() + "' has no component");

        String previousTab = this.selectedTab;
        this.selectedTab = id;

        if (this.labelComponent != null) {
            String translationKey = this.selectedTab().getTranslationKey();
            this.labelComponent.text(Text.translatable(translationKey)).tooltip(Text.translatable(translationKey + ".tooltip"));
        }

        this.contentLayout.<EFlowLayout>configure(layout -> {
            // remove current tab of tabsView because it can't be mounted in two places at the same time
            this.tabsView.removeChild(tabComponent);
            layout.clearChildren();
            layout.child(tabComponent);
            if (previousTab != null) {
                this.tabsView.child(this.tabs.get(previousTab));
            }

            ParentComponent rootComponent = this.root();
            if (rootComponent == null) return;

            this.updateButton(rootComponent, id, previousTab);
        });
    }

    private void updateButton(ParentComponent rootComponent, String selectedTab, String previousTab) {
        EButtonComponent button = rootComponent.childById(EButtonComponent.class, selectedTab + "-button");
        if (button != null) {
            button.active(false);
        }

        if (previousTab == null || previousTab.equals(selectedTab)) return;
        EButtonComponent previousButton = rootComponent.childById(EButtonComponent.class, previousTab + "-button");
        if (previousButton != null) {
            previousButton.active(true);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ITab> T selectedTab() {
        for (var tab : this.parsedTabs) {
            if (tab.getId().equals(this.selectedTab)) return (T) tab;
        }
        return (T) this.parsedTabs.get(0);
    }

    public Set<String> tabIds() {
        return this.tabs.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends ITab> void setupTabs(EFlowLayout rootComponent, String selectedId, Consumer<T> onPress) {
        EButtonComponent selectedButton = null;
        for (var tab : this.parsedTabs) {
            String id = tab.getId();
            EButtonComponent buttonComponent = rootComponent.childByIdOrThrow(EButtonComponent.class, id + "-button");
            buttonComponent.verticalSizing(Sizing.fixed(16));
            buttonComponent.setMessage(tab.getButtonText());

            buttonComponent.onPress(button -> {
                this.selectTab(id);
                onPress.accept((T) tab);

                for (var tabId : this.tabIds()) {
                    rootComponent.childByIdOrThrow(EButtonComponent.class, tabId + "-button").active(true);
                }
                buttonComponent.active(false);
            });
            tab.setupComponents(rootComponent);

            if (selectedId.equals(id)) {
                selectedButton = buttonComponent;
            }
        }

        if (selectedButton != null) {
            selectedButton.onPress();
        }
    }

    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.backup());
        output.writeObject(this.selectedTab);
    }

    private Map<String, byte[]> backup() {
        Map<String, byte[]> mementoTabHashMap = new HashMap<>();
        for (var tab : this.parsedTabs) {
            if (tab instanceof IMemento mementoTab) {
                mementoTabHashMap.put(tab.getId(), mementoTab.backup());
            }
        }
        return mementoTabHashMap;
    }

    @SuppressWarnings("unchecked")
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.restore((Map<String, byte[]>) input.readObject());
        this.selectTab((String) input.readObject());
    }

    private void restore(Map<String, byte[]> mementoTabHashMap) {
        for (var tab : this.parsedTabs) {
            if (tab instanceof IMemento mementoTab) {
                mementoTab.restore(mementoTabHashMap.get(tab.getId()));
            }
        }
    }

    public void addParsedTabs(List<? extends ITab> tabs) {
        this.parsedTabs.addAll(tabs);
        if (this.parsedTabs.size() != this.tabs.size()) {
            throw new IllegalStateException("Tabs of '" + this.id() + "' are mismatching (expected tabs: " + this.parsedTabs.size() + ", actual components: " + this.tabs.size() + ")");
        }

        for (var tab : this.tabs.keySet()) {
            if (this.tabs.get(tab) == null) {
                throw new IllegalStateException("Tab '" + tab + "' of '" + this.id() + "' has no component");
            }
        }
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        List<Element> tabComponentList = UIParsing
                .get(children, "tabs", e -> UIParsing.<Element>allChildrenOfType(e, Node.ELEMENT_NODE))
                .orElse(Collections.emptyList());

        for (var tabComponent : tabComponentList) {
            if (tabComponent == null) throw new NullPointerException("Component of tab container '" + this.id() + "' is null");
            Component component = model.parseComponent(Component.class, tabComponent);
            this.tabsView.child(component);
            this.tabs.put(component.id(), component);
        }

        // both are optional
        this.labelComponent = this.childById(ELabelComponent.class, "tab-label");
        this.contentLayout = this.childById(EFlowLayout.class, "tab-content");

        if (this.contentLayout == null) {
            this.contentLayout = EContainers.verticalFlow(Sizing.content(), Sizing.content());
            this.child(this.contentLayout);
        }
    }

    public static EFlowLayout parse(Element element) {
        UIParsing.expectAttributes(element, "direction");

        return switch (element.getAttribute("direction")) {
            case "horizontal" -> EContainers.tabHorizontal(Sizing.content(), Sizing.content());
            case "ltr-text-flow" -> EContainers.tabLtrTextFlow(Sizing.content(), Sizing.content());
            default -> EContainers.tabVertical(Sizing.content(), Sizing.content());
        };
    }
}
