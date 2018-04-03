package com.nesmelov.alexey.gpstracker.application;

import android.app.Application;

import com.nesmelov.alexey.gpstracker.application.components.DaggerAppUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.DaggerLocationUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.DaggerMapUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.LocationUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.MapUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.modules.AppContextModule;
import com.nesmelov.alexey.gpstracker.application.components.AppUtilsComponent;

/**
 * GPS tracker application class, provides components.
 */
public class TrackerApplication extends Application {

    private static AppUtilsComponent sAppUtilsComp;
    private static LocationUtilsComponent sLocationUtilsComp;
    private static MapUtilsComponent sMapUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        final AppContextModule contextModule = new AppContextModule(this);
        sAppUtilsComp = DaggerAppUtilsComponent
                .builder()
                .appContextModule(contextModule)
                .build();
        sLocationUtilsComp = DaggerLocationUtilsComponent
                .builder()
                .appContextModule(contextModule)
                .build();
        sMapUtils = DaggerMapUtilsComponent
                .builder()
                .appContextModule(contextModule)
                .build();
    }

    /**
     * Returns application utils component.
     *
     * @return application utils component.
     */
    public static AppUtilsComponent getAppUtilsComp() {
        return sAppUtilsComp;
    }

    /**
     * Returns location utils component.
     *
     * @return location utils component.
     */
    public static LocationUtilsComponent getLocationUtilsComp() {
        return sLocationUtilsComp;
    }

    /**
     * Returns map utils component.
     *
     * @return map utils component.
     */
    public static MapUtilsComponent getMapUtilsComp() {
        return sMapUtils;
    }
}
