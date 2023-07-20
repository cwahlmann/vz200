package de.dreierschach.vz200ui.views.vzfiles;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.service.VzFileInfo;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.util.ConfirmedUpload;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;
import org.vaadin.stefan.LazyDownloadButton;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

@Route(value = "files", layout = MainView.class)
@PageTitle("VZ-Files")
@CssImport("./styles/views/vzfiles/vz-files-view.css")
@VaadinSessionScope
public class VzFilesView extends View<VzFilesPresenter> {
    static final String UPLOAD_ID = "UPLOAD_ID";

    Button refreshButton;
    Button deleteButton;
    Button downloadButton;
    Button openAsBasicButton;
    Button openAsAssemblerButton;
    Button installButton;
    Button runButton;
    Button resetButton;

    Grid<VzFileInfo> grid;

    Supplier<String> filenameSupplier = () -> "unknown.vz";
    Supplier<InputStream> inputStreamSupplier = () -> null;
    Runnable onDownload = () -> {
    };
    ConfirmedUpload confirmedUpload;

    @Override
    protected String getViewId() {
        return "vz-files-view";
    }

    @Override
    protected void createContent() {
        refreshButton = ComponentFactory.withTooltip(new Button(VaadinIcon.REFRESH.create()), "refresh files list");
        deleteButton = ComponentFactory.withTooltip(new Button(VaadinIcon.CLOSE.create()), "delete file...");
        downloadButton = new LazyDownloadButton("Download", VaadinIcon.DOWNLOAD.create(), () -> filenameSupplier.get(),
                                                () -> inputStreamSupplier.get());

        openAsBasicButton = ComponentFactory
                .withTooltip(new Button("Basic...", VaadinIcon.FILE_TEXT_O.create()), "open file as basic");
        openAsAssemblerButton = ComponentFactory
                .withTooltip(new Button("Assembler...", VaadinIcon.FILE_TEXT_O.create()), "open file as assembler");

        installButton = ComponentFactory
                .withTooltip(new Button("Install", VaadinIcon.LAPTOP.create()), "install vz-file");
        runButton = ComponentFactory.withTooltip(new Button("Run", VaadinIcon.LAPTOP.create()), "run vz-file");
        resetButton = ComponentFactory.withTooltip(new Button("Reset", VaadinIcon.LAPTOP.create()), "reset emulator");

        confirmedUpload = new ConfirmedUpload().withIcon(VaadinIcon.UPLOAD).withDropCaption("Drop file")
                                               .withButtonCaption("Save");

        grid = new Grid<>();
        Grid.Column<VzFileInfo> idColumn = grid.addColumn(VzFileInfo::getId).setHeader("ID").setSortProperty("ID")
                                               .setWidth("2em");
        grid.addColumn(VzFileInfo::getName).setHeader("Name").setSortProperty("Name").setWidth("12em");
        grid.addColumn(info -> String.format("%04x", info.getStart())).setHeader("Start").setSortProperty("Start")
            .setWidth("3em");
        grid.addColumn(VzFileInfo::getLength).setHeader("Size").setSortProperty("Size").setWidth("4em");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.sort(List.of(new GridSortOrder<>(idColumn, SortDirection.ASCENDING)));
        grid.setHeightByRows(true);
        grid.setMaxWidth("40em");
        add(refreshButton, deleteButton, openAsBasicButton, openAsAssemblerButton, installButton, runButton,
            resetButton, downloadButton, new Label(""));
        addAndExpand(grid);

        setVerticalComponentAlignment(Alignment.END, refreshButton, deleteButton, openAsBasicButton,
                                      openAsAssemblerButton, installButton, runButton, resetButton, downloadButton,
                                      grid);
        addUpload(UPLOAD_ID, this, 8, confirmedUpload);
    }
}
