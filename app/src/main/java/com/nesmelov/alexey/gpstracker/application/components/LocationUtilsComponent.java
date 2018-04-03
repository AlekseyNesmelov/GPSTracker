package com.nesmelov.alexey.gpstracker.application.components;

import com.nesmelov.alexey.gpstracker.application.modules.AppContextModule;
import com.nesmelov.alexey.gpstracker.application.modules.LocationModule;
import com.nesmelov.alexey.gpstracker.ui.fragments.MapFragment;
import com.nesmelov.alexey.gpstracker.viewmodels.MapFragmentViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppContextModule.class, LocationModule.class})
public interface LocationUtilsComponent {
    void inject(final MapFragment fragment);
}
