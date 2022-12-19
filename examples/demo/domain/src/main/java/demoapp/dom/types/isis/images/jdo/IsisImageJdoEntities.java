package demoapp.dom.types.isis.images.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Image;

@Service
public class IsisImageJdoEntities {

    public Optional<IsisImageJdo> find(final Image readOnlyProperty) {
        return repositoryService.firstMatch(IsisImageJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisImageJdo> all() {
        return repositoryService.allInstances(IsisImageJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
