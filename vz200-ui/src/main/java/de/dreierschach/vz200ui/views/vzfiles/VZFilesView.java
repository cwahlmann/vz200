package de.dreierschach.vz200ui.views.vzfiles;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.dreierschach.vz200ui.views.main.MainView;

@Route(value = "files", layout = MainView.class)
@PageTitle("VZ-Files")
@CssImport("./styles/views/vzfiles/v-z-files-view.css")
public class VZFilesView extends HorizontalLayout {

    private TextField name;
    private Button sayHello;

    public VZFilesView() {
        setId("v-z-files-view");
        name = new TextField("Your name");
        sayHello = new Button("Say hello");
        add(name, sayHello);
        setVerticalComponentAlignment(Alignment.END, name, sayHello);
        sayHello.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
        });
    }

}
