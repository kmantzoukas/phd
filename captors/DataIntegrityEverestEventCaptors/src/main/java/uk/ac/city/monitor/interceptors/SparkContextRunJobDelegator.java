package uk.ac.city.monitor.interceptors;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.apache.spark.SparkEnv$;
import org.apache.spark.TaskContext;
import org.apache.spark.rdd.RDD;
import scala.Function2;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction2;
import uk.ac.city.monitor.emitters.Emitter;
import uk.ac.city.monitor.emitters.EventEmitterFactory;
import uk.ac.city.monitor.enums.EmitterType;
import uk.ac.city.monitor.enums.OperationType;
import uk.ac.city.monitor.iterators.DataIntegrityMonitorableIterator;
import uk.ac.city.monitor.utils.Morpher;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class SparkContextRunJobDelegator implements Serializable{

    private final EmitterType type;
    private final Properties properties;

    public SparkContextRunJobDelegator(EmitterType type, Properties properties){
        this.type = type;
        this.properties = properties;
    }

    @RuntimeType
    public Object runJob(
            @Argument(0) RDD rdd,
            @Argument(1) Function2 f,
            @Argument(2) Object classTag,
            @Morph Morpher<Object> morpher,
            @This Object sc) throws IOException {

        Properties props = new Properties();
        props.put("host","10.207.1.103");
        props.put("port","10334");

        Emitter emitter = EventEmitterFactory.getInstance(EmitterType.SOCKET, props);
        emitter.connect();

        long start = new Date().getTime();

        String applicationId = SparkEnv$.MODULE$.get().conf().get("spark.app.id");
        String applicationName = SparkEnv$.MODULE$.get().conf().get("spark.app.name");

        final class Func extends AbstractFunction2<TaskContext , Iterator, Object> implements Serializable {

            @Override
            public Object apply(TaskContext context, Iterator it) {

                Map<String,String> parameters = new LinkedHashMap<>();
                parameters.put("appId", applicationId);
                parameters.put("appName", applicationName);
                parameters.put("rddId", String.valueOf(rdd.id()));
                parameters.put("partitionId", String.valueOf(context.getPartitionId()));

                return  f.apply(context, new DataIntegrityMonitorableIterator(it, type, properties, OperationType.READRDD, parameters));
            }

        }

        Object result = morpher.invoke(rdd, new Func(), classTag);
        long end = new Date().getTime();

        emitter.send(String.valueOf(end - start));
        emitter.close();

        return result;
    }
}
