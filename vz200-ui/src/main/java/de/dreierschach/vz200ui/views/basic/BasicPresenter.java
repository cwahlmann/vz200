package de.dreierschach.vz200ui.views.basic;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.util.GraphicCharUtil;
import de.dreierschach.vz200ui.views.Presenter;
import de.f0rce.ace.enums.AceTheme;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@SpringComponent
@VaadinSessionScope
public class BasicPresenter extends Presenter<BasicView> {

    private final Vz200Service vz200Service;
    private final Config config;

    private boolean changed = false;
    private final VzSource vzSource;
    private final Binder<VzSource> binder;

    @Autowired
    public BasicPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;
        vzSource = new VzSource();
        vzSource.setType(VzSource.SourceType.basic);
        vzSource.setName("UNKNOWN");
        vzSource.setAutorun(false);
        vzSource.setSource("");
        binder = new Binder<>();
    }


    @Override
    protected void doBind() {
        binder.setBean(vzSource);

        binder.bind(view.sourceEditor, vzSource -> mapGraphicCharsToEditor(vzSource.getSource()),
                (vzsource, source) -> vzsource.setSource(mapGraphicCharsFromEditor(source)));
        binder.bind(view.nameField, VzSource::getName, VzSource::setName);

        view.sourceEditor.addValueChangeListener(event -> {
            setChanged(true);
        });
        view.sourceEditor.setTheme(AceTheme.valueOf(config.getOrDefault(Config.ACE_THEME, AceTheme.ambiance.name())));

        view.installButton.addClickListener(event -> installToMemory(false));
        view.runButton.addClickListener(event -> installToMemory(true));
        view.downloadButton.addClickListener(event -> {
            if (!changed) {
                downloadFromMemory();
                return;
            }
            ComponentFactory.confirm("Overwrite recent changes?", "Overwrite", "Cancel", this::downloadFromMemory,
                    () -> {
                    });
        });
        view.resetButton.addClickListener(event -> vz200Service.reset());
        view.confirmedUpload.withNeedsConfirmation(() -> changed).withOnConfirmed(this::onUploadConfirmed)
                .withOnDeclined(this::onUploadDeclined);
        view.setDownloadSupplier(() -> String.format("%s.bas", view.nameField.getValue()),
                () -> new ByteArrayInputStream(vzSource.getSource().getBytes(StandardCharsets.UTF_8)),
                () -> this.setChanged(false));
        view.setConvertSupplier(() -> String.format("%s.vz", view.nameField.getValue()),
                () -> new ByteArrayInputStream(convertToVz()), () -> this.setChanged(false));
    }

    public String mapGraphicCharsToEditor(String value) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            if (value.charAt(i) == '_' && i < value.length() - 2) {
                int c = Integer.parseInt(value.substring(i + 1, i + 3), 16);
                if (GraphicCharUtil.GRAPHIC_CHARS.containsKey(c)) {
                    result.append(GraphicCharUtil.GRAPHIC_CHARS.get(c).getUnicodeChar());
                    i += 3;
                } else {
                    result.append(value.charAt(i));
                    i++;
                }
            } else {
                result.append(value.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    public String mapGraphicCharsFromEditor(String value) {
        StringBuilder result = new StringBuilder();
        Map<Character, Integer> unicodeToCharMap = GraphicCharUtil.GRAPHIC_CHARS.values().stream().collect(
                Collectors.toMap(GraphicCharUtil.GraphicChar::getUnicodeChar,
                        GraphicCharUtil.GraphicChar::getVz200Code));
        value.chars().forEach(c -> {
            if (unicodeToCharMap.containsKey((char) c)) {
                result.append(String.format("_%02X", unicodeToCharMap.get((char) c)));
            } else {
                result.append((char) c);
            }
        });
        return result.toString();
    }

    private byte[] convertToVz() {
        vzSource.setType(VzSource.SourceType.basic);
        VzSource result = vz200Service.convertTo(vzSource, VzSource.SourceType.vz);
        return Base64.getDecoder().decode(result.getSource());
    }

    private void installToMemory(boolean run) {
        try {
            vz200Service.saveBasicToMemory(vzSource);
            if (run) {
                vz200Service.type("run\n");
            }
            Notification.show("Install/Run successful.");
        } catch (Exception e) {
            ComponentFactory.warning("Install/Run failed: " + e.getMessage());
        }
    }

    private void downloadFromMemory() {
        try {
            VzSource vzSource = vz200Service.loadBasicFromMemory();
            this.vzSource.setName(vzSource.getName());
            this.vzSource.setSource(vzSource.getSource());
            binder.readBean(this.vzSource);
            setChanged(false);
            ComponentFactory.info("Download successful.");
        } catch (Exception e) {
            ComponentFactory.warning("Download failed: " + e.getMessage());
        }
    }

    private void onUploadConfirmed() {
        try (InputStream in = view.getUploadStream(BasicView.UPLOAD_ID)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            vzSource.setSource(reader.lines().collect(Collectors.joining("\n")));
            vzSource.setName(cutFilenameEnding(view.getUploadFilename(BasicView.UPLOAD_ID)));
            binder.readBean(this.vzSource);
            setChanged(false);
            view.resetUploadButton(BasicView.UPLOAD_ID);
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
        view.resetUploadButton(BasicView.UPLOAD_ID);
    }

    private void setChanged(boolean changed) {
        this.changed = changed;
        view.changedCheckbox.setValue(changed);
    }
}
