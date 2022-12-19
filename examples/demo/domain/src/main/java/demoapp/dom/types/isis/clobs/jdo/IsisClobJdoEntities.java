package demoapp.dom.types.isis.clobs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Clob;

@Service
public class IsisClobJdoEntities {

    public Optional<IsisClobJdo> find(final Clob readOnlyProperty) {
        return repositoryService.firstMatch(IsisClobJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisClobJdo> all() {
        return repositoryService.allInstances(IsisClobJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
