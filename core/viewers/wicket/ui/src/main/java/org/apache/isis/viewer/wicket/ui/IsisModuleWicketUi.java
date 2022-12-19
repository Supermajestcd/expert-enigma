package org.apache.isis.viewer.wicket.ui;

import org.apache.isis.viewer.wicket.model.IsisModuleWicketModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.IsisWicketThemeSupportDefault;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleWicketModel.class,

        // @Service's
        IsisWicketThemeSupportDefault.class,
})
public class IsisModuleWicketUi {
}
