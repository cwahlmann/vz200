package de.dreierschach.vz200ui.views.setup;

import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.theme.lumo.Lumo;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@SpringComponent
@VaadinSessionScope
public class SetupPresenter extends Presenter<SetupView> {
    public static final Logger log = LoggerFactory.getLogger(SetupPresenter.class);

    private final Vz200Service vz200Service;
    private final Config config;
    private Binder<Config> binder;
    private final Set<String> knownDevices = new HashSet<>() {{
        add("localhost");
    }};
    private int testDevicesProgress = 0;

    @Autowired
    public SetupPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;
    }

    @Override
    protected void doBind() {
        switchTheme(getTheme(config));

        binder = new Binder<>();
        binder.setBean(config);

        binder.bind(view.themeVariantComboBox, this::getTheme, this::setTheme);

        view.selectHostComboBox.setItems(knownDevices);

        binder.bind(view.selectHostComboBox, c -> c.getOrDefault(Config.HOSTNAME, "localhost"), (c, v) -> {
            if (StringUtils.isNotEmpty(v)) {
                c.set(Config.HOSTNAME, v);
            }
        });

        view.selectHostComboBox.addCustomValueSetListener(event -> {
            knownDevices.add(event.getDetail());
            view.selectHostComboBox.setItems(knownDevices);
            view.selectHostComboBox.setValue(event.getDetail());
        });

        view.scanButton.addClickListener(event -> {
            String hostname = getBaseNet(view.selectHostComboBox.getValue());
            int port = StringUtils.isNumeric(view.portField.getValue()) ? Integer.parseInt(view.portField.getValue())
                                                                        : 8080;
            log.info("Scanning for emulator on http://" + hostname + ".*:" + port + "...");
            this.testDevicesProgress = 0;
            vz200Service.scanForDevices(hostname, port, this::onDeviceFound, this::onTestDevicesProgress);
        });

        binder.withValidator(c -> {
            String port = c.getOrDefault(Config.PORT, "8080");
            return StringUtils.isNotEmpty(port) && StringUtils.isNumeric(port);
        }, "port must be numeric")
              .bind(view.portField, c -> c.getOrDefault(Config.PORT, "8080"), (c, v) -> c.set(Config.PORT, v));

        view.applyButton.addClickListener(event -> saveConfig());
        view.undoButton.addClickListener(event -> loadConfig());

        view.testConnectionButton.addClickListener(event -> {
            try {
                ComponentFactory.info("Connection established: " + vz200Service.getJemuVersion());
            } catch (Exception e) {
                ComponentFactory.warning("Connection failed: " + e.getMessage());
            }
        });

        view.sourceEditor.setValue(SetupConstants.SOURCE_EXAMPLE);
        view.themeComboBox.addValueChangeListener(event -> view.sourceEditor.setTheme(event.getValue()));
        binder.bind(view.themeComboBox,
                    c -> AceTheme.valueOf(c.getOrDefault(Config.ACE_THEME, AceTheme.ambiance.toString())),
                    (c, v) -> c.set(Config.ACE_THEME, v.name()));
        loadConfig();
    }

    private void onDeviceFound(String adr) {
        UI ui = view.getUI().orElse(null);
        if (ui == null) {
            return;
        }
        ui.access(() -> {
            this.knownDevices.add(adr);
            view.selectHostComboBox.setItems(knownDevices);
            view.selectHostComboBox.setValue(adr);
            ComponentFactory.info("device found at " + adr);
            ui.push();
        });
    }

    private void onTestDevicesProgress(String adr) {
        UI ui = view.getUI().orElse(null);
        if (ui == null) {
            return;
        }
        ui.access(() -> {
            testDevicesProgress++;
            if (testDevicesProgress < 254) {
                view.scanButton.setText(String.format("%3d%%", 100 * testDevicesProgress / 254));
            } else {
                view.scanButton.setText("");
            }
            ui.push();
        });
    }

    private String getBaseNet(String hostname) {
        if (!StringUtils.isEmpty(hostname)) {
            String[] parts = hostname.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2];
            }
        }
        return "192.168.1";
    }

    private void setTheme(Config config, String variant) {
        config.set(Config.APP_THEME_VARIANT, variant);
        switchTheme(variant);
    }

    private String getTheme(Config config) {
        return config.getOrDefault(Config.APP_THEME_VARIANT, Lumo.DARK);
    }

    private void switchTheme(String variant) {
        UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute(\"theme\",\"" + variant + "\")");
    }

    private void loadConfig() {
        try {
            config.load();
            binder.setBean(config);
            ComponentFactory.info("Configuration loaded.");
        } catch (IOException e) {
            ComponentFactory.danger("Unable to load configuration: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try {
            config.save();
            ComponentFactory.info("Configuration saved.");
        } catch (IOException e) {
            ComponentFactory.danger("Unable to save configuration: " + e.getMessage());
        }
    }
}
