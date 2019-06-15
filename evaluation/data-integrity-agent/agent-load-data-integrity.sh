#!/bin/bash
for i in {1..1000}
do
spark-submit \
--master spark://10.207.1.102:7077 \
--class uk.ac.city.services.ComputeAverage \
--deploy-mode client \
--conf "spark.driver.extraJavaOptions=-javaagent:/home/abfc149/toreador-demo/captors/DataIntegrityEverestEventCaptors.jar=emitter=socket,host=10.207.1.103,port=10334,eventStype=NONE" \
--conf "spark.executor.extraJavaOptions=-javaagent:/home/abfc149/toreador-demo/captors/DataIntegrityEverestEventCaptors.jar=emitter=socket,host=10.207.1.103,port=10334,eventStype=NONE" \
/home/abfc149/computeaverage_2.11-0.1-SNAPSHOT.jar
done
