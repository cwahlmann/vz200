package de.dreierschach.vz200ui.views.printer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.util.GraphicCharUtil;
import de.dreierschach.vz200ui.util.StringObject;
import de.dreierschach.vz200ui.views.Presenter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        output.append(maskUnprintableChars(vz200Service.getFlushPrinter()));
        binder.readBean(output);
    }

    private String maskUnprintableChars(String s) {
        char[] chars = s.toCharArray();
        int p = 0;
        StringBuilder result = new StringBuilder();
        while (p < chars.length) {
            if (chars[p] >= 32 && chars[p] < 127) {
                result.append(chars[p]);
                p++;
            } else if (chars[p] == 0x0a) {
                result.append("\n");
                p++;
            } else if (chars[p] == 8 && p + 7 < chars.length) {
                int c0 = chars[p + 1];
                int c1 = chars[p + 4];
                int cc = (c1 & 0x01) * 4 | ((c1 >> 3) & 0x01) | (c0 & 0x01) * 8 | ((c0 >> 3) & 0x01) * 2 | 0x80;
                result.append(GraphicCharUtil.GRAPHIC_CHARS.get(cc).getUnicodeChar());
                //                result.append(String.format("_%02X", cc));
                p += 8;
            } else {
                result.append(String.format("_%02X", (int) chars[p]));
                p++;
            }
        }
        return result.toString();
    }
}
