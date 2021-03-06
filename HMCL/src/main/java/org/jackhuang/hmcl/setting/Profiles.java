/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2018  huangyuhui <huanghongxun2008@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.setting;

import com.jfoenix.concurrency.JFXUtilities;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.jackhuang.hmcl.Launcher;
import org.jackhuang.hmcl.event.EventBus;
import org.jackhuang.hmcl.event.RefreshedVersionsEvent;

import java.io.File;
import java.util.HashSet;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static org.jackhuang.hmcl.setting.ConfigHolder.config;
import static org.jackhuang.hmcl.ui.FXUtils.onInvalidating;
import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public final class Profiles {

    public static final String DEFAULT_PROFILE = "Default";
    public static final String HOME_PROFILE = "Home";

    private static InvalidationListener listener = o -> loadVersion();

    private Profiles() {
    }

    public static String getProfileDisplayName(Profile profile) {
        switch (profile.getName()) {
            case Profiles.DEFAULT_PROFILE:
                return i18n("profile.default");
            case Profiles.HOME_PROFILE:
                return i18n("profile.home");
            default:
                return profile.getName();
        }
    }

    private static final ObservableList<Profile> profiles = observableArrayList(profile -> new Observable[] { profile });
    private static final ReadOnlyListWrapper<Profile> profilesWrapper = new ReadOnlyListWrapper<>(profiles);

    private static ObjectProperty<Profile> selectedProfile = new SimpleObjectProperty<Profile>() {
        {
            profiles.addListener(onInvalidating(this::invalidated));

            this.addListener(this::change);
        }

        @Override
        protected void invalidated() {
            Profile profile = get();

            if (get() != null)
                get().removeListener(listener);

            if (profiles.isEmpty()) {
                if (profile != null) {
                    set(null);
                    return;
                }
            } else {
                if (!profiles.contains(profile)) {
                    set(profiles.get(0));
                    return;
                }
            }

            if (!initialized)
                return;

            config().setSelectedProfile(profile == null ? "" : profile.getName());
            loadVersion();
        }

        private void change(ObservableValue<? extends Profile> observableValue, Profile oldProfile, Profile newProfile) {
            if (oldProfile != null)
                oldProfile.selectedVersionProperty().removeListener(listener);
            if (newProfile != null)
                newProfile.selectedVersionProperty().addListener(listener);
        }
    };

    private static void checkProfiles() {
        if (profiles.isEmpty()) {
            Profile current = new Profile(Profiles.DEFAULT_PROFILE, new File(".minecraft"), new VersionSetting(), null, true);
            Profile home = new Profile(Profiles.HOME_PROFILE, Launcher.MINECRAFT_DIRECTORY);
            Platform.runLater(() -> profiles.addAll(current, home));
        }
    }

    /**
     * True if {@link #init()} hasn't been called.
     */
    private static boolean initialized = false;

    static {
        profiles.addListener(onInvalidating(Profiles::updateProfileStorages));
        profiles.addListener(onInvalidating(Profiles::checkProfiles));

        selectedProfile.addListener((a, b, newValue) -> {
            if (newValue != null)
                newValue.getRepository().refreshVersionsAsync().start();
        });
    }

    private static void updateProfileStorages() {
        // don't update the underlying storage before data loading is completed
        // otherwise it might cause data loss
        if (!initialized)
            return;
        // update storage
        config().getConfigurations().clear();
        config().getConfigurations().putAll(profiles.stream().collect(Collectors.toMap(Profile::getName, it -> it)));
    }

    /**
     * Called when it's ready to load profiles from {@link ConfigHolder#config()}.
     */
    static void init() {
        if (initialized)
            throw new IllegalStateException("Already initialized");

        HashSet<String> names = new HashSet<>();
        config().getConfigurations().forEach((name, profile) -> {
            if (!names.add(name)) return;
            profiles.add(profile);
            profile.setName(name);
        });
        checkProfiles();

        // Platform.runLater is necessary or profiles will be empty
        // since checkProfiles adds 2 base profile later.
        Platform.runLater(() -> {
            selectedProfile.set(
                    profiles.stream()
                            .filter(it -> it.getName().equals(config().getSelectedProfile()))
                            .findFirst()
                            .orElse(profiles.get(0)));

            initialized = true;
        });

        EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).registerWeak(event -> {
            JFXUtilities.runInFX(() -> {
                if (selectedProfile.get() != null && selectedProfile.get().getRepository() == event.getSource())
                    loadVersion();
            });
        });
    }

    public static ObservableList<Profile> getProfiles() {
        return profiles;
    }

    public static ReadOnlyListProperty<Profile> profilesProperty() {
        return profilesWrapper.getReadOnlyProperty();
    }

    public static Profile getSelectedProfile() {
        return selectedProfile.get();
    }

    public static void setSelectedProfile(Profile profile) {
        selectedProfile.set(profile);
    }

    public static ObjectProperty<Profile> selectedProfileProperty() {
        return selectedProfile;
    }

    private static final ReadOnlyStringWrapper selectedVersion = new ReadOnlyStringWrapper();

    public static ReadOnlyStringProperty selectedVersionProperty() {
        return selectedVersion.getReadOnlyProperty();
    }

    public static String getSelectedVersion() {
        return selectedVersion.get();
    }

    private static void loadVersion() {
        Profile profile = selectedProfile.get();
        if (profile == null || !profile.getRepository().isLoaded()) return;
        JFXUtilities.runInFX(() ->
                selectedVersion.set(profile.getSelectedVersion()));
    }
}
