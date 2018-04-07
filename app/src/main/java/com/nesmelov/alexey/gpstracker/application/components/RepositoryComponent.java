package com.nesmelov.alexey.gpstracker.application.components;

import com.nesmelov.alexey.gpstracker.application.modules.AddressRepositoryModule;
import com.nesmelov.alexey.gpstracker.application.modules.AppContextModule;
import com.nesmelov.alexey.gpstracker.application.modules.AppDatabaseModule;
import com.nesmelov.alexey.gpstracker.application.modules.GeocoderModule;
import com.nesmelov.alexey.gpstracker.viewmodels.MapFragmentViewModel;
import com.nesmelov.alexey.gpstracker.viewmodels.SearchActivityViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppContextModule.class, AppDatabaseModule.class,
        GeocoderModule.class, AddressRepositoryModule.class})
public interface RepositoryComponent {
    void inject(final SearchActivityViewModel activityViewModel);
    void inject(final MapFragmentViewModel fragmentViewModel);
}
