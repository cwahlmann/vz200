package de.dreierschach.vz200ui.views.printer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.util.StringObject;
import de.dreierschach.vz200ui.views.Presenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SpringComponent
@VaadinSessionScope
public class PrinterPresenter extends Presenter<PrinterView> {
    public static final Logger log = LoggerFactory.getLogger(PrinterPresenter.class);

    private final Vz200Service vz200Service;
    private final Config config;
    private final StringObject output = new StringObject();
    private Binder<StringObject> binder;

    @Autowired
    public PrinterPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;
    }

    @Override
    protected void doBind() {
        binder = new Binder<>();
        binder.setBean(output);

        binder.bind(view.printerOutput, StringObject::getValue, StringObject::setValue);

        view.flushButton.addClickListener(event -> appendFlushPrinter());
        view.clearButton.addClickListener(event -> {
            output.clear();
            binder.readBean(output);
        });
        view.clipboardButton.addClickListener(
                event -> UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", output.getValue()));
    }

    private void appendFlushPrinter() {
        if (!output.isEmpty()) {
            output.append("\n");
        }
        output.append(vz200Service.getFlushPrinter());
        binder.readBean(output);
    }
}
