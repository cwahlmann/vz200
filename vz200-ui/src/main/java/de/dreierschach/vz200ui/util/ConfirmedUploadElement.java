package de.dreierschach.vz200ui.util;

import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.StartedEvent;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;

public class ConfirmedUploadElement extends Upload {
    enum ConfirmationState {none, inProgress, confirmed, declined}

    enum UploadState {none, started, succeeded}

    private ConfirmationState confirmationState;
    private UploadState uploadState;

    private ConfirmedUpload parameter;

    public ConfirmedUploadElement(ConfirmedUpload parameter) {
        super();
        this.parameter = parameter;
        this.confirmationState = ConfirmationState.none;
        this.uploadState = UploadState.none;
        addStartedListener(this::onStarted);
        addSucceededListener(this::onSucceeded);
    }

    public ConfirmedUploadElement(Receiver receiver, ConfirmedUpload parameter) {
        this(parameter);
        setReceiver(receiver);
    }

    private void onStarted(StartedEvent event) {
        if (parameter.getNeedsConfirmation().get()) {
            confirmationState = ConfirmationState.inProgress;
            ComponentFactory
                    .confirm(parameter.getMessage(), parameter.getConfirmCaption(), parameter.getDeclineCaption(),
                             () -> {
                                 if (uploadState != UploadState.succeeded) {
                                     confirmationState = ConfirmationState.confirmed;
                                     return;
                                 }
                                 confirm();
                             }, () -> {
                                if (uploadState != UploadState.succeeded) {
                                    confirmationState = ConfirmationState.declined;
                                    return;
                                }
                                decline();
                            });
        }
    }

    private void onSucceeded(SucceededEvent event) {
        if (!parameter.getNeedsConfirmation().get()) {
            parameter.getOnConfirmed().run();
            return;
        }
        switch (confirmationState) {
            case inProgress:
                uploadState = UploadState.succeeded;
                break;
            case confirmed:
                confirm();
                break;
            case declined:
                decline();
        }
    }

    private void decline() {
        confirmationState = ConfirmationState.none;
        uploadState = UploadState.none;
        parameter.getOnDeclined().run();
    }

    private void confirm() {
        confirmationState = ConfirmationState.none;
        uploadState = UploadState.none;
        parameter.getOnConfirmed().run();
    }
}
