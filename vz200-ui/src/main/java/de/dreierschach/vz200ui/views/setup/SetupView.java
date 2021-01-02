package de.dreierschach.vz200ui.views.setup;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
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

    TextField hostnameField;
    TextField portField;
    Button applyButton;
    Button undoButton;
    Button testConnectionButton;

    public SetupView() {
        super();
        log.info("===>>> SETUP VIEW NEW INSTANCE: " + this.toString());
    }

    @Override
    protected String getViewId() {
        return "setup-view";
    }

    protected void createContent() {
        hostnameField = new TextField("", "localhost", "Hostname");
        hostnameField.setWidth("10em");
        portField = new TextField("", "8080", "Port");
        portField.setMaxLength(5);
        portField.setWidth("5em");
        applyButton = new Button("Apply", VaadinIcon.CHECK.create());
        undoButton = new Button("Undo", VaadinIcon.CLOSE.create());
        testConnectionButton = new Button("Test Connection", VaadinIcon.CONNECT.create());

        HorizontalLayout row1 = new HorizontalLayout(hostnameField, portField);
        HorizontalLayout row2 = new HorizontalLayout(applyButton, undoButton, testConnectionButton);

        add(row1, row2);
    }

}
