package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.action.AddDiskToVmParameters;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

@CustomLogFields({ @CustomLogField("DiskName") })
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddDiskToVmCommand<T extends AddDiskToVmParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 4499428315430159917L;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddDiskToVmCommand(Guid commandId) {
        super(commandId);
    }

    public AddDiskToVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(parameters.getVmId());
        setQuotaId(parameters.getDiskInfo() != null ? parameters.getDiskInfo().getQuotaId() : null);
    }

    public String getDiskName() {
        return getParameters() == null
                || getParameters().getDiskInfo() == null
                || getParameters().getDiskInfo().getinternal_drive_mapping() == null
                ? "[N/A]"
                : String.format("Disk %1$s", getParameters().getDiskInfo().getinternal_drive_mapping());
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVm() == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else if (getVm().getstatus() != VMStatus.Down) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        } else if (hasRunningTasks()) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_TASKS_ARE_ALREADY_RUNNING);
        } else {
            // update disks from db
            VmHandler.updateDisksFromDb(getVm());
            // if user sent drive check that its not in use
            if (!StringHelper.isNullOrEmpty(getParameters().getDiskInfo().getinternal_drive_mapping())
                    && getVm().getDiskMap().containsKey(getParameters().getDiskInfo().getinternal_drive_mapping())) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LETTER_ALREADY_IN_USE);
            } else {
                getParameters().getDiskInfo().setinternal_drive_mapping(GetCorrectDriveForDisk());
                if (getParameters().getDiskInfo().getinternal_drive_mapping() == null) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LIMITATION_EXCEEDED);
                }
            }

            if (returnValue && getParameters().getDiskInfo().getdisk_type().equals(DiskType.System)) {
                for (DiskImageBase disk : getVm().getDiskMap().values()) {
                    if (disk.getdisk_type().equals(DiskType.System)) {
                        returnValue = false;
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SYSTEM_ALREADY_EXISTS);
                        getReturnValue().getCanDoActionMessages().add(
                                String.format("$DiskName %1$s", disk.getinternal_drive_mapping()));
                        break;
                    }
                }
            }

            if (returnValue && getParameters().getDiskInfo().getboot()) {
                for (DiskImageBase disk : getVm().getDiskMap().values()) {
                    if (disk.getboot()) {
                        returnValue = false;
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                        getReturnValue().getCanDoActionMessages().add(
                                String.format("$DiskName %1$s", disk.getinternal_drive_mapping()));
                        break;
                    }
                }
            }
            storage_domains storageDomain = getStorageDomainDao().get(
                    getStorageDomainId().getValue());
            if (returnValue) {
                if (storageDomain == null) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
                } else {
                    if (getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                            getStorageDomainId().getValue(), getVm().getstorage_pool_id())) == null) {
                        returnValue = false;
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
                    } else {

                        List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
                        List<DiskImageBase> allVmDisks = LinqUtils.foreach(getVm().getDiskMap().values(),
                                new Function<DiskImage, DiskImageBase>() {
                                    @Override
                                    public DiskImageBase eval(DiskImage diskImage) {
                                        return diskImage;
                                    }
                                });
                        allVmDisks.add(getParameters().getDiskInfo());
                        returnValue = ImagesHandler.CheckImagesConfiguration(
                                getStorageDomainId().getValue(),
                                Arrays.asList(getParameters().getDiskInfo()),
                                getReturnValue().getCanDoActionMessages())
                                        && CheckPCIAndIDELimit(getVm().getnum_of_monitors(),
                                                vmInterfaces,
                                                allVmDisks,
                                                getReturnValue().getCanDoActionMessages());
                    }
                }
            }

            List<DiskImage> emptyList = Collections.emptyList();
            returnValue = returnValue
                         && ImagesHandler.PerformImagesChecks(getVm(), getReturnValue().getCanDoActionMessages(), getVm()
                                 .getstorage_pool_id(), getStorageDomainId().getValue(), false, false, false, false, true,
                                 false, false, true, emptyList);

            if (returnValue && !hasFreeSpace(storageDomain)) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
            }
        }
        if (returnValue) {
            if (getRequestDiskSpace() > Config
                    .<Integer> GetValue(ConfigValues.MaxDiskSize)) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED);
                getReturnValue().getCanDoActionMessages().add(
                        String.format("$max_disk_size %1$s", Config.<Integer> GetValue(ConfigValues.MaxDiskSize)));
                returnValue = false;
            }
        }
        return returnValue;
    }

    protected boolean hasRunningTasks() {
        return getAsycTaskManager().EntityHasTasks(getVmId());
    }

    protected AsyncTaskManager getAsycTaskManager() {
        return AsyncTaskManager.getInstance();
    }

    private VolumeType getVolumeType() {
        return getParameters().getDiskInfo().getvolume_type();
    }

    private long getRequestDiskSpace() {
        return getParameters().getDiskInfo().getSizeInGigabytes();
    }

    private boolean hasFreeSpace(storage_domains storageDomain) {
        if (getVolumeType() == VolumeType.Preallocated) {
            return StorageDomainSpaceChecker.hasSpaceForRequest(storageDomain, getRequestDiskSpace());
        } else {
            return StorageDomainSpaceChecker.isBelowThresholds(storageDomain);
        }
    }

    /**
     * @return The VmNetworkInterfaceDAO
     */
    protected VmNetworkInterfaceDAO getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDAO();
    }

    /**
     * @return The StorageDomainStaticDAO
     */
    protected StorageDomainStaticDAO getStorageDomainStaticDao() {
        return DbFacade.getInstance().getStorageDomainStaticDAO();
    }

    /**
     * @return The StoragePoolIsoMapDAO
     */
    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
        return DbFacade.getInstance().getStoragePoolIsoMapDAO();
    }

    /**
     * @return The StorageDomainDAO
     */
    protected StorageDomainDAO getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDAO();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    /**
     * @return The ID of the storage domain where the VM's disks reside.
     */
    private Guid getDisksStorageDomainId() {
        return getVm().getDiskMap().values().iterator().next().getstorage_ids().get(0);
    }

    @Override
    public NGuid getStorageDomainId() {
        Guid storageDomainId = getParameters().getStorageDomainId();
        if (Guid.Empty.equals(storageDomainId) &&
                getVm() != null &&
                getVm().getDiskMap() != null &&
                !getVm().getDiskMap().isEmpty()) {
            return getDisksStorageDomainId();
        } else {
            return storageDomainId == null ? Guid.Empty : storageDomainId;
        }
    }

    @Override
    protected boolean validateQuota() {
        // Set default quota id if storage pool enforcement is disabled.
        getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getParameters().getDiskInfo().getQuotaId(),
                getStoragePool()));

        return (QuotaManager.validateStorageQuota(getStorageDomainId().getValue(),
                getParameters().getQuotaId(),
                getStoragePool().getQuotaEnforcementType(),
                new Double(getRequestDiskSpace()),
                getCommandId(),
                getReturnValue().getCanDoActionMessages()));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> listPermissionSubjects = super.getPermissionCheckSubjects();
        listPermissionSubjects =
                QuotaHelper.getInstance().addQuotaPermissionSubject(listPermissionSubjects,
                        getStoragePool(),
                        getQuotaId());
        return listPermissionSubjects;
    }

    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected void ExecuteVmCommand() {
        // NOTE: Assuming that we need to lock the vm before adding a disk!
        VmHandler.checkStatusAndLockVm(getVm().getId(), getCompensationContext());

        // create from blank template, create new vm snapshot id
        AddImageFromScratchParameters parameters = new AddImageFromScratchParameters(Guid.Empty, getVmId(),
                getParameters().getDiskInfo());
        parameters.setQuotaId(getParameters().getQuotaId());
        parameters.setStorageDomainId(getStorageDomainId().getValue());
        parameters.setVmSnapshotId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE));
        parameters.setParentCommand(VdcActionType.AddDiskToVm);
        parameters.setEntityId(getParameters().getEntityId());
        getParameters().getImagesParameters().add(parameters);
        parameters.setParentParemeters(getParameters());
        VdcReturnValueBase tmpRetValue =
                Backend.getInstance().runInternalAction(VdcActionType.AddImageFromScratch,
                        parameters,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        getReturnValue().getTaskIdList().addAll(tmpRetValue.getInternalTaskIdList());
        getReturnValue().setActionReturnValue(tmpRetValue.getActionReturnValue());
        getReturnValue().setFault(tmpRetValue.getFault());
        setSucceeded(tmpRetValue.getSucceeded());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_ADD_DISK_TO_VM : AuditLogType.USER_FAILED_ADD_DISK_TO_VM;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_FAILURE;
        }
    }

    private String GetCorrectDriveForDisk() {
        int driveNum = 1;
        List<Integer> vmDisks = LinqUtils.foreach(getVm().getDiskMap().keySet(), new Function<String, Integer>() {
            @Override
            public Integer eval(String s) {
                return new Integer(s);
            }
        });
        Collections.sort(vmDisks);

        for (int disk : vmDisks) {
            if ((disk - driveNum) == 0) {
                driveNum++;
            } else {
                break;
            }
        }
        return Integer.toString(driveNum);
    }

    @Override
    protected void removeQuotaCommandLeftOver() {
        QuotaManager.removeStorageDeltaQuotaCommand(getQuotaId(),
                getStorageDomainId().getValue(),
                getStoragePool().getQuotaEnforcementType(),
                getCommandId());
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.AddImageFromScratch;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
