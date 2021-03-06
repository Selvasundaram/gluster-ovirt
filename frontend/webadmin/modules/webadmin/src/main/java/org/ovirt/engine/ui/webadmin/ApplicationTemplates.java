package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface ApplicationTemplates extends CommonApplicationTemplates {

    /**
     * Creates a progress bar template.
     *
     * @param progress
     *            Progress value in percent.
     * @param text
     *            Text to show within the progress bar.
     */
    @Template("<div class='engine-progress-box'>" +
            "<div style='background: {2}; width: {0}%; height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div></div>")
    SafeHtml progressBar(int progress, String text, String color);

    /**
     * Creates a tree-item HTML
     *
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node title
     * @return
     */
    @Template("<span style='position: relative; bottom: 1px;'>{0}</span>" +
            "<span style='position: relative; bottom: 7px;'>{1}</span>")
    SafeHtml treeItem(SafeHtml imageHtml, String text);

    /**
     * Creates a bookmark-item HTML
     *
     * @param text
     *            the bookmark title
     */
    @Template("<span id='{0}' style='display: inline-block; padding: 5px;'>{1}</span>")
    SafeHtml bookmarkItem(String id, String text);

    /**
     * Creates a tag-item HTML
     *
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node title
     * @return
     */
    @Template("<span style='position: relative; border: 1px solid {3}; " +
            "bottom: 4px; padding: 0 3px; margin: 0 1px;  white-space: nowrap; background-color: {2};'>" +
            "<span style='position: relative; top: 1px;'>{0}</span> {1}</span>")
    SafeHtml tagItem(SafeHtml imageHtml, String text, String backgroundColor, String borderColor);

    /**
     * Creates a tag-button HTML
     *
     * @param imageHtml
     *            the image HTML
     * @return
     */
    @Template("<span style='position: relative; border: 1px solid {2}; visibility: {3};" +
            " bottom: 4px; padding: 0 3px; background-color: {1};'>{0}</span>")
    SafeHtml tagButton(SafeHtml imageHtml, String backgroundColor, String borderColor, String visibility);

    @Template("<span style='position: relative; white-space: nowrap;'><span>{0}</span>{1} Alerts</span>")
    SafeHtml alertFooterHeader(SafeHtml imageHtml, int alertCount);

    @Template("<table cellspacing='0' cellpadding='0'><tr>" +
            "<td style='background: url({2});width: 4px;'></td>" +
            "<td style='text-align:center;'>" +
            "<div class='{5}' style='background: url({3}) repeat-x; white-space: nowrap; height: 20px; line-height: 20px;'>" +
            "<span style='vertical-align: middle; margin-right: 3px; line-height: 20px;'>{0}</span>{1}</div>" +
            "</td>" +
            "<td style='background: url({4});width: 4px;'></td>" +
            "</tr></table>")
    SafeHtml alertEventButton(SafeHtml image, String text, String start, String stretch,
            String end, String contentStyleName);

    @Template("<div style=\"text-align: center; padding-top: 6px;\">{0}{1}</div>")
    SafeHtml statusWithAlertTemplate(SafeHtml statusImage, SafeHtml alertImage);

    @Template("<div style=\"text-align: center; padding-top: 6px;\">{0}</div>")
    SafeHtml statusTemplate(SafeHtml statusImage);

}
