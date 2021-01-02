package de.dreierschach.vz200ui.views.setup;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@SpringComponent
@VaadinSessionScope
public class SetupPresenter extends Presenter<SetupView> {
    public static final Logger log = LoggerFactory.getLogger(SetupPresenter.class);

    private final Vz200Service vz200Service;
    private final Config config;
    private Binder<Config> binder;

    @Autowired
    public SetupPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;
    }

    @Override
    protected void doBind() {
        log.info("===>>> BIND SETUP-VIEW: " + view.toString());
        binder = new Binder<>();
        binder.setBean(config);
        binder.bind(view.hostnameField, c -> c.getOrDefault(Config.HOSTNAME, "localhost"),
                    (c, v) -> c.set(Config.HOSTNAME, v));
        binder.withValidator(c -> {
            String port = c.getOrDefault(Config.PORT, "8080");
            return StringUtils.isNotEmpty(port) && StringUtils.isNumeric(port);
        }, "port must be numeric")
              .bind(view.portField, c -> c.getOrDefault(Config.PORT, "8080"), (c, v) -> c.set(Config.PORT, v));

        view.applyButton.addClickListener(event -> saveConfig());
        view.undoButton.addClickListener(event -> loadConfig());

        view.testConnectionButton.addClickListener(event -> {
            String url = "http://" + view.hostnameField + ":" + view.portField + "/api/vz200/version";
            try {
                ComponentFactory.info("Connection established: " + vz200Service.getJemuVersion());
            } catch (Exception e) {
                ComponentFactory.warning("Connection failed: " + e.getMessage());
            }
        });
        loadConfig();
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
