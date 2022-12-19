package org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.testing.fakedata.applib.services.FakeDataService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;

import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts.data.FakeDataDemoObjectWithAll_create_withFakeData;

import lombok.Getter;
import lombok.Setter;

@lombok.experimental.Accessors(chain = true)
public class FakeDataDemoObjectWithAll_create3 extends FixtureScript {

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Integer numberToCreate;
    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Boolean withFakeData;

    @Getter(onMethod = @__( @Programmatic ))
    private List<FakeDataDemoObjectWithAll> demoObjects = Lists.newArrayList();

    @Override
    protected void execute(final ExecutionContext executionContext) {

        this.defaultParam("numberToCreate", executionContext, 3);
        this.defaultParam("withFakeData", executionContext, true);

        for (int i = 0; i < getNumberToCreate(); i++) {
            final FakeDataDemoObjectWithAll_create_withFakeData fs = new FakeDataDemoObjectWithAll_create_withFakeData().setWithFakeData(withFakeData);
            executionContext.executeChildT(this, fs);
            demoObjects.add(fs.getFakeDataDemoObject());
        }

    }

    @Inject FakeDataService fakeDataService;
}
