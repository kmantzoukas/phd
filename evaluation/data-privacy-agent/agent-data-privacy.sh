#!/bin/bash
for i in {1..100}
do
spark-submit \
--master spark://10.207.1.102:7077 \
--class uk.ac.city.services.AnonymizeData \
--deploy-mode client \
--conf "spark.driver.extraJavaOptions=-javaagent:/home/abfc149/toreador-demo/captors/DataPrivacyEverestEventCaptors.jar=emitter=socket,host=10.207.1.103,port=10334,eventStype=NONE" \
--conf "spark.executor.extraJavaOptions=-javaagent:/home/abfc149/toreador-demo/captors/DataPrivacyEverestEventCaptors.jar=emitter=socket,host=10.207.1.103,port=10334,eventStype=NONE" \
/home/abfc149/anonymizedata_2.11-0.1-SNAPSHOT.jar
done
