package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplatePermissionsPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.template.TemplatePermissionListModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedTemplatePermissionsView extends AbstractSubTabTableWidgetView<VmTemplate, permissions, UserPortalTemplateListModel, PermissionListModel>
        implements SubTabExtendedTemplatePermissionsPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedTemplatePermissionsView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedTemplatePermissionsView(TemplatePermissionListModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new PermissionListModelTable<PermissionListModel>(modelProvider, eventBus, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
