package org.ovirt.engine.core.utils.ovf;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Encoding;
import org.ovirt.engine.core.compat.Formatting;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlTextWriter;

public abstract class OvfWriter implements IOvfBuilder {
    protected String _fileName;
    protected int _instanceId;
    protected List<DiskImage> _images;
    protected XmlTextWriter _writer;
    protected XmlDocument _document;
    protected VM _vm;
    protected VmBase vmBase;

    public OvfWriter(RefObject<XmlDocument> document, VmBase vmBase, List<DiskImage> images) {
        _fileName = Path.GetTempFileName();
        document.argvalue = new XmlDocument();
        _document = document.argvalue;
        _images = images;
        _writer = new XmlTextWriter(_fileName, Encoding.UTF8);
        this.vmBase = vmBase;
        WriteHeader();
    }

    private void WriteHeader() {
        _instanceId = 0;
        _writer.Formatting = Formatting.Indented;
        _writer.Indentation = 4;
        _writer.WriteStartDocument(false);
        _writer.WriteStartElement("ovf", "Envelope", OVF_URI);
        _writer.WriteAttributeString("xmlns", "ovf", null, OVF_URI);
        _writer.WriteAttributeString("xmlns", "rasd", null, RASD_URI);
        _writer.WriteAttributeString("xmlns", "vssd", null, VSSD_URI);
        _writer.WriteAttributeString("xmlns", "xsi", null, XSI_URI);
        // Setting the OVF version according to ENGINE (in 2.2 , version was set to "0.9")
        _writer.WriteAttributeString("ovf", "version", null, Config.<String> GetValue(ConfigValues.VdcVersion));
    }

    private void CloseElements() {
        _writer.WriteEndElement();
    }

    protected long BytesToGigabyte(long bytes) {
        return bytes / 1024 / 1024 / 1024;
    }

    @Override
    public void BuildReference() {
        _writer.WriteStartElement("References");
        for (DiskImage image : _images) {
            _writer.WriteStartElement("File");
            _writer.WriteAttributeString("ovf", "href", null, OvfParser.CreateImageFile(image));
            _writer.WriteAttributeString("ovf", "id", null, image.getId().toString());
            _writer.WriteAttributeString("ovf", "size", null, (new Long(image.getsize())).toString());
            _writer.WriteAttributeString("ovf", "description", null, StringUtils.defaultString(image.getdescription()));
            _writer.WriteEndElement();

        }
        for (VmNetworkInterface iface : vmBase.getInterfaces()) {
            _writer.WriteStartElement("Nic");
            _writer.WriteAttributeString("ovf", "id", null, iface.getId().toString());
            _writer.WriteEndElement();
        }
        _writer.WriteEndElement();
    }

    @Override
    public void BuildNetwork() {
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:NetworkSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("List of networks");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Network");
        _writer.WriteAttributeString("ovf", "name", null, "Network 1");
        _writer.WriteEndElement();
        _writer.WriteEndElement();
    }

    @Override
    public void BuildDisk() {
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:DiskSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("List of Virtual Disks");
        _writer.WriteEndElement();
        for (DiskImage image : _images) {
            _writer.WriteStartElement("Disk");
            _writer.WriteAttributeString("ovf", "diskId", null, image.getId().toString());
            _writer.WriteAttributeString("ovf", "size", null, (new Long(BytesToGigabyte(image.getsize()))).toString());
            _writer.WriteAttributeString("ovf", "actual_size", null,
                    (new Long(BytesToGigabyte(image.getactual_size()))).toString());
            _writer.WriteAttributeString("ovf", "vm_snapshot_id", null, (image.getvm_snapshot_id() != null) ? image
                    .getvm_snapshot_id().getValue().toString() : "");

            if (image.getParentId().equals(Guid.Empty)) {
                _writer.WriteAttributeString("ovf", "parentRef", null, "");
            } else {
                // LINQ 29456
                // List<DiskImage> res = _images.SkipWhile(img => image.ParentId
                // == img.image_guid).ToList();
                int i = 0;
                while (_images.get(i).getId().equals(image.getParentId()))
                    i++;
                List<DiskImage> res = _images.subList(i, _images.size() - 1);
                // LINQ 29456
                if (res.size() > 0) {
                    _writer.WriteAttributeString("ovf", "parentRef", null, OvfParser.CreateImageFile(res.get(0)));
                } else {
                    _writer.WriteAttributeString("ovf", "parentRef", null, "");
                }
            }

            _writer.WriteAttributeString("ovf", "fileRef", null, OvfParser.CreateImageFile(image));

            String format = "";
            switch (image.getvolume_format()) {
            case RAW:
                format = "http://www.vmware.com/specifications/vmdk.html#sparse";
                break;

            case COW:
                format = "http://www.gnome.org/~markmc/qcow-image-format.html";
                break;

            case Unassigned:
                break;
            }
            _writer.WriteAttributeString("ovf", "format", null, format);
            _writer.WriteAttributeString("ovf", "volume-format", null, image.getvolume_format().toString());
            _writer.WriteAttributeString("ovf", "volume-type", null, image.getvolume_type().toString());
            _writer.WriteAttributeString("ovf", "disk-interface", null, image.getdisk_interface().toString());
            _writer.WriteAttributeString("ovf", "disk-type", null, image.getdisk_type().toString());
            _writer.WriteAttributeString("ovf", "boot", null, (new Boolean(image.getboot())).toString());
            _writer.WriteAttributeString("ovf", "wipe-after-delete", null,
                    (new Boolean(image.getwipe_after_delete())).toString());
            _writer.WriteEndElement();
        }
        _writer.WriteEndElement();
    }

    @Override
    public void BuildVirtualSystem() {
        // General Vm
        _writer.WriteStartElement("Content");
        _writer.WriteAttributeString("ovf", "id", null, "out");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:VirtualSystem_Type");

        // General Data
        WriteGeneralData();

        // Application List
        WriteAppList();

        // Content Items
        WriteContentItems();

        _writer.WriteEndElement(); // End Content tag
    }

    protected abstract void WriteGeneralData();

    protected abstract void WriteAppList();

    protected abstract void WriteContentItems();

    @Override
    protected void finalize() throws Throwable {
        Dispose();
        super.finalize();
    }

    protected void writeManagedDeviceInfo(VmBase vmBase, XmlTextWriter writer, Guid deviceId) {
        VmDevice vmDevice = vmBase.getManagedVmDeviceMap().get(deviceId);
        if (deviceId != null && vmDevice != null && vmDevice.getAddress() != null) {
            writeVmDeviceInfo(vmDevice);
        }
    }

    protected void writeUnmanagedDevices(VmBase vmBase, XmlTextWriter write) {
        List<VmDevice> devices = vmBase.getUnmanagedDeviceList();
        for (VmDevice vmDevice : devices) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement("rasd:ResourceType");
            _writer.WriteRaw(OvfHardware.OTHER);
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:InstanceId");
            _writer.WriteRaw((String.valueOf(vmDevice.getId().getDeviceId())));
            _writer.WriteEndElement();
            writeVmDeviceInfo(vmDevice);
            _writer.WriteEndElement(); // item
        }
    }

    protected void writeMonitors(VmBase vmBase) {
        Collection<VmDevice> devices = vmBase.getManagedVmDeviceMap().values();
        int numOfMonitors = vmBase.getnum_of_monitors();
        int i = 0;
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType().equals(VmDeviceType.VIDEO.getName())) {
                _writer.WriteStartElement("Item");
                _writer.WriteStartElement("rasd:Caption");
                _writer.WriteRaw("Graphical Controller");
                _writer.WriteEndElement();
                _writer.WriteStartElement("rasd:InstanceId");
                _writer.WriteRaw((String.valueOf(vmDevice.getId().getDeviceId())));
                _writer.WriteEndElement();
                _writer.WriteStartElement("rasd:ResourceType");
                _writer.WriteRaw(OvfHardware.Monitor);
                _writer.WriteEndElement();
                _writer.WriteStartElement("rasd:VirtualQuantity");
                // we should write number of monitors for each entry for backward compatibility
                _writer.WriteRaw(String.valueOf(numOfMonitors));
                _writer.WriteEndElement();
                writeVmDeviceInfo(vmDevice);
                _writer.WriteEndElement(); // item
                if (i++ == numOfMonitors) {
                    break;
                }
            }
        }
    }

    protected void writeCd(VmBase vmBase) {
        Collection<VmDevice> devices = vmBase.getManagedVmDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType().equals(VmDeviceType.CDROM.getName())) {
                _writer.WriteStartElement("Item");
                _writer.WriteStartElement("rasd:Caption");
                _writer.WriteRaw("CDROM");
                _writer.WriteEndElement();
                _writer.WriteStartElement("rasd:InstanceId");
                _writer.WriteRaw((String.valueOf(vmDevice.getId().getDeviceId())));
                _writer.WriteEndElement();
                _writer.WriteStartElement("rasd:ResourceType");
                _writer.WriteRaw(OvfHardware.CD);
                _writer.WriteEndElement();
                writeVmDeviceInfo(vmDevice);
                _writer.WriteEndElement(); // item
                break; // only one CD is currently supported
            }
        }
    }
    public void Dispose() {
        if (_writer != null) {
            CloseElements();
            _writer.close();
            _document.Load(_fileName);
        }
        deleteTmpFile();
    }

    public void deleteTmpFile() {
        try {
            File tmpFile = new File(_fileName);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        } catch (Exception e) {
        }
    }

    public void dispose() {
        this.Dispose();
    }

    private void writeVmDeviceInfo(VmDevice vmDevice) {
        _writer.WriteStartElement(OvfProperties.VMD_TYPE);
        _writer.WriteRaw(String.valueOf(vmDevice.getType()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_DEVICE);
        _writer.WriteRaw(String.valueOf(vmDevice.getDevice()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_ADDRESS);
        _writer.WriteRaw(vmDevice.getAddress());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_BOOT_ORDER);
        _writer.WriteRaw(String.valueOf(vmDevice.getBootOrder()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_IS_PLUGGED);
        _writer.WriteRaw(String.valueOf(vmDevice.getIsPlugged()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_IS_READONLY);
        _writer.WriteRaw(String.valueOf(vmDevice.getIsReadOnly()));
        _writer.WriteEndElement();
    }
}
