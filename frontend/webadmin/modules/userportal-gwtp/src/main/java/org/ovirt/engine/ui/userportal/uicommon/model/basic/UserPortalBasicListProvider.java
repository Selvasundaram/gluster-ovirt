package org.ovirt.engine.ui.userportal.uicommon.model.basic;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.inject.Inject;

public class UserPortalBasicListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalBasicListModel> {

    @Inject
    public UserPortalBasicListProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    protected UserPortalBasicListModel createModel() {
        return new UserPortalBasicListModel();
    }

}

