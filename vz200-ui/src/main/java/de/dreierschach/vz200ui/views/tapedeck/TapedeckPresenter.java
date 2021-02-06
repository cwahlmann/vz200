package de.dreierschach.vz200ui.views.tapedeck;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.service.TapeInfo;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@SpringComponent
@VaadinSessionScope
public class TapedeckPresenter extends Presenter<TapedeckView> {
    private final Vz200Service vz200Service;

    private List<TapeInfo> tapeInfos = new ArrayList<>();
    private TapeInfo currentTape = null;

    @Autowired
    public TapedeckPresenter(Vz200Service vz200Service) {
        this.vz200Service = vz200Service;
    }

    @Override
    protected void doBind() {
        view.refreshButton.addClickListener(event -> {
            refreshTapeInfos();
            refreshTapedeckPanel();
        });
        view.createButton.addClickListener(event -> createTape());
        view.deleteButton.addClickListener(event -> deleteTape());
        view.tapeInfoGrid.addSelectionListener(e -> refreshGridButtons());
        view.tapeInfoGrid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                insertTape(e.getItem());
            }
        });

        view.rewindButton.addClickListener(e -> rewind());
        view.fastForwardButton.addClickListener(e -> fastForward());
        view.playButton.addClickListener(e -> play());
        view.recButton.addClickListener(e -> record());
        view.stopButton.addClickListener(e -> stop());

        refreshTapeInfos();
        refreshTapedeckPanel();
        refreshGridButtons();
    }

    private void refreshTapeInfos() {
        try {
            tapeInfos = vz200Service.getAllTapeInfos();
            UI.getCurrent().access(() -> {
                view.tapeInfoGrid.setItems(tapeInfos);
                view.tapeInfoGrid.recalculateColumnWidths();
            });
        } catch (Exception e) {
            ComponentFactory.warning("Unable to load tape infos: " + e.getMessage());
        }
    }

    private void refreshTapedeckPanel() {
        try {
            currentTape = vz200Service.getCurrentTapeInfo();
            view.nameField.setValue(currentTape.getName());
            view.positionField.setValue(String.valueOf(currentTape.getPosition()));
            view.positionCountField.setValue(String.valueOf(currentTape.getPositionCount()));
            view.setTapedeckMode(currentTape.getMode());
        } catch (Exception e) {
            currentTape = null;
            view.nameField.setValue("");
            view.positionField.setValue("");
            view.positionCountField.setValue("");
            view.setTapedeckMode(TapeInfo.Mode.off);
            ComponentFactory.warning("Unable to load tape infos: " + e.getMessage());
        }
    }

    private void refreshGridButtons() {
        TapeInfo selected = view.tapeInfoGrid.getSelectedItems().stream().findAny().orElse(null);
        view.deleteButton.setEnabled(selected != null && !"default".equals(selected.getName()));
        view.insertButton.setEnabled(
                selected != null && (currentTape == null || !selected.getName().equals(currentTape.getName())));
    }

    private void createTape() {
        ComponentFactory.singleInput("Enter a name for the new tape.", "Name", name -> {
            try {
                if (tapeInfos.stream().anyMatch(t -> t.getName().equals(name))) {
                    ComponentFactory
                            .warning("There is already a tape with the name '" + name + "'. Please try another one.");
                    return;
                }
                vz200Service.createTape(name);
                refreshTapeInfos();
            } catch (Exception e) {
                ComponentFactory.warning("Unable to create new tape: " + e.getMessage());
            }
        }, () -> {
        });
    }

    private void deleteTape() {
        TapeInfo info = view.tapeInfoGrid.getSelectedItems().stream().findAny().orElse(null);
        if (info == null) {
            return;
        }
        ComponentFactory.confirm("Really delete tape '" + info.getName() + "'?", "Delete", "Cancel", () -> {
            try {
                vz200Service.deleteTape(info.getName());
                ComponentFactory.info("Tape '" + info.getName() + "' successfully deleted.");
                refreshTapeInfos();
            } catch (Exception e) {
                ComponentFactory.warning("Unable to delete type '" + info.getName() + "': " + e.getMessage());
            }
        }, () -> {
        });
    }

    private void insertTape(TapeInfo t) {
        if (currentTape.getMode() != TapeInfo.Mode.off && currentTape.getMode() != TapeInfo.Mode.idle) {
            ComponentFactory.warning("Please stop current tape before replacing it.");
            return;
        }
        try {
            vz200Service.insertTape(t.getName());
            refreshTapedeckPanel();
            refreshGridButtons();
            ComponentFactory.info("Tape successfully inserted.");
        } catch (Exception e) {
            ComponentFactory.warning("Error inserting tape: " + e.getMessage());
        }
    }

    private void rewind() {
        if (currentTape.getPosition() == 0) {
            return;
        }
        try {
            vz200Service.reelTape(currentTape.getPosition() - 1);
            refreshTapedeckPanel();
            refreshTapeInfos();
            ComponentFactory.info("Previous track located.");
        } catch (Exception e) {
            ComponentFactory.warning("Error rewinding tape: " + e.getMessage());
        }
    }

    private void fastForward() {
        try {
            vz200Service.reelTape(currentTape.getPosition() + 1);
            refreshTapedeckPanel();
            refreshTapeInfos();
            ComponentFactory.info("Next track located.");
        } catch (Exception e) {
            ComponentFactory.warning("Error fast forwarding tape: " + e.getMessage());
        }
    }

    private void play() {
        try {
            vz200Service.playTape();
            refreshTapedeckPanel();
            refreshGridButtons();
            refreshTapeInfos();
            ComponentFactory.info("Playback started.");
        } catch (Exception e) {
            ComponentFactory.warning("Error starting playback: " + e.getMessage());
        }
    }

    private void record() {
        try {
            vz200Service.recordTape();
            refreshTapedeckPanel();
            refreshGridButtons();
            refreshTapeInfos();
            ComponentFactory.info("Recording started.");
        } catch (Exception e) {
            ComponentFactory.warning("Error starting recording: " + e.getMessage());
        }
    }

    private void stop() {
        try {
            vz200Service.stopTape();
            refreshTapedeckPanel();
            refreshGridButtons();
            refreshTapeInfos();
            ComponentFactory.info("Tape stopped.");
        } catch (Exception e) {
            ComponentFactory.warning("Error stopping tape: " + e.getMessage());
        }
    }
}
