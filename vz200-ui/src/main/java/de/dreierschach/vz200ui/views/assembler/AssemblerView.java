package de.dreierschach.vz200ui.views.assembler;

import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.util.ConfirmedUpload;
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

    AceEditor sourceEditor;
    Button installButton;
    Button runButton;
    TextField downloadFrom;
    TextField downloadTo;
    Button downloadButton;
    Button resetButton;
    LazyDownloadButton saveButton;
    Checkbox changedCheckbox;
    TextField nameField;
    Supplier<String> filenameSupplier = () -> "source.asm";
    Supplier<InputStream> inputStreamSupplier = () -> null;
    Runnable onDownload = () -> {
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
        changedCheckbox = new Checkbox("Changed", false);
        changedCheckbox.setReadOnly(true);

        libSelectComboBox = ComponentFactory.withTooltip(new ComboBox<>(), "select source to edit");
        libSelectComboBox.setAllowCustomValue(true);

        removeLibButton = ComponentFactory
                .withTooltip(new Button(VaadinIcon.CLOSE_SMALL.create()), "remove lib or clear main");

        sourceEditor = ComponentFactory.aceEditor("enter your assembler code here", AceTheme.ambiance, 16, 25, 44);

        HorizontalLayout bar = new HorizontalLayout(saveButton, changedCheckbox);
        bar.setAlignItems(Alignment.BASELINE);

        add(nameField, libSelectComboBox, removeLibButton, installButton, runButton, downloadFrom, downloadTo,
            downloadButton, resetButton, bar);
        addAndExpand(sourceEditor);

        setVerticalComponentAlignment(Alignment.END, nameField, installButton, runButton, downloadFrom, downloadTo,
                                      downloadButton, resetButton, libSelectComboBox, removeLibButton, saveButton,
                                      changedCheckbox);

        confirmedUpload = new ConfirmedUpload().withMessage("Overwrite recent changes?").withConfirmCaption("Overwrite")
                                               .withDeclineCaption("Cancel").withButtonCaption("Load")
                                               .withDropCaption("Drop file").withIcon(VaadinIcon.FILE);


        addUpload(UPLOAD_ID, bar, 0, confirmedUpload);

        new Button().addClickShortcut(Key.ENTER);
    }

    public void setDownloadSupplier(Supplier<String> filenameSupplier, Supplier<InputStream> inputStreamSupplier, Runnable onDownload) {
        this.filenameSupplier = filenameSupplier;
        this.inputStreamSupplier = inputStreamSupplier;
        this.onDownload = onDownload;
    }
}
