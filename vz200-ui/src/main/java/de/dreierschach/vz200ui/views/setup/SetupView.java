package de.dreierschach.vz200ui.views.setup;

import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.theme.lumo.Lumo;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "setup", layout = MainView.class)
@PageTitle("Setup")
@CssImport("./styles/views/setup/setup-view.css")
@RouteAlias(value = "", layout = MainView.class)
@VaadinSessionScope
public class SetupView extends View<SetupPresenter> {
    public static final Logger log = LoggerFactory.getLogger(SetupView.class);

    ComboBox<String> selectHostComboBox;
    TextField portField;
    Button scanButton;
    Button testConnectionButton;

    Button applyButton;
    Button undoButton;
    AceEditor sourceEditor;
    ComboBox<AceTheme> themeComboBox;
    ComboBox<String> themeVariantComboBox;

    public SetupView() {
        super();
    }

    @Override
    protected String getViewId() {
        return "setup-view";
    }

    protected void createContent() {
        selectHostComboBox = new ComboBox<>("", "localhost");
        selectHostComboBox.setPlaceholder("IP / Hostname");
        selectHostComboBox.setWidth("10em");
        selectHostComboBox.setAllowCustomValue(true);
        portField = new TextField("", "8080", "port");
        portField.setMaxLength(5);
        portField.setWidth("4em");
        scanButton = new Button("Scan");
        testConnectionButton = new Button("Test", VaadinIcon.CONNECT.create());

        applyButton = new Button("Apply", VaadinIcon.CHECK.create());
        undoButton = new Button("Undo", VaadinIcon.CLOSE.create());

        sourceEditor = ComponentFactory.aceEditor("enter your assembler code here", AceTheme.ambiance, 10, 10, 16);
        sourceEditor.setReadOnly(true);
        themeComboBox = new ComboBox<>();
        themeComboBox.setItems(AceTheme.values());
        themeComboBox.setValue(AceTheme.ambiance);

        themeVariantComboBox = new ComboBox<>();
        themeVariantComboBox.setItems(Lumo.DARK, Lumo.LIGHT);
        themeVariantComboBox.setValue(Lumo.DARK);

        HorizontalLayout row0 = new HorizontalLayout(new Label("App-Theme:"), themeVariantComboBox);
        row0.setAlignItems(Alignment.BASELINE);

        HorizontalLayout row1 = new HorizontalLayout(selectHostComboBox, portField, scanButton, testConnectionButton);

        VerticalLayout col = new VerticalLayout(new Label("Editor-Theme:"), themeComboBox);
        col.setPadding(false);
        col.setWidth("12em");
        HorizontalLayout row2 = new HorizontalLayout(col, sourceEditor);

        HorizontalLayout row3 = new HorizontalLayout(applyButton, undoButton);

        add(row0, ComponentFactory.ruler(), row1, ComponentFactory.ruler(), row2, ComponentFactory.ruler(), row3);
    }
}
