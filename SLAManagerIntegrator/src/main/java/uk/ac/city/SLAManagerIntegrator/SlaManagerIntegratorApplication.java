package uk.ac.city.SLAManagerIntegrator;

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

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SlaManagerIntegratorApplication implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication.run(SlaManagerIntegratorApplication.class, args);
    }

    @Autowired
    ApplicationContext context;

    @Bean("restTemplate")
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean("authRestTemplate")
    RestTemplate getAuthRestTemplate() {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("ibasdekis", "t0reador"));
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();

        clientHttpRequestFactory.setHttpClient(client);

        return new RestTemplate(clientHttpRequestFactory);
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
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

        /*
        Get all the tasks defined in Spring Cloud Dataflow
         */
            ResponseEntity<String> response = ((RestTemplate) context.getBean("restTemplate"))
                    .exchange("http://10.207.1.102:9393/tasks/definitions", HttpMethod.GET, new HttpEntity<String>(headers), String.class);
            JSONObject obj = new JSONObject(response.getBody());
         /*
         Extract the JSON definitions from the JSON response from the Spring Cloud Dataflow server
         */
         JSONArray taskDefinitions = obj
            .getJSONObject("_embedded").getJSONArray("taskDefinitionResourceList");

         ResponseEntity<String> result =
            ((RestTemplate) context.getBean("authRestTemplate")).postForEntity("http://10.207.1.102:8180/toreador/rest/api/users/3/projects/scdf",
                    new HttpEntity<String>(taskDefinitions.toString(),headers), String.class);
        }
    }
}


