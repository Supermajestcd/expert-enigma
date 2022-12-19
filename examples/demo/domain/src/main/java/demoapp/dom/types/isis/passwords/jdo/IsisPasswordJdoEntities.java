package demoapp.dom.types.isis.passwords.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Password;

@Service
public class IsisPasswordJdoEntities {

    public Optional<IsisPasswordJdo> find(final Password readOnlyProperty) {
        return repositoryService.firstMatch(IsisPasswordJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisPasswordJdo> all() {
        return repositoryService.allInstances(IsisPasswordJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
