/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2017  huangyuhui <huanghongxun2008@126.com>
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
package org.jackhuang.hmcl.ui.profile;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import org.jackhuang.hmcl.setting.Profile;
import org.jackhuang.hmcl.setting.Profiles;
import org.jackhuang.hmcl.ui.construct.AdvancedListItem;

public class ProfileAdvancedListItem extends AdvancedListItem {
    private ObjectProperty<Profile> profile = new SimpleObjectProperty<Profile>() {

        @Override
        protected void invalidated() {
            Profile profile = get();
            if (profile == null) {
            } else {
                titleProperty().set(Profiles.getProfileDisplayName(profile));
                subtitleProperty().set(profile.getGameDir().toString());
            }
        }
    };

    public ProfileAdvancedListItem() {
        imageProperty().set(new Image("/assets/img/craft_table.png"));
    }

    public ObjectProperty<Profile> profileProperty() {
        return profile;
    }
}
