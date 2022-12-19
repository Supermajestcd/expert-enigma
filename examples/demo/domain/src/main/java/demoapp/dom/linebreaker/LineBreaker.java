package demoapp.dom.linebreaker;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * REST endpoint to allow for remote application shutdown 
 *
 */
@DomainService(nature = NatureOfService.REST, objectType = "demo.LineBreaker")
@Log4j2
public class LineBreaker {
    
    @Inject private IsisInteractionTracker isisInteractionTracker;
    
    @Action(semantics = SemanticsOf.SAFE)
    public void shutdown() {
        log.info("about to shutown the JVM");

        // allow for current interaction to complete gracefully
        isisInteractionTracker.currentInteraction()
        .ifPresent(interaction->{
            interaction.setOnClose(()->System.exit(0));
        });
    }

}
