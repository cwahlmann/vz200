package de.dreierschach.vz200ui.views.basic;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@SpringComponent
@VaadinSessionScope
public class BasicPresenter extends Presenter<BasicView> {

    private final Vz200Service vz200Service;
    private boolean changed = false;

    @Autowired
    public BasicPresenter(Vz200Service vz200Service) {
        this.vz200Service = vz200Service;
    }

    @Override
    protected void doBind() {
        view.sourceEditor.addValueChangeListener(event -> setChanged(true));
        view.installButton.addClickListener(event -> installToMemory(false));
        view.runButton.addClickListener(event -> installToMemory(true));
        view.downloadButton.addClickListener(event -> {
            if (!changed) {
                downloadFromMemory();
                return;
            }
            ComponentFactory
                    .confirm("Overwrite recent changes?", "Overwrite", "Cancel", this::downloadFromMemory, () -> {
                    });
        });
        view.resetButton.addClickListener(event -> vz200Service.reset());
        view.confirmedUpload.withNeedsConfirmation(() -> changed).withOnConfirmed(this::onUploadConfirmed)
                            .withOnDeclined(this::onUploadDeclined);
        view.setDownloadSupplier(() -> String.format("%s.bas", view.nameField.getValue()),
                                 () -> new ByteArrayInputStream(
                                         view.sourceEditor.getValue().getBytes(StandardCharsets.UTF_8)),
                                 () -> this.setChanged(false));
    }

    private void installToMemory(boolean run) {
        try {
            VzSource vzSource = new VzSource();
            vzSource.setType(VzSource.SourceType.basic);
            vzSource.setSource(view.sourceEditor.getValue());
            vzSource.setName(view.nameField.getValue());
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
            view.sourceEditor.setValue(vzSource.getSource());
            view.nameField.setValue(vzSource.getName());
            setChanged(false);
            ComponentFactory.info("Download successful.");
        } catch (Exception e) {
            ComponentFactory.warning("Download failed: " + e.getMessage());
        }
    }

    private void onUploadConfirmed() {
        try (InputStream in = view.getUploadStream(BasicView.UPLOAD_ID)) {
            view.sourceEditor.setValue("");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            view.sourceEditor.setValue(reader.lines().collect(Collectors.joining("\n")));
            view.nameField.setValue(cutFilenameEnding(view.getUploadFilename(BasicView.UPLOAD_ID)));
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
