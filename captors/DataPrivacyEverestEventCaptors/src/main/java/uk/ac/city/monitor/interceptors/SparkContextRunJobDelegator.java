package uk.ac.city.monitor.interceptors;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.apache.spark.rdd.RDD;
import scala.Function2;
import uk.ac.city.monitor.emitters.Emitter;
import uk.ac.city.monitor.emitters.EventEmitterFactory;
import uk.ac.city.monitor.enums.EmitterType;
import uk.ac.city.monitor.utils.Morpher;

import java.io.Serializable;
import java.util.Date;
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
            @This Object sc) {

        Properties props = new Properties();
        props.put("host","10.207.1.103");
        props.put("port","10334");

        Emitter emitter = EventEmitterFactory.getInstance(EmitterType.SOCKET, props);
        emitter.connect();

        long start = new Date().getTime();

        Object result = morpher.invoke(rdd, f, classTag);
        long end = new Date().getTime();

        emitter.send(String.valueOf(end - start));
        emitter.close();

        return result;
    }
}
