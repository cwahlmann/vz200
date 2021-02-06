package de.dreierschach.vz200ui.views.assembler;

import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.service.VzSource;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@SpringComponent
@VaadinSessionScope
public class AssemblerPresenter extends Presenter<AssemblerView> {
    public static final Logger log = LoggerFactory.getLogger(AssemblerPresenter.class);

    private final Vz200Service vz200Service;
    private final Config config;

    private boolean changed = false;
    private VzSource vzSource;
    private String currentLib;
    public static final String MAIN_FILE = "main";

    @Autowired
    public AssemblerPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;

        vzSource = new VzSource();
        vzSource.setType(VzSource.SourceType.asm);
        vzSource.setName("UNKNOWN");
        vzSource.setAutorun(false);
        vzSource.setSource("");
        currentLib = MAIN_FILE;
    }

    @Override
    protected void doBind() {
        Binder<VzSource> binder = new Binder<>();
        binder.setBean(vzSource);

//        binder.bind(view.sourceEditor, this::getSource, this::setSource);
        binder.bind(view.nameField, VzSource::getName, VzSource::setName);
        view.sourceEditor.addValueChangeListener(event -> {
            setChanged(true);
            this.setSource(vzSource, event.getValue());
        });
        view.sourceEditor.setTheme(AceTheme.valueOf(config.getOrDefault(Config.ACE_THEME, AceTheme.ambiance.name())));

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
        view.setDownloadSupplier(() -> String.format("%s.asm", view.nameField.getValue()),
                                 () -> new ByteArrayInputStream(
                                         view.sourceEditor.getValue().getBytes(StandardCharsets.UTF_8)),
                                 () -> this.setChanged(false));
        view.downloadFrom.addValueChangeListener(event -> {
            String value = event.getValue();
            if (!isHex(value)) {
                view.downloadFrom.setErrorMessage("please insert a hexadecimal value");
            } else {
                view.downloadFrom.setErrorMessage(null);
            }
        });

        view.downloadTo.addValueChangeListener(event -> {
            String value = event.getValue();
            if (!isHex(value)) {
                view.downloadTo.setErrorMessage("please insert a hexadecimal value");
            } else {
                view.downloadTo.setErrorMessage(null);
            }
        });

        view.libSelectComboBox.setItems(MAIN_FILE);
        view.libSelectComboBox.setValue(MAIN_FILE);

        view.libSelectComboBox.addCustomValueSetListener(event -> {
            String newName = event.getDetail();
            if (MAIN_FILE.equals(currentLib)) {
                vzSource.setSource(view.sourceEditor.getValue());
                VzSource.Lib lib = createNewLib(newName);
                vzSource.getLibs().add(lib);
                currentLib = newName;
                setLibSelectItems();
                view.libSelectComboBox.setValue(currentLib);
                view.sourceEditor.setValue(getSource(vzSource));
                ComponentFactory.info("New lib created.");
                return;
            }
            VzSource.Lib lib = getLib(currentLib);
            if (lib != null) {
                ComponentFactory.confirm("Rename lib or create a new one?", "Rename", "Create", () -> {
                    lib.setName(newName);
                    lib.setSource(view.sourceEditor.getValue());
                    setLibSelectItems();
                    view.libSelectComboBox.setValue(newName);
                    ComponentFactory.info("Lib name changed.");
                    currentLib = newName;
                }, () -> {
                    vzSource.setSource(view.sourceEditor.getValue());
                    VzSource.Lib newLib = createNewLib(newName);
                    vzSource.getLibs().add(newLib);
                    currentLib = newName;
                    setLibSelectItems();
                    view.libSelectComboBox.setValue(currentLib);
                    view.sourceEditor.setValue(getSource(vzSource));
                    ComponentFactory.info("New lib created.");
                });
            }
        });

        view.libSelectComboBox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                currentLib = view.libSelectComboBox.getValue();
                view.sourceEditor.setValue(getSource(vzSource));
            }
        });

        view.removeLibButton.addClickListener(event -> {
            ComponentFactory.confirm("Really remove " + currentLib + "?", "Remove", "Cancel", () -> {
                if (MAIN_FILE.equals(currentLib)) {
                    vzSource.setSource("");
                    ComponentFactory.info(MAIN_FILE + " cleared.");
                    view.sourceEditor.setValue(getSource(vzSource));
                } else {
                    VzSource.Lib lib = getLib(currentLib);
                    if (lib != null) {
                        vzSource.getLibs().remove(lib);
                        ComponentFactory.info("Lib " + currentLib + " removed.");
                        setLibSelectItems();
                        currentLib = MAIN_FILE;
                        view.libSelectComboBox.setValue(currentLib);
                        view.sourceEditor.setValue(getSource(vzSource));
                    }
                }
            }, () -> {
            });
        });
    }

    private String getSource(VzSource vzSource) {
        if (MAIN_FILE.equals(currentLib)) {
            return vzSource.getSource();
        }
        return vzSource.getLibs().stream().filter(l -> currentLib.equals(l.getName())).map(VzSource.Lib::getSource)
                       .findAny().orElse("");
    }

    private void setSource(VzSource vzSource, String source) {
        if (MAIN_FILE.equals(currentLib)) {
            vzSource.setSource(source);
        }
        vzSource.getLibs().stream().filter(l -> currentLib.equals(l.getName())).findAny()
                .ifPresent(l -> l.setSource(source));
    }

    private void setLibSelectItems() {
        Set<String> items = vzSource.getLibs().stream().map(VzSource.Lib::getName).collect(Collectors.toSet());
        items.add(MAIN_FILE);
        view.libSelectComboBox.setItems(items);
    }

    private VzSource.Lib getLib(String name) {
        return vzSource.getLibs().stream().filter(l -> name.equals(l.getName())).findAny().orElse(null);
    }

    private VzSource.Lib createNewLib(String name) {
        VzSource.Lib lib = new VzSource.Lib();
        lib.setName(name);
        lib.setSource("");
        return lib;
    }

    private boolean isHex(String value) {
        return StringUtils.isNotEmpty(value) && value.matches("[0-9a-zA-Z]+");
    }

    private void installToMemory(boolean run) {
        try {
            vzSource.setAutorun(run);
            vz200Service.saveAssemblerToMemory(vzSource);
            Notification.show("Install/Run successful.");
        } catch (Exception e) {
            ComponentFactory.warning("Install/Run failed: " + e.getMessage());
        }
    }

    private void downloadFromMemory() {
        try {
            VzSource vzSource = vz200Service
                    .loadAssemblerFromMemory(view.downloadFrom.getValue(), view.downloadTo.getValue());
            view.sourceEditor.setValue(vzSource.getSource());
            view.nameField.setValue(vzSource.getName());
            setChanged(false);
            ComponentFactory.info("Download successful.");
        } catch (Exception e) {
            ComponentFactory.warning("Download failed: " + e.getMessage());
        }
    }

    private void onUploadConfirmed() {
        try (InputStream in = view.getUploadStream(AssemblerView.UPLOAD_ID)) {
            view.sourceEditor.setValue("");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            view.sourceEditor.setValue(reader.lines().collect(Collectors.joining("\n")));
            view.nameField.setValue(cutFilenameEnding(view.getUploadFilename(AssemblerView.UPLOAD_ID)));
            setChanged(false);
            view.resetUploadButton(AssemblerView.UPLOAD_ID);
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
        view.resetUploadButton(AssemblerView.UPLOAD_ID);
    }

    private void setChanged(boolean changed) {
        this.changed = changed;
        view.changedCheckbox.setValue(changed);
    }
}
