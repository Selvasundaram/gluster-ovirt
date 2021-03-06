package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.storage.ConnectAllHostsToLunCommand.ConnectAllHostsToLunCommandReturnValue;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.vdscommands.ExtendStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

@SuppressWarnings("serial")
public class ExtendSANStorageDomainCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {
    public ExtendSANStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        SetStorageDomainStatus(StorageDomainStatus.Locked);
        for (LUNs lun : getParameters().getLunsList()) {
            ProceedLUNInDb(lun);
        }
        if (Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.ExtendStorageDomain,
                        new ExtendStorageDomainVDSCommandParameters(getStoragePoolId().getValue(), getStorageDomain()
                                .getId(), getParameters().getLunIds())).getSucceeded()) {
            SetStorageDomainStatus(StorageDomainStatus.Active);
            setSucceeded(true);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean canDoAction() {
        super.canDoAction();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXTEND);
        boolean returnValue = CheckStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active);
        if (returnValue
                && (getStorageDomain().getstorage_type() == StorageType.NFS || getStorageDomain().getstorage_type() == StorageType.UNKNOWN)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            returnValue = false;
        } else {
            final ConnectAllHostsToLunCommandReturnValue connectResult =
                    (ConnectAllHostsToLunCommandReturnValue) Backend.getInstance().runInternalAction(
                            VdcActionType.ConnectAllHostsToLun,
                            new ExtendSANStorageDomainParameters(getParameters().getStorageDomainId(), getParameters()
                                    .getLunIds()));
            if (!connectResult.getSucceeded()) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_EXTEND_CONNECTION_FAILED);
                if (connectResult.getFailedVds() != null) {
                    getReturnValue().getCanDoActionMessages().add(String.format("$hostName %1s",
                            connectResult.getFailedVds().getvds_name()));
                }
                if (connectResult.getFailedLun() != null) {
                    getReturnValue().getCanDoActionMessages().add(String.format("$lun %1s",
                            connectResult.getFailedLun().getLUN_id()));
                }
                returnValue = false;
            } else {
                // use luns list from connect command
                getParameters().setLunsList((ArrayList<LUNs>) connectResult.getActionReturnValue());
            }
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_EXTENDED_STORAGE_DOMAIN
                : AuditLogType.USER_EXTENDED_STORAGE_DOMAIN_FAILED;
    }
}
