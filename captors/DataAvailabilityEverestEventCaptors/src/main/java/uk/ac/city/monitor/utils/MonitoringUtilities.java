package uk.ac.city.monitor.utils;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.apache.spark.rdd.RDD;
import org.slaatsoi.eventschema.*;
import uk.ac.city.monitor.agent.DataAvailabilityEverestEventCaptor;
import uk.ac.city.monitor.enums.DirectionType;
import uk.ac.city.monitor.enums.OperationType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MonitoringUtilities {

    final static Logger logger = Logger.getLogger(MonitoringUtilities.class);

    /*
    Create an XML representation of the event to be emitted. A series of parameters are passed as arguments based in the security property
    that the agent collects events for. Every security property mandates the agent collects different types of meta-information
     */
    public static String createEventXML(OperationType type,
                                        String operationName,
                                        String applicationId,
                                        String applicationName,
                                        RDD rdd,
                                        Partition split)
            throws JAXBException, UnknownHostException, DatatypeConfigurationException {

        EventInstance event = new EventInstance();

        EventIdType eventID = new EventIdType();
        eventID.setID(generateRandomLong());
        eventID.setEventTypeID("ServiceOperationCallEndEvent");

        EventContextType eventContext = new EventContextType();

        EventTime time = new EventTime();
        time.setTimestamp(System.currentTimeMillis());
        time.setCollectionTime(DatatypeFactory
                .newInstance()
                .newXMLGregorianCalendar(
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                .format(new Date())));
        time.setReportTime(DatatypeFactory
                .newInstance()
                .newXMLGregorianCalendar(
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                .format(new Date())));

        eventContext.setTime(time);

        EventNotifier notifier = new EventNotifier();
        InetAddress inetAddress = InetAddress.getLocalHost();
        notifier.setIP(inetAddress.getHostAddress());
        notifier.setName(inetAddress.getHostName());
        notifier.setPort(20302);

        eventContext.setNotifier(notifier);

        EventSource source = new EventSource();
        ServiceSourceType serviceSourceType = new ServiceSourceType();

        Entity sender = new Entity();
        sender.setName("toreadorSLA");
        sender.setIp("0.0.0.0");
        sender.setPort(10022);
        serviceSourceType.setSender(sender);

        Entity receiver = new Entity();
        receiver.setName("toreadorSLA");
        receiver.setIp("192.168.43.26");
        receiver.setPort(10022);
        serviceSourceType.setReceiver(receiver);

        source.setSwServiceLayerSource(serviceSourceType);

        eventContext.setSource(source);

        EventPayloadType eventPayload = new EventPayloadType();
        ArgumentList args = new ArgumentList();

        InteractionEventType interactionEvent = new InteractionEventType();
        interactionEvent.setOperationName(operationName);
        interactionEvent.setOperationID(generateRandomLong());

        /*
        If the operation name is "start" then the interaction event status should be a REQ-A whereas if the operation name is "end" the
        interaction event status should be RES-B. This is critical for the unification of rules from EVEREST
         */
        if("start".equals(operationName)){
            interactionEvent.setStatus("REQ-A");
        }else if("end".equals(operationName)){
            interactionEvent.setStatus("RES-B");
        }

        ArgumentType arg1 = new ArgumentType();
        SimpleArgument appIdArg = new SimpleArgument();
        appIdArg.setArgName("appId");
        appIdArg.setArgType("string");

        /*
        If the operation name is "start" then the direction of the parameters should be in (input) whereas if the operation name is "end" the
        direction of the parameters should be out (output). This is critical for the unification of rules from EVEREST
         */
        if("start".equals(operationName)){
            appIdArg.setDirection(DirectionType.IN.name());
        }else if("end".equals(operationName)){
            appIdArg.setDirection(DirectionType.OUT.name());
        }

        appIdArg.setValue(applicationId);
        arg1.setSimple(appIdArg);
        args.getArgument().add(arg1);

        ArgumentType arg2 = new ArgumentType();
        SimpleArgument appNameArg = new SimpleArgument();
        appNameArg.setArgName("appName");
        appNameArg.setArgType("string");

         /*
        If the operation name is "start" then the direction of the parameters should be in (input) whereas if the operation name is "end" the
        direction of the parameters should be out (output). This is critical for the unification of rules from EVEREST
         */
        if("start".equals(operationName)){
            appNameArg.setDirection(DirectionType.IN.name());
        }else if("end".equals(operationName)){
            appNameArg.setDirection(DirectionType.OUT.name());
        }
        appNameArg.setValue(applicationName);
        arg2.setSimple(appNameArg);
        args.getArgument().add(arg2);

        ArgumentType arg3 = new ArgumentType();
        SimpleArgument rddIdArg = new SimpleArgument();
        rddIdArg.setArgName("rddId");
        rddIdArg.setArgType("string");

        /*
        If the operation name is "start" then this in EC-Assertion terms refers to a REQ-A whereas if the operation name is "end" this refers to
        a RES-B. This is critical for the unification of rules from EVEREST
         */
        if("start".equals(operationName)){
            rddIdArg.setDirection(DirectionType.IN.name());
        }else if("end".equals(operationName)){
            rddIdArg.setDirection(DirectionType.OUT.name());
        }
        rddIdArg.setValue(String.valueOf(rdd.id()));
        arg3.setSimple(rddIdArg);
        args.getArgument().add(arg3);

        ArgumentType arg4 = new ArgumentType();
        SimpleArgument partitionIdArg = new SimpleArgument();
        partitionIdArg.setArgName("partId");
        partitionIdArg.setArgType("string");

        if("start".equals(operationName)){
            partitionIdArg.setDirection(DirectionType.IN.name());
        }else if("end".equals(operationName)){
            partitionIdArg.setDirection(DirectionType.OUT.name());
        }

        if(split != null){
            partitionIdArg.setValue(String.valueOf(split.index()));
        }else {
            partitionIdArg.setValue("NaN");
        }

        arg4.setSimple(partitionIdArg);
        args.getArgument().add(arg4);

        ArgumentType arg5 = new ArgumentType();
        SimpleArgument typeArg = new SimpleArgument();
        typeArg.setArgName("operationType");
        typeArg.setArgType("string");

         /*
        If the operation name is "start" then the direction of the parameters should be in (input) whereas if the operation name is "end" the
        direction of the parameters should be out (output). This is critical for the unification of rules from EVEREST
         */
        if("start".equals(operationName)){
            typeArg.setDirection(DirectionType.IN.name());
        }else if("end".equals(operationName)){
            typeArg.setDirection(DirectionType.OUT.name());
        }
        typeArg.setValue(type.name());
        arg5.setSimple(typeArg);
        args.getArgument().add(arg5);

        interactionEvent.setParameters(args);
        eventPayload.setInteractionEvent(interactionEvent);

        event.setEventID(eventID);
        event.setEventContext(eventContext);
        event.setEventPayload(eventPayload);

        JAXBContext context = JAXBContext.newInstance(EventInstance.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        m.marshal(event, sw);

        return sw.toString();

    }

    static public String createEvent(String style,
                                     OperationType type,
                                     String operationName,
                                     String applicationId,
                                     String applicationName,
                                     RDD rdd,
                                     Partition split) throws JAXBException, UnknownHostException, DatatypeConfigurationException {

        String event = null;

        if("TEXT".equalsIgnoreCase(style)){
            event =  String.format("%s(%s, %s, %s)", operationName, applicationId, applicationName, System.currentTimeMillis());
        }if("XML".equalsIgnoreCase(style)){
            event = MonitoringUtilities.createEventXML(type, operationName, applicationId, applicationName, rdd, split);
        }

        return event;
    }

    public static long generateRandomLong() {
        long leftLimit = 1L;
        long rightLimit = 100000L;

        return new RandomDataGenerator().nextLong(leftLimit, rightLimit);
    }
}
