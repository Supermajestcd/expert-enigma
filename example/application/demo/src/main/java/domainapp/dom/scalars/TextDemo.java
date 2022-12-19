package domainapp.dom.scalars;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import domainapp.utils.DemoStub;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
@Log
public class TextDemo extends DemoStub {

    @Override
    public void initDefaults() {

        log.info("TextDemo::initDefaults");
        
        string = "a string (click me)";
        stringMultiline = "A multiline string\nspanning\n3 lines. (click me)";
        
        stringReadonly = "a readonly string (but allows text select)";
        stringMultilineReadonly = "A readonly string\nspanning\n3 lines. (but allows text select)";
    }
    
    // -- EDITABLE
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @XmlElement @Getter @Setter private String string;
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(multiLine=3)
    @XmlElement @Getter @Setter private String stringMultiline;

    // -- READONLY
    
    @Property(editing=Editing.DISABLED)
    @XmlElement @Getter @Setter private String stringReadonly;
    
    @Property(editing=Editing.DISABLED)
    @PropertyLayout(multiLine=3)
    @XmlElement @Getter @Setter private String stringMultilineReadonly;
    
}
