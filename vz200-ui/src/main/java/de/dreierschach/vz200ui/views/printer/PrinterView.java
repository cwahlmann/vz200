package de.dreierschach.vz200ui.views.printer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.dreierschach.vz200ui.views.main.MainView;

@Route(value = "printer", layout = MainView.class)
@PageTitle("Printer")
@CssImport("./styles/views/printer/printer-view.css")
public class PrinterView extends HorizontalLayout {

    private TextField name;
    private Button sayHello;
    private TextField a;
    private TextField b;
    private TextField c;
    private TextField d;
    private TextField e;
    private TextField f;

    public PrinterView() {
        setId("printer-view");
        name = new TextField("Your name");
        sayHello = new Button("Say hello");
        a = new TextField("Your name");
        b = new TextField("Your name");
        c = new TextField("Your name");
        d = new TextField("Your name");
        e = new TextField("Your name");
        f = new TextField("Your name");
        add(name, sayHello, a, b, c, d, e, f);
        setVerticalComponentAlignment(Alignment.END, name, sayHello);
        sayHello.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
        });
    }

}
