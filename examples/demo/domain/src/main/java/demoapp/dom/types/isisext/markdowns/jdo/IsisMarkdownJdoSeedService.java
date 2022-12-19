package demoapp.dom.types.isisext.markdowns.jdo;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class IsisMarkdownJdoSeedService extends SeedServiceAbstract {

    public IsisMarkdownJdoSeedService() {
        super(IsisMarkdownJdoEntityFixture::new);
    }

    static class IsisMarkdownJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(IsisMarkdownJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<Markdown> samples;
    }
}
