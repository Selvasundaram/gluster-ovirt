package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskModelByNameComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

@SuppressWarnings("unused")
public class TemplateDiskListModel extends SearchableListModel
{
    private UICommand privateCopyCommand;

    public UICommand getCopyCommand()
    {
        return privateCopyCommand;
    }

    private void setCopyCommand(UICommand value)
    {
        privateCopyCommand = value;
    }

    private VmTemplate getEntityStronglyTyped()
    {
        return (VmTemplate) ((super.getEntity() instanceof VmTemplate) ? super.getEntity() : null);
    }

    public TemplateDiskListModel()
    {
        setTitle("Virtual Disks");

        setCopyCommand(new UICommand("Copy", this));

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }

        UpdateActionAvailability();
    }

    @Override
    public void Search()
    {
        if (getEntityStronglyTyped() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch(VdcQueryType.GetVmTemplatesDisks,
                new GetVmTemplatesDisksParameters(getEntityStronglyTyped().getId()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetVmTemplatesDisks,
                new GetVmTemplatesDisksParameters(getEntityStronglyTyped().getId())));
        setItems(getAsyncResult().getData());
    }

    ArrayList<DiskModel> diskModels;
    Iterable value;

    @Override
    public void setItems(Iterable value)
    {
        if (diskModels != null)
        {
            ItemsChanging(value, items);
            Linq.Sort(diskModels, new DiskModelByNameComparer());
            items = diskModels;
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            OnPropertyChanged(new PropertyChangedEventArgs("Items"));

            diskModels = null;
        }
        else
        {
            this.value = value;

            VmTemplate template = (VmTemplate) getEntity();
            AsyncDataProvider.GetStorageDomainListByTemplate(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            TemplateDiskListModel templateDiskListModel = (TemplateDiskListModel) target;
                            ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                            ArrayList<DiskImage> disks = Linq.<DiskImage> Cast(templateDiskListModel.value);
                            ArrayList<DiskModel> diskModels = new ArrayList<DiskModel>();

                            for (DiskImage diskImage : disks) {
                                DiskModel diskModel = new DiskModel();
                                diskModel.setDiskImage(diskImage);
                                diskModel.setName(diskImage.getinternal_drive_mapping());
                                diskModel.getStorageDomain().setItems(
                                        Linq.getStorageDomainsByIds(diskImage.getstorage_ids(), storageDomains));
                                diskModels.add(diskModel);
                            }

                            templateDiskListModel.diskModels = diskModels;
                            setItems(templateDiskListModel.value);
                        }
                    }), template.getId());
        }

        UpdateActionAvailability();
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        getCopyCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && isCopyCommandAvailable());
    }

    private boolean isCopyCommandAvailable() {
        ArrayList<DiskImage> disks =
                getSelectedItems() != null ? Linq.<DiskImage> Cast(getSelectedItems()) : new ArrayList<DiskImage>();

        for (DiskImage disk : disks)
        {
            if (disk.getimageStatus() != ImageStatus.OK)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getCopyCommand())
        {
            Copy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }

    private void Copy()
    {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        VmTemplate template = (VmTemplate) getEntity();

        CopyDiskModel model = new CopyDiskModel(template);
        model.setIsSingleDiskCopy(disks.size() == 1);
        setWindow(model);
        model.setTitle("Copy Disk(s)");
        model.setHashName("copy_disk");
        model.setIsVolumeFormatAvailable(false);
        model.setIsSourceStorageDomainAvailable(true);
        model.setIsSourceStorageDomainChangable(true);
        model.setEntity(this);
        model.init(disks);
        model.StartProgress(null);
    }

    private void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected String getListName() {
        return "TemplateDiskListModel";
    }
}
