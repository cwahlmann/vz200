package de.dreierschach.vz200ui.views.tapedeck;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.service.TapeInfo;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;

@Route(value = "tape", layout = MainView.class)
@PageTitle("Tapedeck")
@CssImport("./styles/views/tapedeck/tapedeck-view.css")
@VaadinSessionScope
public class TapedeckView extends View<TapedeckPresenter> {

    TextField nameField;
    TextField positionField;
    TextField positionCountField;

    Button rewindButton;
    Button fastForwardButton;
    Button recButton;
    Button playButton;
    Button stopButton;

    Button refreshButton;
    Button createButton;
    Button deleteButton;
    Button insertButton;

    Grid<TapeInfo> tapeInfoGrid;

    @Override
    protected String getViewId() {
        return "tapedeck-view";
    }

    @Override
    protected void createContent() {
        nameField = new TextField("Name");
        nameField.setReadOnly(true);
        positionField = new TextField("Track");
        positionField.setWidth("3em");
        positionField.setReadOnly(true);
        positionCountField = new TextField("# Tracks");
        positionCountField.setWidth("3em");
        positionCountField.setReadOnly(true);

        rewindButton = refreshButton = ComponentFactory.withTooltip(new Button(VaadinIcon.BACKWARDS.create()), "rewind");
        fastForwardButton = refreshButton = ComponentFactory
                .withTooltip(new Button(VaadinIcon.FORWARD.create()), "fast forward");
        playButton = refreshButton = ComponentFactory.withTooltip(new Button(VaadinIcon.PLAY.create()), "play");
        recButton = refreshButton = ComponentFactory.withTooltip(new Button(VaadinIcon.CIRCLE.create()), "record");
        stopButton = refreshButton = ComponentFactory.withTooltip(new Button(VaadinIcon.STOP.create()), "stop");

        VerticalLayout controlPanel = new VerticalLayout(new HorizontalLayout(nameField, positionField, positionField),
                                                         new HorizontalLayout(rewindButton, fastForwardButton, playButton,
                                                                              recButton, stopButton));
        controlPanel.setSpacing(false);
        controlPanel.setPadding(false);

        refreshButton = ComponentFactory.withTooltip(new Button(VaadinIcon.REFRESH.create()), "Refresh table");
        createButton = ComponentFactory.withTooltip(new Button(VaadinIcon.PLUS.create()), "Create new tape");
        deleteButton = ComponentFactory.withTooltip(new Button(VaadinIcon.CLOSE.create()), "delete selected tape");
        insertButton = ComponentFactory
                .withTooltip(new Button(VaadinIcon.DOWNLOAD_ALT.create()), "insert tape into tapedeck");
        HorizontalLayout buttonBar = new HorizontalLayout(refreshButton, createButton, deleteButton, insertButton);

        tapeInfoGrid = new Grid<>();
        tapeInfoGrid.addColumn(TapeInfo::getName).setHeader("Name").setSortProperty("Name").setFlexGrow(5);
        tapeInfoGrid.addColumn(t -> String.valueOf(t.getPosition())).setHeader("Track").setSortProperty("Track")
                    .setFlexGrow(1);
        tapeInfoGrid.addColumn(t -> String.valueOf(t.getPositionCount())).setHeader("# Tracks").setSortProperty("Size")
                    .setFlexGrow(1);
        tapeInfoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        tapeInfoGrid.setMinWidth("20em");
        tapeInfoGrid.setMaxWidth("40em");

        add(controlPanel, ComponentFactory.ruler(), buttonBar, tapeInfoGrid);
    }

    void setTapedeckMode(TapeInfo.Mode mode) {
        switch (mode) {
            case idle:
                rewindButton.setEnabled(true);
                fastForwardButton.setEnabled(true);
                recButton.setEnabled(true);
                recButton.removeClassName("record-style");
                playButton.setEnabled(true);
                playButton.removeClassName("play-style");
                stopButton.setEnabled(false);
                break;
            case play:
                rewindButton.setEnabled(false);
                fastForwardButton.setEnabled(false);
                recButton.setEnabled(false);
                recButton.removeClassName("record-style");
                playButton.setEnabled(false);
                playButton.addClassName("play-style");
                stopButton.setEnabled(true);
                break;
            case record:
                rewindButton.setEnabled(false);
                fastForwardButton.setEnabled(false);
                recButton.setEnabled(false);
                recButton.addClassName("record-style");
                playButton.setEnabled(false);
                playButton.removeClassName("play-style");
                stopButton.setEnabled(true);
                break;
            case off:
                rewindButton.setEnabled(false);
                fastForwardButton.setEnabled(false);
                recButton.setEnabled(false);
                recButton.removeClassName("record-style");
                playButton.setEnabled(false);
                playButton.removeClassName("play-style");
                stopButton.setEnabled(false);
        }
    }
}
