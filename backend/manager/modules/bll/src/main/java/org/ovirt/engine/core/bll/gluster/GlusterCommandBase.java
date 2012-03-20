package org.ovirt.engine.core.bll.gluster;

<<<<<<< HEAD
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.VdsGroupCommandBase;
=======
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.common.PermissionSubject;
>>>>>>> engine : Create gluster volume command
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
<<<<<<< HEAD
=======
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
>>>>>>> engine : Create gluster volume command
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class GlusterCommandBase<T extends VdsGroupParametersBase> extends VdsGroupCommandBase<T> {
    public GlusterCommandBase(T params) {
        super(params);
        setVdsGroupId(params.getVdsGroupId());
    }

    @Override
    protected boolean canDoAction() {
        boolean canDoAction = super.canDoAction();
        if (canDoAction && getVdsGroup() == null) {
            canDoAction = false;
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
        }
        return canDoAction;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.bll.CommandBase#getPermissionCheckSubjects()
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        return permissionList;
    }

    public List<VDS> getOnlineServers() {
        List<VDS> hosts =
                DbFacade.getInstance()
                        .getVdsDAO()
                        .getAllForVdsGroupWithStatus( getVdsGroup().getId().getValue(), VDSStatus.Up);
        return hosts;
    }

    public VDS getOnlineServer() {
        List<VDS> hosts = getOnlineServers();
        if (hosts == null || hosts.size() == 0) {
            throw new VdcBLLException(VdcBllErrors.NO_ONLINE_SERVER_FOUND);
        }
        return hosts.get(0);
    }
}
