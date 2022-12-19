package demoapp.dom.annotDomain.Property.domainEvent.subscribers;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventVm;

// tag::class[]
@DomainService(objectType = "demo.PropertyDomainEventControlService")
@RequiredArgsConstructor(onConstructor_ = { @Inject })
class PropertyDomainEventControlService {

    final ServiceRegistry serviceRegistry;

    PropertyDomainEventControlStrategy controlStrategy = PropertyDomainEventControlStrategy.DO_NOTHING;   // <.>

    @EventListener(PropertyDomainEventVm.TextDomainEvent.class)     // <.>
    public void on(PropertyDomainEventVm.TextDomainEvent ev) {
        controlStrategy.on(ev, serviceRegistry);
    }

}
// end::class[]
