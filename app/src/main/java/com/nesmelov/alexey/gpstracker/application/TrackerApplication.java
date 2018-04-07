package com.nesmelov.alexey.gpstracker.application;

import android.app.Application;

import com.nesmelov.alexey.gpstracker.application.components.DaggerRepositoryComponent;
import com.nesmelov.alexey.gpstracker.application.components.DaggerLocationUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.DaggerMapUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.LocationUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.MapUtilsComponent;
import com.nesmelov.alexey.gpstracker.application.components.RepositoryComponent;
import com.nesmelov.alexey.gpstracker.application.modules.AppContextModule;

/**
 * GPS tracker application class, provides components.
 */
public class TrackerApplication extends Application {

    private static RepositoryComponent sAppUtilsComp;
    private static LocationUtilsComponent sLocationUtilsComp;
    private static MapUtilsComponent sMapUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        final AppContextModule contextModule = new AppContextModule(this);
        sAppUtilsComp = DaggerRepositoryComponent
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
    public static RepositoryComponent getAppUtilsComp() {
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
