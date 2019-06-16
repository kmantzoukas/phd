package uk.ac.city.monitor.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.log4j.Logger;
import org.apache.spark.SparkEnv$;
import org.apache.spark.rdd.RDD;
import uk.ac.city.monitor.emitters.Emitter;
import uk.ac.city.monitor.emitters.EventEmitterFactory;
import uk.ac.city.monitor.enums.EmitterType;
import uk.ac.city.monitor.enums.OperationType;
import uk.ac.city.monitor.utils.MonitoringUtilities;
import uk.ac.city.monitor.utils.Morpher;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.UnknownHostException;
import java.util.*;

public class DataAvailabilityEverestEventCaptor implements Serializable {

    final static Logger logger = Logger.getLogger(DataAvailabilityEverestEventCaptor.class);
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
                })
                .installOn(instrumentation);

        logger.info("Event captors has been successfully installed.");

    }

    /*
    Interceptor for method compute() for class SparkContext
     */
    public static class SparkContextRunJobInterceptor<T, U> {

        @RuntimeType
        public static Object[] runJob(
                @Argument(0) RDD rdd,
                @Argument(1) Object f,
                @Argument(2) Object classTag,
                @Morph Morpher<Object[]> m)
                throws JAXBException,
                UnknownHostException,
                DatatypeConfigurationException {

            String applicationId = SparkEnv$.MODULE$.get().conf().get("spark.app.id");
            String applicationName = SparkEnv$.MODULE$.get().conf().get("spark.app.name");

            Emitter emitter = EventEmitterFactory.getInstance(type, properties);
            emitter.connect();

            emitter.send(MonitoringUtilities.createEvent(properties.getProperty("eventStype"),
                    OperationType.ACTION,
                    "start",
                    applicationId,
                    applicationName,
                    rdd,
                    null));

            Object[] result = m.invoke(new Object[]{rdd, f, classTag});

            emitter.send(MonitoringUtilities.createEvent(properties.getProperty("eventStype"),
                    OperationType.ACTION,
                    "end",
                    applicationId,
                    applicationName,
                    rdd,
                    null));

            emitter.close();

            return result;
        }
    }
}