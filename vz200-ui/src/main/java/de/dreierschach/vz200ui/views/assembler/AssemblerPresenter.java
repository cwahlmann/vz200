package de.dreierschach.vz200ui.views.assembler;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import de.f0rce.ace.enums.AceTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

@SpringComponent
@VaadinSessionScope
public class AssemblerPresenter extends Presenter<AssemblerView> {
    public static final Logger log = LoggerFactory.getLogger(AssemblerPresenter.class);
    public static final String LIB_SEPERATOR = "________LIB: ";

    private final Vz200Service vz200Service;
    private final Config config;

    @Autowired
    public AssemblerPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;
    }

    @Override
    protected void doBind() {
        view.sourceEditor.setTheme(config.getOrDefault(Config.ACE_THEME, AceTheme.ambiance.name()));
        view.installButton.addClickListener(event -> installToMemory(false));
        view.runButton.addClickListener(event -> installToMemory(true));
        view.downloadButton.addClickListener(event -> {
            if (!view.sourceEditor.hasChanged()) {
                downloadFromMemory();
                return;
            }
            ComponentFactory.confirm("Overwrite recent changes?", "Overwrite", "Cancel", this::downloadFromMemory,
                    () -> {
                    });
        });
        view.resetButton.addClickListener(event -> vz200Service.reset());
        view.confirmedUpload.withNeedsConfirmation(() -> view.sourceEditor.hasChanged())
                .withOnConfirmed(this::onUploadConfirmed).withOnDeclined(this::onUploadDeclined);
        view.setDownloadSupplier(() -> String.format("%s.asm", view.nameField.getValue()),
                this::provideSourceStream, () -> {
                });
        view.downloadFrom.addValueChangeListener(event -> {
            String value = event.getValue();
            if (!isHex(value)) {
                view.downloadFrom.setErrorMessage("please insert a hexadecimal value");
            } else {
                view.downloadFrom.setErrorMessage(null);
            }
        });

        view.downloadTo.addValueChangeListener(event -> {
            String value = event.getValue();
            if (!isHex(value)) {
                view.downloadTo.setErrorMessage("please insert a hexadecimal value");
            } else {
                view.downloadTo.setErrorMessage(null);
            }
        });

        view.setConvertSupplier(() -> String.format("%s.vz", view.nameField.getValue()),
                () -> new ByteArrayInputStream(convertToVz()), () -> {
                });
    }

    private ByteArrayInputStream provideSourceStream() {
        var vzSource = view.sourceEditor.getValue();
        StringBuilder sources = new StringBuilder();
        sources.append(vzSource.getSource()).append("\n");
        vzSource.getLibs().forEach(lib -> sources.append("\n").append(LIB_SEPERATOR).append(lib.getName()).append("\n").append(lib.getSource()));
        return new ByteArrayInputStream(sources.toString().getBytes(StandardCharsets.UTF_8));
    }

    private byte[] convertToVz() {
        VzSource vzSource = view.sourceEditor.getValue();
        vzSource.setType(VzSource.SourceType.asm);
        vzSource.setAutorun(true);
        VzSource result = vz200Service.convertTo(vzSource, VzSource.SourceType.vz);
        return Base64.getDecoder().decode(result.getSource());
    }

    private boolean isHex(String value) {
        return StringUtils.isNotEmpty(value) && value.matches("[0-9a-zA-Z]+");
    }

    private void installToMemory(boolean run) {
        try {
            VzSource vzSource = view.sourceEditor.getValue();
            vzSource.setType(VzSource.SourceType.asm);
            vzSource.setAutorun(run);
            vz200Service.saveAssemblerToMemory(vzSource);
            Notification.show("Install/Run successful.");
        } catch (Exception e) {
            log.warn("Install/Run failed", e);
            ComponentFactory.warning("Install/Run failed: " + e.getMessage());
        }
    }

    private void downloadFromMemory() {
        try {
            VzSource vzSource = vz200Service.loadAssemblerFromMemory(view.downloadFrom.getValue(),
                    view.downloadTo.getValue());
            view.sourceEditor.setValue(vzSource);
            ComponentFactory.info("Download successful.");
        } catch (Exception e) {
            ComponentFactory.warning("Download failed: " + e.getMessage());
        }
    }

    private void onUploadConfirmed() {
        try (InputStream in = view.getUploadStream(AssemblerView.UPLOAD_ID)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            VzSource source = new VzSource();
            source.setName(cutFilenameEnding(view.getUploadFilename(AssemblerView.UPLOAD_ID)));
            var line = reader.readLine();
            var s = new StringBuilder();
            String libName = "";
            boolean mainSource = true;
            while (line != null) {
                if (line.startsWith(LIB_SEPERATOR)) {
                    if (mainSource) {
                        source.setSource(s.toString());
                        mainSource = false;
                    } else {
                        var lib = new VzSource.Lib();
                        lib.setSource(s.toString());
                        lib.setName(libName);
                        source.getLibs().add(lib);
                    }
                    libName = line.substring(LIB_SEPERATOR.length());
                    s = new StringBuilder();
                } else {
                    s.append(line).append("\n");
                }
                line = reader.readLine();
            }
            if (mainSource) {
                source.setSource(s.toString().stripTrailing());
            } else {
                var lib = new VzSource.Lib();
                lib.setSource(s.toString());
                lib.setName(libName);
                source.getLibs().add(lib);
            }
            view.sourceEditor.setValue(source);
            view.resetUploadButton(AssemblerView.UPLOAD_ID);
            ComponentFactory.info("File successfull loaded.");
        } catch (IOException e) {
            ComponentFactory.warning("Error loading file: " + e.getMessage());
        }
    }

    private String cutFilenameEnding(String filename) {
        if (filename == null) {
            return "";
        }
        int index = filename.lastIndexOf(".");
        if (index < 0) {
            return filename;
        }
        if (index == 0) {
            return "";
        }
        return filename.substring(0, index);
    }

    private void onUploadDeclined() {
        ComponentFactory.info("Loading cancelled.");
        view.resetUploadButton(AssemblerView.UPLOAD_ID);
    }
}
