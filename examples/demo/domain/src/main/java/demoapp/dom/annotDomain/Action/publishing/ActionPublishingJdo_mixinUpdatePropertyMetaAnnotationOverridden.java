package demoapp.dom.annotDomain.Action.publishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@ActionPublishingDisabledMetaAnnotation     // <.>
@Action(
    publishing = Publishing.ENABLED         // <.>
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyMetaAnnotatedOverridden"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs =
        "@ActionPublishingDisabledMetaAnnotation " +
        "@Action(publishing = ENABLED)"
)
public class ActionPublishingJdo_mixinUpdatePropertyMetaAnnotationOverridden {
    // ...
//end::class[]

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_mixinUpdatePropertyMetaAnnotationOverridden(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

//tag::class[]
    public ActionPublishingJdo act(final String value) {
        actionPublishingJdo.setPropertyMetaAnnotatedOverridden(value);
        return actionPublishingJdo;
    }
    public String default0Act() {
        return actionPublishingJdo.getPropertyMetaAnnotatedOverridden();
    }
}
//end::class[]
