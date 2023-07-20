package de.dreierschach.vz200ui.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.shared.Registration;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceTheme;
import org.apache.commons.lang3.StringUtils;

import javax.xml.transform.Source;
import java.util.*;
import java.util.function.Consumer;

@Tag("SourceEditor")
public class SourceEditor extends Component implements HasComponents, HasValue<HasValue.ValueChangeEvent<VzSource>, VzSource> {
    private Tabs sourceTabs;
    private Map<SourceTab, AceEditor> sourceEditors;
    private Div sourcePage;
    private SourceTab mainTab;
    private Tab newButtonTab;
    private String theme = AceTheme.ambiance.name();

    public SourceEditor() {
        sourceEditors = new HashMap<>();
        sourcePage = new Div();
        sourceTabs = new Tabs();
        add(sourceTabs, sourcePage);
        init();
    }

    private void init() {
        newButtonTab = new Tab(VaadinIcon.PLUS.create());
        mainTab = addNewTab(0, "MAIN");
        sourceTabs.add(newButtonTab);

        sourceTabs.addSelectedChangeListener(event -> {
            if (sourceEditors.containsKey(sourceTabs.getSelectedTab())) {
                sourceEditors.forEach((key, value) -> value.setVisible(key.equals(sourceTabs.getSelectedTab())));
                return;
            }
            if (newButtonTab.equals(sourceTabs.getSelectedTab())) {
                addNewTab(sourceTabs.getSelectedIndex(), getNewFilename());
            }
        });
    }

    public void clear() {
        sourceEditors.clear();
        sourcePage.removeAll();
        sourceTabs.removeAll();
        init();
    }

    @Override
    public void setValue(VzSource source) {
        clear();
        sourceEditors.get(mainTab).setValue(source.getSource());
        mainTab.withName(source.getName());
        for (VzSource.Lib lib : source.getLibs()) {
            Tab newTab = addNewTab(sourceTabs.getComponentCount() - 1, lib.getName());
            sourceEditors.get(newTab).setValue(lib.getSource());
        }
        clearChanged();
        sourceEditors.keySet().forEach(tab -> setChanged(tab, false));
    }

    @Override
    public VzSource getValue() {
        VzSource result = new VzSource();
        result.setSource(sourceEditors.get(mainTab).getValue());
        result.setName(mainTab.getName());
        List<VzSource.Lib> libs = new ArrayList<>();
        for (int i = 1; i < sourceTabs.getComponentCount() - 1; i++) {
            SourceTab tab = (SourceTab) sourceTabs.getComponentAt(i);
            var lib = new VzSource.Lib();
            lib.setName(tab.getName());
            lib.setSource(sourceEditors.get(tab).getValue());
            libs.add(lib);
        }
        result.setLibs(libs);
        return result;
    }


    public boolean hasChanged() {
        return sourceEditors.keySet().stream().anyMatch(SourceTab::isChanged);
    }

    public void clearChanged() {
        sourceEditors.keySet().stream().forEach(tab -> tab.withChanged(false));
    }

    private void setChanged(SourceTab tab, boolean changed) {
        tab.withChanged(changed);
    }

    private String getNewFilename() {
        String newFileName = "NEW";
        if (sourceEditors.keySet().stream().map(SourceTab::getName).noneMatch(newFileName::equals)) {
            return newFileName;
        }
        int index = 2;
        while (sourceEditors.keySet().stream().map(SourceTab::getName).anyMatch(("NEW_" + index)::equals)) {
            index++;
        }
        return "NEW_" + index;
    }

    public SourceTab addNewTab(int index, String filename) {
        AceEditor newSourceEditor = ComponentFactory.aceEditor("enter your assembler code here", AceTheme.ambiance,
                44, 40);
        newSourceEditor.setTheme(AceTheme.valueOf(theme));
        var newTab = new SourceTab().withName(filename);
        sourceEditors.put(newTab, newSourceEditor);
        newSourceEditor.addValueChangeListener(value -> setChanged(newTab, true));
        sourcePage.add(newSourceEditor);
        sourceTabs.addComponentAtIndex(index, newTab);
        sourceTabs.setSelectedTab(newTab);
        return newTab;
    }

    public void setTheme(String theme) {
        this.theme = theme;
        sourceEditors.values().forEach(e -> e.setTheme(AceTheme.valueOf(theme)));
    }

    @Override
    public Registration addValueChangeListener(HasValue.ValueChangeListener valueChangeListener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean b) {
        // noop
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean b) {
        // noop
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
