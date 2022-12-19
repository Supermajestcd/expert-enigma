package org.apache.isis.extensions.commandreplay.secondary.mixins;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandreplay.secondary.IsisModuleExtCommandReplaySecondary;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
    semantics = SemanticsOf.SAFE,
    domainEvent = Object_openOnPrimary.ActionDomainEvent.class,
    restrictTo = RestrictTo.PROTOTYPING
)
@RequiredArgsConstructor
public class Object_openOnPrimary {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandReplaySecondary.ActionDomainEvent<Object_openOnPrimary> { }

    final Object object;

    public URL act() {
        val baseUrlPrefix = lookupBaseUrlPrefix();
        val urlSuffix = bookmarkService.bookmarkFor(object).toString();

        try {
            return new URL(baseUrlPrefix + urlSuffix);
        } catch (MalformedURLException e) {
            throw new ApplicationException(e);
        }
    }
    public boolean hideAct() {
        return !secondaryConfig.isConfigured();
    }

    private String lookupBaseUrlPrefix() {
        return secondaryConfig.getPrimaryBaseUrlWicket() + "entity/";
    }

    @Inject SecondaryConfig secondaryConfig;
    @Inject BookmarkService bookmarkService;

}
