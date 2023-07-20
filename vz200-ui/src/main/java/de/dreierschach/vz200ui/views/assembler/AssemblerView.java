package de.dreierschach.vz200ui.views.assembler;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.util.ConfirmedUpload;
import de.dreierschach.vz200ui.views.SourceEditor;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;
import org.vaadin.stefan.LazyDownloadButton;

import java.io.InputStream;
import java.util.function.Supplier;


@Route(value = "assembler", layout = MainView.class)
@PageTitle("Assembler")
@CssImport("./styles/views/assembler/assembler-view.css")
@VaadinSessionScope
public class AssemblerView extends View<AssemblerPresenter> {
    static final String UPLOAD_ID = "UPLOAD_ID";

    SourceEditor sourceEditor;

    Button installButton;
    Button runButton;
    TextField downloadFrom;
    TextField downloadTo;
    Button downloadButton;
    Button resetButton;
    Checkbox changedCheckbox;
    TextField nameField;

    LazyDownloadButton saveButton;
    Supplier<String> filenameSupplier = () -> "source.asm";
    Supplier<InputStream> inputStreamSupplier = () -> null;
    Runnable onDownload = () -> {
    };

    LazyDownloadButton convertToVzButton;
    Supplier<String> convertFilenameSupplier = () -> "default.vz";
    Supplier<InputStream> convertInputStreamSupplier = () -> null;
    Runnable onDownloadVz = () -> {
    };

    ConfirmedUpload confirmedUpload;

    // libs

    ComboBox<String> libSelectComboBox;
    Button removeLibButton;

    @Override
    protected String getViewId() {
        return "assembler-view";
    }

    protected void createContent() {
        nameField = new TextField("", "", "Name");

        installButton = new Button("Install", VaadinIcon.LAPTOP.create());
        runButton = new Button("Run", VaadinIcon.LAPTOP.create());
        downloadFrom = new TextField("", "from");
        downloadFrom.setMaxLength(4);
        downloadFrom.setWidth("4em");
        downloadTo = new TextField("", "to");
        downloadTo.setMaxLength(4);
        downloadTo.setWidth("4em");
        downloadButton = new Button("Download", VaadinIcon.LAPTOP.create());
        resetButton = new Button("Reset", VaadinIcon.LAPTOP.create());
        saveButton = new LazyDownloadButton("Save", VaadinIcon.DOWNLOAD.create(), () -> filenameSupplier.get(),
                () -> inputStreamSupplier.get());
        saveButton.addDownloadStartsListener(event -> this.onDownload.run());
        convertToVzButton = new LazyDownloadButton("Save as VZ", VaadinIcon.DOWNLOAD.create(),
                () -> convertFilenameSupplier.get(), () -> convertInputStreamSupplier.get());
        convertToVzButton.addDownloadStartsListener(event -> this.onDownloadVz.run());
        changedCheckbox = new Checkbox("Changed", false);
        changedCheckbox.setReadOnly(true);

        libSelectComboBox = ComponentFactory.withTooltip(new ComboBox<>(), "select source to edit");
        libSelectComboBox.setAllowCustomValue(true);

        removeLibButton = ComponentFactory.withTooltip(new Button(VaadinIcon.CLOSE_SMALL.create()),
                "remove lib or clear main");

        HorizontalLayout bar = new HorizontalLayout(saveButton, changedCheckbox);
        bar.setVerticalComponentAlignment(Alignment.CENTER, saveButton, changedCheckbox);

        add(nameField, libSelectComboBox, removeLibButton, installButton, runButton, downloadFrom, downloadTo,
                downloadButton, resetButton, convertToVzButton, bar);

        sourceEditor = new SourceEditor();
        addAndExpand(sourceEditor);

        setVerticalComponentAlignment(Alignment.END, nameField, installButton, runButton, downloadFrom, downloadTo,
                downloadButton, resetButton, libSelectComboBox, removeLibButton);

        confirmedUpload = new ConfirmedUpload().withMessage("Overwrite recent changes?").withConfirmCaption("Overwrite")
                .withDeclineCaption("Cancel").withButtonCaption("Load").withDropCaption("Drop file")
                .withIcon(VaadinIcon.FILE);


        addUpload(UPLOAD_ID, bar, 0, confirmedUpload);

        new Button().addClickShortcut(Key.ENTER);
    }

    public void setDownloadSupplier(Supplier<String> filenameSupplier, Supplier<InputStream> inputStreamSupplier, Runnable onDownload) {
        this.filenameSupplier = filenameSupplier;
        this.inputStreamSupplier = inputStreamSupplier;
        this.onDownload = onDownload;
    }

    public void setConvertSupplier(Supplier<String> filenameSupplier, Supplier<InputStream> inputStreamSupplier, Runnable onDownload) {
        this.convertFilenameSupplier = filenameSupplier;
        this.convertInputStreamSupplier = inputStreamSupplier;
        this.onDownloadVz = onDownload;
    }
}
