package org.ovirt.engine.ui.uicommonweb.models.qouta;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

public class QuotaPermissionListModel extends PermissionListModel {

    public static final Guid CONSUME_QUOTA_ROLE_ID = new Guid("DEF0000a-0000-0000-0000-DEF00000000a");

    public QuotaPermissionListModel() {
        setTitle("Permissions");
    }

    @Override
    protected void SyncSearch() {
        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(getObjectType());
        tempVar.setDirectOnly(false);
        tempVar.setRefresh(getIsQueryFirstTime());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                ArrayList<permissions> list =
                        (java.util.ArrayList<permissions>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                ArrayList<permissions> newList = new ArrayList<permissions>();
                for (permissions permission : list) {
                    if (!permission.getrole_id().equals(CONSUME_QUOTA_ROLE_ID)) {
                        newList.add(permission);
                    }
                }
                searchableListModel.setItems(newList);
            }
        };

        tempVar.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetPermissionsForObject, tempVar, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    protected String getListName() {
        return "QuotaPermissionListModel";
    }

}
