package org.apache.ode.jacob.soup.jackson;

import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.ChannelProxy;
import org.apache.ode.jacob.soup.Continuation;
import org.apache.ode.jacob.soup.jackson.JacksonExecutionQueueImpl.ExecutionQueueImplDeserializer;
import org.apache.ode.jacob.soup.jackson.JacksonExecutionQueueImpl.ExecutionQueueImplSerializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacobModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public JacobModule() {
        super("jacob-module", Version.unknownVersion());
        
        final ChannelProxySerializer cps = new ChannelProxySerializer();
        addSerializer(ChannelProxy.class, cps);
        addSerializer(Continuation.class, new ContinuationSerializer());
        addSerializer(JacksonExecutionQueueImpl.class, new ExecutionQueueImplSerializer(cps));
        addDeserializer(JacksonExecutionQueueImpl.class, new ExecutionQueueImplDeserializer());
        addDeserializer(Continuation.class, new ContinuationDeserializer());
        addDeserializer(Channel.class, new ChannelProxyDeserializer());
        
        setDeserializerModifier(new BeanDeserializerModifier() {

            public JsonDeserializer<?> modifyDeserializer(
                    DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                
                // use channel proxy deserializer for channels.
                if (Channel.class.isAssignableFrom(beanDesc.getBeanClass()) && beanDesc.getBeanClass().isInterface()) {
                    return new ChannelProxyDeserializer();
                }

                return super.modifyDeserializer(config, beanDesc, deserializer);
            }
        });
    }
    
    @Override
    public void setupModule(SetupContext context) {
        context.appendAnnotationIntrospector(new JacobJacksonAnnotationIntrospector());
        super.setupModule(context);
    }

}
