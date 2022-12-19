package demoapp.dom.annotDomain.Action.command;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@Action(
    command = CommandReification.ENABLED        // <.>
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "property"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@Action(command = ENABLED)"
)
public class ActionCommandJdo_mixinUpdateProperty {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;

    public ActionCommandJdo_mixinUpdateProperty(ActionCommandJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }

    public ActionCommandJdo act(final String value) {
        actionCommandJdo.setProperty(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getProperty();
    }
//tag::class[]
}
//end::class[]
