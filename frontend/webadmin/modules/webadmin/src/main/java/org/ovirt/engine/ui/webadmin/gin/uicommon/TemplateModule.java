package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateNewPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class TemplateModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VmTemplate, TemplateListModel> getTemplateListProvider(ClientGinjector ginjector,
            final Provider<TemplateNewPresenterWidget> popupProvider,
            final Provider<VmExportPopupPresenterWidget> exportPopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> copyPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<VmTemplate, TemplateListModel>(ginjector, TemplateListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                TemplateListModel model = getModel();

                if (lastExecutedCommand == model.getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getExportCommand()) {
                    return exportPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getCopyCommand()) {
                    return copyPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    };

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<TemplateListModel, TemplateGeneralModel> getTemplateGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<TemplateListModel, TemplateGeneralModel>(ginjector,
                TemplateListModel.class,
                TemplateGeneralModel.class);
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, TemplateListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, TemplateListModel, PermissionListModel>(ginjector,
                TemplateListModel.class,
                PermissionListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                PermissionListModel model = getModel();

                if (lastExecutedCommand == model.getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> getTemplateDiskListProvider(ClientGinjector ginjector,
            final Provider<DisksAllocationPopupPresenterWidget> copyPopupProvider) {
        return new SearchableDetailTabModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel>(ginjector,
                TemplateListModel.class,
                TemplateDiskListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                TemplateDiskListModel model = getModel();

                if (lastExecutedCommand == model.getCopyCommand()) {
                    return copyPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> getTemplateInterfaceListProvider(ClientGinjector ginjector,
            final Provider<TemplateInterfacePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel>(ginjector,
                TemplateListModel.class,
                TemplateInterfaceListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                TemplateInterfaceListModel model = getModel();

                if ((lastExecutedCommand == model.getNewCommand()) || (lastExecutedCommand == model.getEditCommand())) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<storage_domains, TemplateListModel, TemplateStorageListModel> getTemplateStorageListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<storage_domains, TemplateListModel, TemplateStorageListModel>(ginjector,
                TemplateListModel.class,
                TemplateStorageListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, TemplateListModel, TemplateVmListModel> getTemplateVmListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VM, TemplateListModel, TemplateVmListModel>(ginjector,
                TemplateListModel.class,
                TemplateVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> getTemplateEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, TemplateListModel, TemplateEventListModel>(ginjector,
                TemplateListModel.class,
                TemplateEventListModel.class);
    }

    @Override
    protected void configure() {
    }

}
