package demoapp.dom.types.javalang.doubles.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperDoubleJdoEntities {

    public Optional<WrapperDoubleJdo> find(final Double readOnlyProperty) {
        return repositoryService.firstMatch(WrapperDoubleJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperDoubleJdo> all() {
        return repositoryService.allInstances(WrapperDoubleJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
