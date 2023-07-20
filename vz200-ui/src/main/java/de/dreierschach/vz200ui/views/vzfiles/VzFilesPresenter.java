package de.dreierschach.vz200ui.views.vzfiles;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.service.VzFileInfo;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringComponent
@VaadinSessionScope
public class VzFilesPresenter extends Presenter<VzFilesView> {
    private final Vz200Service vz200Service;

    private List<VzFileInfo> fileInfos;

    public VzFilesPresenter(Vz200Service vz200Service) {
        this.vz200Service = vz200Service;
        fileInfos = new ArrayList<>();
    }

    @Override
    protected void doBind() {
        view.refreshButton.addClickListener(event -> refreshGrid());

        view.inputStreamSupplier = () -> view.grid.getSelectedItems().stream().findAny().map(VzFileInfo::getId)
                                                  .map(this::getVzInputStream).orElse(null);
        view.filenameSupplier = () -> view.grid.getSelectedItems().stream().findAny().map(VzFileInfo::getName)
                                               .map(n -> n + ".vz").orElse("unknown.vz");
        view.confirmedUpload.withOnConfirmed(this::onUpload);

        view.installButton.addClickListener(event -> install(false));
        view.runButton.addClickListener(event -> install(true));
        view.deleteButton.addClickListener(event -> remove());
        view.resetButton.addClickListener(event -> reset());
        refreshGrid();
    }

    private void onUpload() {
        int id = 0;
        Set<Integer> ids = fileInfos.stream().map(VzFileInfo::getId).collect(Collectors.toSet());
        while (id < 256 && ids.contains(id)) {
            id++;
        }
        if (id > 255) {
            ComponentFactory.warning("There are no free slot anymore. Please remove a file to get space.");
            return;
        }
        try (InputStream in = view.getUploadStream(VzFilesView.UPLOAD_ID)) {
            String base64 = Base64.getEncoder().encodeToString(in.readAllBytes());
            VzSource vzSource = new VzSource();
            vzSource.setSource(base64);
            vzSource.setName(cutFilenameEnding(view.getUploadFilename(VzFilesView.UPLOAD_ID)));
            vzSource.setType(VzSource.SourceType.vz);
            vz200Service.putVzFile(id, vzSource);
            ComponentFactory.info("File successfull loaded on slot " + id + ".");
            refreshGrid();
        } catch (IOException e) {
            ComponentFactory.warning("Error loading file to emulator on slot " + id + ": " + e.getMessage());
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

    private InputStream getVzInputStream(int id) {
        try {
            VzSource source = vz200Service.getVzFile(id, VzSource.SourceType.vz);
            return new ByteArrayInputStream(Base64.getDecoder().decode(source.getSource()));
        } catch (Exception e) {
            view.getUI().ifPresent(ui -> ui.access(
                    () -> ComponentFactory.warning("unable to load file with id '" + id + "': " + e.getMessage())));
            return null;
        }
    }

    private void refreshGrid() {
        try {
            fileInfos = vz200Service.getVzFileInfos();
            view.grid.setItems(fileInfos);
            view.grid.recalculateColumnWidths();
        } catch (Exception e) {
            ComponentFactory.warning("Unable to load VZ-File-Infos: " + e.getMessage());
        }
    }

    private void install(boolean autorun) {
        view.grid.getSelectedItems().stream().findAny().map(VzFileInfo::getId).ifPresent(id -> {
            try {
                vz200Service.installVzFileFromDir(id, autorun);
                if (autorun) {
                    vz200Service.type("run\n");
                }
            } catch (Exception e) {
                ComponentFactory.warning("Unable to install / run vz-file '" + id + "': " + e.getMessage());
            }
        });
    }

    private void remove() {
        view.grid.getSelectedItems().stream().findAny().ifPresent(info -> {
            ComponentFactory
                    .confirm("Do you really want to remove '" + info.getName() + "' on slot " + info.getId() + "?",
                             "Remove", "Cancel", () -> {
                                try {
                                    vz200Service.deleteVzFile(info.getId());
                                    ComponentFactory.info("File '" + info.getName() + "' on slot " + info.getId() +
                                                          " successfull removed.");
                                    refreshGrid();
                                } catch (Exception e) {
                                    ComponentFactory.warning(
                                            "Unable to remove '" + info.getName() + "' on slot " + info.getId() + ": " +
                                            e.getMessage());
                                }
                            }, () -> {
                            });
        });
    }

    private void reset() {
        try {
            vz200Service.reset();
        } catch (Exception e) {
            ComponentFactory.warning("Unable to reset emulator: " + e.getMessage());
        }
    }

}
