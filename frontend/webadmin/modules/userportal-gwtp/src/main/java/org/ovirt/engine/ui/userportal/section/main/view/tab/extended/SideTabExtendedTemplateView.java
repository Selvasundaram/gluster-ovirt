package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn.OsTypeExtractor;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;

public class SideTabExtendedTemplateView extends AbstractSideTabWithDetailsView<VmTemplate, UserPortalTemplateListModel>
        implements SideTabExtendedTemplatePresenter.ViewDef {

    @Inject
    public SideTabExtendedTemplateView(UserPortalTemplateListProvider provider, ApplicationTemplates templates) {
        super(provider);
        initTable(templates);
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedTemplatePresenter.TYPE_SetSubTabPanelContent;
    }

    private void initTable(final ApplicationTemplates templates) {
        getTable().addColumn(new VmImageColumn<VmTemplate>(new OsTypeExtractor<VmTemplate>() {

            @Override
            public VmOsType extractOsType(VmTemplate item) {
                return item.getos();
            }
        }), "", "77px");

        SafeHtmlColumn<VmTemplate> nameAndDescriptionColumn = new SafeHtmlColumn<VmTemplate>() {
            @Override
            public SafeHtml getValue(VmTemplate template) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.append(templates.vmNameCellItem(template.getname()));

                if (template.getdescription() != null) {
                    builder.append(templates.vmDescriptionCellItem(template.getdescription()));
                }

                return builder.toSafeHtml();
            }
        };
        getTable().addColumn(nameAndDescriptionColumn, "");

        getTable().addActionButton(new UserPortalButtonDefinition<VmTemplate>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UserPortalButtonDefinition<VmTemplate>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
