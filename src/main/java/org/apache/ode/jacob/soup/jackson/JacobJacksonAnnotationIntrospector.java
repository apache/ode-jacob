package org.apache.ode.jacob.soup.jackson;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;

public class JacobJacksonAnnotationIntrospector extends
        JacksonAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    /* enable object ids for all objects. */
    @Override
    public ObjectIdInfo findObjectIdInfo(Annotated ann) {
        return new ObjectIdInfo("@id", Object.class, ObjectIdGenerators.IntSequenceGenerator.class);
    }

}
