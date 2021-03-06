package org.ovirt.engine.ui.common.widget.refresh;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent.UiCommonInitHandler;
import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;

/**
 * Provides refresh rate management for a {@link GridController}.
 */
public abstract class AbstractRefreshManager<T extends BaseRefreshPanel> {

    private static final Integer DEFAULT_REFRESH_RATE = GridTimer.DEFAULT_NORMAL_RATE;
    private static final String REFRESH_RATE_ITEM_NAME = "GridRefreshRate";
    private static final Set<Integer> REFRESH_RATES = new LinkedHashSet<Integer>();

    static {
        REFRESH_RATES.add(DEFAULT_REFRESH_RATE);
        REFRESH_RATES.add(10000);
        REFRESH_RATES.add(20000);
        REFRESH_RATES.add(30000);
    }

    /**
     * Returns acceptable refresh rates.
     */
    public static Set<Integer> getRefreshRates() {
        return Collections.unmodifiableSet(REFRESH_RATES);
    }

    private final ModelProvider<? extends GridController> modelProvider;
    private final ClientStorage clientStorage;
    private final T refreshPanel;

    private GridController controller;

    public AbstractRefreshManager(ModelProvider<? extends GridController> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        this.modelProvider = modelProvider;
        this.clientStorage = clientStorage;
        this.refreshPanel = createRefreshPanel();
        listenOnManualRefresh();

        // Add handler to be notified when UiCommon models are (re)initialized
        eventBus.addHandler(UiCommonInitEvent.getType(), new UiCommonInitHandler() {
            @Override
            public void onUiCommonInit(UiCommonInitEvent event) {
                updateController();
            }
        });

        updateController();
    }

    private void updateController() {
        this.controller = modelProvider.getModel();

        controller.setRefreshRate(readRefreshRate());
        controller.getTimer().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onRefresh(event.getValue());
            }
        });
    }

    /**
     * When the user clicks the refresh button, enforce the refresh without even asking the timer.
     */
    protected void listenOnManualRefresh() {
        refreshPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                controller.refresh();
            }
        });
    }

    protected abstract T createRefreshPanel();

    protected void onRefresh(String status) {
        refreshPanel.showStatus(status);
    }

    public T getRefreshPanel() {
        return refreshPanel;
    }

    public void setCurrentRefreshRate(int newRefreshRate) {
        controller.setRefreshRate(newRefreshRate);
        saveRefreshRate(newRefreshRate);
    }

    public int getCurrentRefreshRate() {
        return controller.getRefreshRate();
    }

    String getRefreshRateItemKey() {
        return REFRESH_RATE_ITEM_NAME + "_" + controller.getId();
    }

    void saveRefreshRate(int newRefreshRate) {
        clientStorage.setLocalItem(getRefreshRateItemKey(), String.valueOf(newRefreshRate));
    }

    /**
     * Returns refresh rate value if it exists. Otherwise, returns default refresh rate.
     */
    int readRefreshRate() {
        String refreshRate = clientStorage.getLocalItem(getRefreshRateItemKey());

        try {
            return new Integer(refreshRate).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_REFRESH_RATE;
        }
    }

}
