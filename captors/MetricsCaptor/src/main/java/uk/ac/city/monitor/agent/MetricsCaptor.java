package uk.ac.city.monitor.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.spark.rdd.RDD;
import uk.ac.city.monitor.emitters.Emitter;
import uk.ac.city.monitor.emitters.EventEmitterFactory;
import uk.ac.city.monitor.enums.EmitterType;
import uk.ac.city.monitor.utils.Morpher;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.*;

public class MetricsCaptor implements Serializable {

    private static EmitterType type;
    private static Properties properties = new Properties();

    public static void premain(String configuration, Instrumentation instrumentation) throws IOException {

        properties.load(new StringReader(configuration.replaceAll(",", "\n")));
        EmitterType emitterType = EmitterType.valueOf(properties.getProperty("emitter").toUpperCase());

        switch (emitterType){
            case RABBITMQ:
                type = EmitterType.RABBITMQ;
                break;
            case SOCKET:
                type = EmitterType.SOCKET;
                break;
        }

        new AgentBuilder.Default()
                .type(type -> type.getName().equals("org.apache.spark.SparkContext"))
                .transform((builder, typeDescription, classLoader, module) -> {
                    return builder
                            .serialVersionUid(1L)
                            .method(method -> (method.getName().equals("runJob") && method.getParameters().size() == 3))
                            .intercept(MethodDelegation
                                    .withDefaultConfiguration()
                                    .withBinders(Morph.Binder.install(Morpher.class))
                                    .to(SparkContextRunJobInterceptor.class));
                }).installOn(instrumentation);
    }

    public static class SparkContextRunJobInterceptor<T, U> {

        @RuntimeType
        public static Object[] runJob(
                @Argument(0) RDD rdd,
                @Argument(1) Object f,
                @Argument(2) Object classTag,
                @Morph Morpher<Object[]> m) {

            Emitter emitter = EventEmitterFactory.getInstance(type, properties);
            emitter.connect();

            long start = new Date().getTime();

            Object[] result = m.invoke(new Object[]{rdd, f, classTag});

            long end = new Date().getTime();

            emitter.send(String.valueOf(end-start));
            emitter.close();

            return result;
        }
    }
}