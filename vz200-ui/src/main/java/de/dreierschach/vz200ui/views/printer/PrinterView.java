package de.dreierschach.vz200ui.views.printer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "printer", layout = MainView.class)
@PageTitle("Printer")
@CssImport("./styles/views/printer/printer-view.css")
@VaadinSessionScope
@JsModule("./js/copytoclipboard.js")
public class PrinterView extends View<PrinterPresenter> {
    public static final Logger log = LoggerFactory.getLogger(PrinterView.class);

    Button flushButton;
    Button clipboardButton;
    Button clearButton;
    TextArea printerOutput;

    @Override
    protected String getViewId() {
        return "printer-view";
    }

    @Override
    protected void createContent() {
        flushButton = new Button("flush", VaadinIcon.LAPTOP.create());
        ComponentFactory.withTooltip(flushButton, "flush printer");
        clipboardButton = new Button(VaadinIcon.COPY.create());
        ComponentFactory.withTooltip(clipboardButton, " copy to clipboard");
        clearButton = new Button(VaadinIcon.CLOSE_SMALL.create());
        ComponentFactory.withTooltip(clearButton, "clear output");

        printerOutput = new TextArea();
        printerOutput.setReadOnly(true);
        printerOutput.setMinWidth("40em");
        printerOutput.setMaxWidth("80em");
        printerOutput.setMinHeight("25em");
        printerOutput.addClassNames("print-style");

        add(new HorizontalLayout(flushButton, clipboardButton, clearButton), printerOutput);
    }

}
