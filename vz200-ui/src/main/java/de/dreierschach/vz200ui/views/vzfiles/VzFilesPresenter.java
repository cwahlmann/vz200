package de.dreierschach.vz200ui.views.vzfiles;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.service.VzFileInfo;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
        view.installButton.addClickListener(event -> install(false));
        view.runButton.addClickListener(event -> install(true));
        view.resetButton.addClickListener(event -> reset());
        refreshGrid();
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

    private void reset() {
        try {
            vz200Service.reset();
        } catch (Exception e) {
            ComponentFactory.warning("Unable to reset emulator: " + e.getMessage());
        }
    }

}
