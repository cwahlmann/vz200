package de.dreierschach.vz200ui.views.basic;

import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.util.ConfirmedUpload;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;
import org.vaadin.stefan.LazyDownloadButton;

import java.io.InputStream;
import java.util.function.Supplier;


@Route(value = "basic", layout = MainView.class)
@PageTitle("Basic")
@CssImport("./styles/views/basic/basic-view.css")
@VaadinSessionScope
public class BasicView extends View<BasicPresenter> {

    static final String UPLOAD_ID = "UPLOAD_ID";

    AceEditor sourceEditor;
    ComboBox<AceTheme> aceThemeComboBox;

    Button installButton;
    Button runButton;
    Button downloadButton;
    Button resetButton;
    LazyDownloadButton saveButton;
    Checkbox changedCheckbox;
    TextField nameField;
    Supplier<String> filenameSupplier = () -> "source.bas";
    Supplier<InputStream> inputStreamSupplier = () -> null;
    Runnable onDownload = () -> {
    };
    ConfirmedUpload confirmedUpload;

    @Override
    protected String getViewId() {
        return "basic-view";
    }

    protected void createContent() {
        //        setAlignItems(Alignment.BASELINE);

        sourceEditor = new AceEditor();
        sourceEditor.setTheme(AceTheme.ambiance);
        sourceEditor.setMode(AceMode.python);
        sourceEditor.setFontSize(15);
        sourceEditor.setHeight("100%");
        sourceEditor.setWidth("100%");
        sourceEditor.setReadOnly(false);
        sourceEditor.setShowInvisibles(false);
        sourceEditor.setShowGutter(false);
        sourceEditor.setShowPrintMargin(false);
        sourceEditor.setDisplayIndentGuides(false);
        sourceEditor.setUseWorker(false);
        sourceEditor.setSofttabs(true);
        sourceEditor.setTabSize(4);
        sourceEditor.setWrap(true);
        sourceEditor.setMinlines(16);
        sourceEditor.setMaxlines(25);
        sourceEditor.setPlaceholder("enter your basic program here");
        sourceEditor.setAutoComplete(true);
        sourceEditor.setHighlightActiveLine(false);
        sourceEditor.setHighlightSelectedWord(false);
        sourceEditor.setWidth("100%");
        sourceEditor.setHeight("100%");
        sourceEditor.setMaxWidth("44em");
        sourceEditor.setInitialFocus(true);

        aceThemeComboBox = new ComboBox<>();
        aceThemeComboBox.setItems(AceTheme.values());
        aceThemeComboBox.setValue(AceTheme.ambiance);

        nameField = new TextField("", "", "Name");

        installButton = new Button("Install", VaadinIcon.LAPTOP.create());
        runButton = new Button("Run", VaadinIcon.LAPTOP.create());
        downloadButton = new Button("Download", VaadinIcon.LAPTOP.create());
        resetButton = new Button("Reset", VaadinIcon.LAPTOP.create());
        saveButton = new LazyDownloadButton("Save", VaadinIcon.DOWNLOAD.create(), () -> filenameSupplier.get(),
                                            () -> inputStreamSupplier.get());
        saveButton.addDownloadStartsListener(event -> this.onDownload.run());
        changedCheckbox = new Checkbox("Changed", false);
        changedCheckbox.setReadOnly(true);

        add(aceThemeComboBox, nameField, installButton, runButton, downloadButton, resetButton, saveButton,
            changedCheckbox);
        addAndExpand(sourceEditor);

        setVerticalComponentAlignment(Alignment.END, aceThemeComboBox, nameField, installButton, runButton,
                                      downloadButton, resetButton, saveButton, changedCheckbox, sourceEditor);

        confirmedUpload = new ConfirmedUpload().withMessage("Overwrite recent changes?").withConfirmCaption("Overwrite")
                                               .withDeclineCaption("Cancel").withButtonCaption("Load")
                                               .withDropCaption("Drop file").withIcon(VaadinIcon.FILE);
        addUpload(UPLOAD_ID, this, 8, confirmedUpload);

        aceThemeComboBox.addValueChangeListener(event -> sourceEditor.setTheme(event.getValue()));
    }

    public void setDownloadSupplier(Supplier<String> filenameSupplier, Supplier<InputStream> inputStreamSupplier, Runnable onDownload) {
        this.filenameSupplier = filenameSupplier;
        this.inputStreamSupplier = inputStreamSupplier;
        this.onDownload = onDownload;
    }
}
