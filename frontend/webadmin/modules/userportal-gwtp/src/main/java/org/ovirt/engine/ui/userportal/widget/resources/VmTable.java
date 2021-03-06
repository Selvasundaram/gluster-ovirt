package org.ovirt.engine.ui.userportal.widget.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.ActionCellTable;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.uicommon.model.resources.ResourcesModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class VmTable extends Composite implements HasEditorDriver<ResourcesModel> {

    private static final VmRowHeaderlessTableResources vmRowResources =
            GWT.create(VmRowHeaderlessTableResources.class);

    private static final DiskRowHeaderlessTableResources diskRowResources =
            GWT.create(DiskRowHeaderlessTableResources.class);

    private HandlerRegistration openHandler = null;

    private HandlerRegistration closeHandler = null;

    private final ResourcesModelProvider modelProvider;

    @UiField(provided = true)
    ActionCellTable<VM> tableHeader;

    @UiField
    Tree vmTree;

    private final ApplicationResources resources;

    private VmSingleSelectionModel vmSelectionModel = new VmSingleSelectionModel();

    interface WidgetUiBinder extends UiBinder<Widget, VmTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public VmTable(ResourcesModelProvider modelProvider,
            SubTableResources headerResources,
            ApplicationResources resources) {
        this.modelProvider = modelProvider;
        this.resources = resources;
        tableHeader = new ActionCellTable<VM>(modelProvider, headerResources);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        initTable();
    }

    private void initTable() {
        tableHeader.addColumn(new EmptyColumn(), "Virtual Machine");
        tableHeader.addColumn(new EmptyColumn(), "Disks");
        tableHeader.addColumn(new EmptyColumn(), "Virtual Size");
        tableHeader.addColumn(new EmptyColumn(), "Actual Size");
        tableHeader.addColumn(new EmptyColumn(), "Snapshots");

        setHeaderColumnWidths(Arrays.asList("40%", "10%", "10%", "10%", "30%"));

        tableHeader.setRowData(new ArrayList<VM>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(ResourcesModel model) {
        if (openHandler != null) {
            openHandler.removeHandler();
        }

        if (closeHandler != null) {
            closeHandler.removeHandler();
        }

        vmTree.clear();

        for (VM vm : (Iterable<VM>) model.getItems()) {
            VmTreeItem vmItem = createVmItem(vm);
            for (DiskImage disk : vm.getDiskList()) {
                TreeItem diskItem = createDiskItem(disk);
                vmItem.addItem(diskItem);
            }
            vmTree.addItem(vmItem);
        }

        updateSelection(model);
        listenOnSelectionChange();
    }

    @Override
    public ResourcesModel flush() {
        return modelProvider.getModel();
    }

    private void setHeaderColumnWidths(List<String> widths) {
        for (int i = 0; i < tableHeader.getColumnCount(); i++) {
            tableHeader.setColumnWidth(tableHeader.getColumn(i), widths.get(i));
        }
    }

    protected void listenOnSelectionChange() {
        openHandler = vmTree.addOpenHandler(new OpenHandler<TreeItem>() {
            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                setSelectionToModel();
            }

        });

        closeHandler = vmTree.addCloseHandler(new CloseHandler<TreeItem>() {

            @Override
            public void onClose(CloseEvent<TreeItem> event) {
                setSelectionToModel();
            }
        });
    }

    private void setSelectionToModel() {
        List<VM> selectedVMs = new ArrayList<VM>();
        for (int i = 0; i < vmTree.getItemCount(); i++) {
            if (vmTree.getItem(i) instanceof VmTreeItem) {
                if (vmTree.getItem(i).getState()) {
                    selectedVMs.add(((VmTreeItem) vmTree.getItem(i)).getVm());
                }
            }
        }

        modelProvider.setSelectedItems(selectedVMs);
    }

    @SuppressWarnings("unchecked")
    protected void updateSelection(final ResourcesModel model) {
        if (model.getSelectedItems() == null || model.getSelectedItems().size() == 0) {
            return;
        }
        for (int i = 0; i < vmTree.getItemCount(); i++) {
            if (vmTree.getItem(i) instanceof VmTreeItem) {
                ((VmTreeItem) vmTree.getItem(i)).setState(model.getSelectedItems());
            }
        }
    }

    private TreeItem createDiskItem(DiskImage disk) {
        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        diskRowResources,
                        true);

        ImageResourceColumn<EntityModel> diskImageColumn = new ImageResourceColumn<EntityModel>() {

            @Override
            public ImageResource getValue(EntityModel object) {
                return resources.vmDiskIcon();
            }

        };

        TextColumnWithTooltip<EntityModel> driveMappingColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return "Disk" + asDisk(entity).getinternal_drive_mapping();
            }
        };

        TextColumnWithTooltip<EntityModel> virtualSizeColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return asDisk(entity).getSizeInGigabytes() + "GB";
            }
        };

        TextColumnWithTooltip<EntityModel> actualSizeColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return ((Double) asDisk(entity).getActualDiskWithSnapshotsSize()).intValue() + "GB";
            }
        };

        TextColumnWithTooltip<EntityModel> snapshotsColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return asDisk(entity).getSnapshots().size() + "";
            }
        };

        table.addColumn(diskImageColumn, "diskImageColumn", "5%");
        table.addColumn(driveMappingColumn, "driveMappingColumn", "43%");
        table.addColumn(virtualSizeColumn, "virtualSizeColumn", "10%");
        table.addColumn(actualSizeColumn, "actualSizeColumn", "10%");
        table.addColumn(snapshotsColumn, "snapshotsColumn", "32%");
        EntityModel entityModel = new EntityModel();
        entityModel.setEntity(disk);

        table.setRowData(Arrays.asList(entityModel));
        return new TreeItem(table);
    }

    private VmTreeItem createVmItem(VM vm) {
        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        vmRowResources,
                        true);

        ImageResourceColumn<EntityModel> vmImageColumn = new ImageResourceColumn<EntityModel>() {

            @Override
            public ImageResource getValue(EntityModel object) {
                return resources.vmIconWithVmTextInside();
            }

        };

        TextColumnWithTooltip<EntityModel> nameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return asVm(entity).getvm_name();
            }
        };

        TextColumnWithTooltip<EntityModel> diskSizeColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return asVm(entity).getDiskList().size() + "";
            }
        };

        TextColumnWithTooltip<EntityModel> virtualSizeColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return ((Double) asVm(entity).getDiskSize()).intValue() + "GB";
            }

        };

        TextColumnWithTooltip<EntityModel> actualSizeColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return ((Double) asVm(entity).getActualDiskWithSnapshotsSize()).intValue() + "GB";
            }
        };

        TextColumnWithTooltip<EntityModel> snapshotsColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                return asVm(entity).getDiskList().size() > 0 ? asVm(entity).getDiskList().get(0).getSnapshots().size()
                        + "" : "0";
            }
        };
        table.addColumn(vmImageColumn, "vmImageColumn", "5%");
        table.addColumn(nameColumn, "nameColumn", "34%");
        table.addColumn(diskSizeColumn, "diskSizeColumn", "10%");
        table.addColumn(virtualSizeColumn, "virtualSizeColumn", "10%");
        table.addColumn(actualSizeColumn, "actualSizeColumn", "10%");
        table.addColumn(snapshotsColumn, "snapshotsColumn", "31%");
        table.setSelectionModel(vmSelectionModel);

        EntityModel entityModel = new EntityModel();
        entityModel.setEntity(vm);

        table.setRowData(Arrays.asList(entityModel));
        return new VmTreeItem(table, vm);
    }

    /**
     * This class guards, that only one row can be selected at a given time and the selection survives the refresh. The
     * single instance of this class should be used for more instances of EntityModelCellTable
     */
    class VmSingleSelectionModel extends SingleSelectionModel<EntityModel> {

        private VM selectedVM = null;

        @Override
        public void setSelected(EntityModel object, boolean selected) {
            if (object.getEntity() instanceof VM) {
                if (selected) {
                    selectedVM = (VM) object.getEntity();
                    super.setSelected(object, true);
                } else {
                    selectedVM = null;
                    super.setSelected(object, false);
                }
            } else {
                super.setSelected(object, selected);
            }

        }

        @Override
        public boolean isSelected(EntityModel object) {
            if (selectedVM == null || !(object.getEntity() instanceof VM)) {
                return super.isSelected(object);
            }

            VM vm = (VM) object.getEntity();
            if (vm.getId().equals(selectedVM.getId())) {
                return true;
            } else {
                return false;
            }
        }
    }

    private VM asVm(EntityModel entity) {
        return (VM) entity.getEntity();
    }

    private DiskImage asDisk(EntityModel entity) {
        return (DiskImage) entity.getEntity();
    }

    public interface VmRowHeaderlessTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/userportal/css/VmListHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    public interface DiskRowHeaderlessTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/userportal/css/DiskListHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    /**
     * An empty column - only for the header
     */
    private class EmptyColumn extends TextColumn<VM> {

        @Override
        public String getValue(VM object) {
            return null;
        }
    }

}
