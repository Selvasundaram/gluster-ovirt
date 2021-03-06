package org.ovirt.engine.ui.uicommonweb.models;

@SuppressWarnings("unused")
public enum SystemTreeItemType
{
    System,
    DataCenter,
    Storages,
    Storage,
    Templates,
    Clusters,
    Cluster,
    VMs,
    Hosts,
    Host;

    public int getValue()
    {
        return this.ordinal();
    }

    public static SystemTreeItemType forValue(int value)
    {
        return values()[value];
    }
}
