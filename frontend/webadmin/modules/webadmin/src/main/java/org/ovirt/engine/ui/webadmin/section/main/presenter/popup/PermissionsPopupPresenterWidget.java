package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.popup.permissions.AbstractPermissionsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PermissionsPopupPresenterWidget extends AbstractPermissionsPopupPresenterWidget<PermissionsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPermissionsPopupPresenterWidget.ViewDef {
    }

    @Inject
    public PermissionsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
