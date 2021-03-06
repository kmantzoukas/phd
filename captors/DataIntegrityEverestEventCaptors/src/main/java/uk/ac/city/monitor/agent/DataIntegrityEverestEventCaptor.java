package uk.ac.city.monitor.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.StringReader;
import java.lang.instrument.Instrumentation;
import java.util.Properties;
import uk.ac.city.monitor.enums.EmitterType;
import uk.ac.city.monitor.interceptors.*;
import uk.ac.city.monitor.utils.Morpher;

import static net.bytebuddy.implementation.MethodDelegation.to;

public class DataIntegrityEverestEventCaptor {

    final static Logger logger = Logger.getLogger(DataIntegrityEverestEventCaptor.class);
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
            .type(type -> type.getName().equals("org.apache.spark.rdd.HadoopRDD"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                        .serialVersionUid(1L)
                        .method(method -> method.getName().equals("compute"))
                        .intercept(
                                MethodDelegation
                                        .withDefaultConfiguration()
                                        .withBinders(Morph.Binder.install(Morpher.class))
                                        .to(new HadoopRDDComputeDelegator(type, properties)));
            })
            .type(type -> type.getName().equals("org.apache.spark.rdd.MapPartitionsRDD"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                    .serialVersionUid(1L)
                    .method(method -> method.getName().equals("compute"))
                    .intercept(to(new MapPartitionsRDDComputeInterceptor(type, properties)));
            })
            .type(type -> type.getName().equals("org.apache.spark.api.python.PythonRDD"))
                .transform((builder, typeDescription, classLoader, module) -> {
                    return builder
                            .serialVersionUid(1L)
                            .method(method -> method.getName().equals("compute"))
                            .intercept(MethodDelegation
                                    .withDefaultConfiguration()
                                    .withBinders(Morph.Binder.install(Morpher.class))
                                    .to(new PythonRDDComputeDelegator(type, properties)));
                })
            .type(type -> type.getName().equals("org.apache.spark.rdd.ShuffledRDD"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                    .serialVersionUid(1L)
                    .method(method -> method.getName().equals("compute"))
                    .intercept(
                        MethodDelegation
                            .withDefaultConfiguration()
                            .withBinders(Morph.Binder.install(Morpher.class))
                            .to(new ShuffledRDDComputeDelegator(type, properties)));
            })
            .type(type -> type.getName().equals("org.apache.spark.util.collection.ExternalSorter"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                    .serialVersionUid(1L)
                    .method(method -> method.getName().equals("writePartitionedFile"))
                    .intercept(MethodDelegation.to(new ExternalSorterWritePartitionedFileDelegator(type, properties)));
            })
            .type(type -> type.getName().equals("org.apache.spark.shuffle.sort.BypassMergeSortShuffleWriter"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                    .serialVersionUid(1L)
                    .method(method -> method.getName().equals("writePartitionedFile"))
                    .intercept(MethodDelegation.to(new BypassMergeSortShuffleWriterWritePartitionedFileDelegator(type, properties)));
            })
            .type(type -> type.getName().equals("org.apache.spark.storage.ShuffleBlockFetcherIterator"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                    .serialVersionUid(1L)
                    .method(method -> method.getName().equals("flatMap"))
                    .intercept(MethodDelegation
                        .withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(Morpher.class))
                        .to(new ShuffleBlockFetcherIteratorFlatMapDelegator(type, properties)));
            })
            .type(type -> type.getName().equals("org.apache.spark.SparkContext"))
            .transform((builder, typeDescription, classLoader, module) -> {
                return builder
                    .serialVersionUid(1L)
                    .method(method -> (method.getName().equals("runJob") && method.getParameters().size() == 3))
                    .intercept(MethodDelegation
                        .withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(Morpher.class))
                        .to(new SparkContextRunJobDelegator(type, properties)));
             })
            .installOn(instrumentation);

            logger.info("Event captors has been successfully installed.");
    }
}