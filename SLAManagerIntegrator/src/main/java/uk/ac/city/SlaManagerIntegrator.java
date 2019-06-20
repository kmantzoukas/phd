package uk.ac.city;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

@SpringBootApplication
public class SlaManagerIntegrator implements CommandLineRunner {

    final static Logger log = Logger.getLogger(SlaManagerIntegrator.class);

    @Autowired
    ApplicationContext context;

    @Autowired
    SCDFConfigurationProperties scdf;

    @Autowired
    RestTemplateProperties rest;

    @Bean("restTemplate")
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean("authRestTemplate")
    RestTemplate getAuthRestTemplate() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(rest.getUsername(), rest.getPassword()));
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        clientHttpRequestFactory.setHttpClient(client);

        return new RestTemplate(clientHttpRequestFactory);
    }

    public static void main(String... args) {
        SpringApplication.run(SlaManagerIntegrator.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        /*
        Create and run a thread executor every 3 seconds to pick up any new workflows created
         */
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new CreateSLAProjectRunnable(), 0, 3, TimeUnit.SECONDS);

    }

    private class CreateSLAProjectRunnable implements Runnable {

        @Override
        public void run() {

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            log.info("Checking for new tasks @" + dateFormat.format(date));

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            /*
            Get all the tasks defined in Spring Cloud Dataflow
             */
            ResponseEntity<String> response = ((RestTemplate) context.getBean("restTemplate"))
                    .exchange(String.format("http://%s:%d%s", scdf.getHost(), scdf.getPort(), scdf.getTaskDefinitionUrl()), HttpMethod.GET, new HttpEntity<String>(headers), String.class);
            JSONObject obj = new JSONObject(response.getBody());
             /*
             Extract the JSON definitions from the JSON response from the Spring Cloud Dataflow server
             */
             JSONArray taskDefinitions = obj
                .getJSONObject("_embedded").getJSONArray("taskDefinitionResourceList");

             ResponseEntity<String> result =
                ((RestTemplate) context.getBean("authRestTemplate"))
                        .postForEntity(String.format("http://%s:%d%s", rest.getHost(), rest.getPort(), rest.getUrl()),
                            new HttpEntity<String>(taskDefinitions.toString(),headers), String.class);
        }
    }
}


