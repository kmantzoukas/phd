package uk.ac.city.monitor.agent;

import java.io.IOException;
import java.io.StringReader;
import java.lang.instrument.Instrumentation;
import java.util.*;

import org.apache.log4j.Logger;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import uk.ac.city.monitor.enums.EmitterType;
import uk.ac.city.monitor.interceptors.RDDComputeInterceptor;
import uk.ac.city.monitor.utils.Morpher;

public class DataPrivacyAgent {

	final static Logger logger = Logger.getLogger(DataPrivacyAgent.class);
    private static EmitterType type;
    private static Properties properties = new Properties();

    public static void premain(String configuration, Instrumentation instrumentation) throws IOException {

        properties.load(new StringReader(configuration.replaceAll(",", "\n")));
        EmitterType emitterType = EmitterType.valueOf(properties.getProperty("emitter").toUpperCase());

        Set<String> coreRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.rdd.BlockRDD",
                        "org.apache.spark.rdd.CartesianRDD",
                        "org.apache.spark.rdd.CheckpointRDD",
                        "org.apache.spark.rdd.CoalescedRDD",
                        "org.apache.spark.rdd.CoGroupedRDD",
                        "org.apache.spark.rdd.EmptyRDD",
                        "org.apache.spark.rdd.HadoopRDD",
                        "org.apache.spark.rdd.HadoopRDD",
                        "org.apache.spark.rdd.JdbcRDD",
                        "org.apache.spark.rdd.MapPartitionsRDD",
                        "org.apache.spark.rdd.NewHadoopRDD",
                        "org.apache.spark.rdd.NewHadoopMapPartitionsWithSplitRDD",
                        "org.apache.spark.rdd.ParallelCollectionRDD",
                        "org.apache.spark.rdd.PartitionerAwareUnionRDD",
                        "org.apache.spark.rdd.PartitionPruningRDD",
                        "org.apache.spark.rdd.PartitionwiseSampledRDD",
                        "org.apache.spark.rdd.PipedRDD",
                        "org.apache.spark.rdd.MyCoolRDD",
                        "org.apache.spark.rdd.ShuffledRDD",
                        "org.apache.spark.rdd.SubtractedRDD",
                        "org.apache.spark.rdd.UnionRDD",
                        "org.apache.spark.rdd.ZippedPartitionsBaseRDD",
                        "org.apache.spark.rdd.ZippedWithIndexRDD"
                ));

        Set<String> graphxRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.graphx.EdgeRDD",
                        "org.apache.spark.graphx.VertexRDD"
                ));

        Set<String> mlibRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.mllib.rdd.RandomRDD",
                        "org.apache.spark.mllib.rdd.RandomVectorRDD"
                ));

        Set<String> sqlRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.sql.execution.EmptyRDDWithPartitions",
                        "org.apache.spark.sql.execution.ShuffledRowRDD",
                        "org.apache.spark.sql.execution.datasources.FileScanRDD",
                        "oorg.apache.spark.sql.execution.datasources.jdbc.JDBCRDD",
                        "org.apache.spark.sql.execution.datasources.v2.DataSourceRDD",
                        "org.apache.spark.sql.execution.streaming.continuous.ContinuousDataSourceRDD",
                        "org.apache.spark.sql.execution.streaming.continuous.ContinuousWriteRDD",
                        "org.apache.spark.sql.execution.streaming.state.StateStoreRDD"
                ));

        Set<String> kafkaRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.sql.kafka010.KafkaSourceRDD"
                ));

        Set<String> streamingKafkaRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.streaming.kafka010.KafkaRDD"
                ));

        Set<String> streamingRDDs = new HashSet<>(Arrays
                .asList("org.apache.spark.streaming.rdd.MapWithStateRDD"
                ));



        switch (emitterType){
            case RABBITMQ:
                type = EmitterType.RABBITMQ;
                break;
            case SOCKET:
                type = EmitterType.SOCKET;
                break;
        }

        new AgentBuilder.Default()

                /*
               Element matcher for class org.apache.spark.rdd.HadoopRDD
                */
                .type(type ->
                    coreRDDs.contains(type.getName()) ||
                    graphxRDDs.contains(type.getName()) ||
                    mlibRDDs.contains(type.getName()) ||
                    sqlRDDs.contains(type.getName()) ||
                    kafkaRDDs.contains(type.getName()) ||
                    streamingKafkaRDDs.contains(type.getName()) ||
                    streamingRDDs.contains(type.getName())
                )
                /*
               Intercept all calls on HadoopRDD.compute() method
                */
                .transform((builder, typeDescription, classLoader, module) -> {
                    return builder
                            .serialVersionUid(1L)
                            .method(method -> method.getName().equals("compute"))
                            .intercept(
                                    MethodDelegation
                                            .withDefaultConfiguration()
                                            .withBinders(Morph.Binder.install(Morpher.class))
                                            .to(new RDDComputeInterceptor(type, properties)));
                })
                .installOn(instrumentation);

            logger.info("Event captors has been successfully installed.");
    }
}