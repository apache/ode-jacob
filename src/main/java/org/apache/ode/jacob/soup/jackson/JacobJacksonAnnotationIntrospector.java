package org.apache.ode.jacob.soup.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

public class JacobJacksonAnnotationIntrospector extends
        JacksonAnnotationIntrospector {

    private static final long serialVersionUID = 1L;
    private TypeResolverBuilder<?> jacobTypeResolver = new JacobTypeResolverBuilder();

    /* enable object ids for all objects. */
    @Override
    public ObjectIdInfo findObjectIdInfo(Annotated ann) {
        return new ObjectIdInfo("@id", Object.class, ObjectIdGenerators.IntSequenceGenerator.class);
    }

    /* use custom type resolver */
    @Override
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config,
            AnnotatedClass ac, JavaType baseType) {
        return jacobTypeResolver;
    }

    /* disable creator visibility */
    // TODO: Check if there is a better way to only serialize non-transient fields.
    @Override
    public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac,
            VisibilityChecker<?> checker) {
        return VisibilityChecker.Std.defaultInstance().with(Visibility.NONE).withVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }
    
}
