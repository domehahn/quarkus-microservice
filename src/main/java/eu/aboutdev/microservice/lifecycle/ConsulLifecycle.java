package eu.aboutdev.microservice.lifecycle;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.health.ServiceHealth;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@ApplicationScoped
public class ConsulLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulLifecycle.class);
    private String instanceId;

    @Inject
    Instance<Consul> consulClient;
    @ConfigProperty(name = "quarkus.application.name")
    String appName;
    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    void onStart(@Observes StartupEvent ev) {
        if (consulClient.isResolvable()) {
            ScheduledExecutorService executorService = Executors
                    .newSingleThreadScheduledExecutor();
            executorService.schedule(() -> {
                HealthClient healthClient = consulClient.get().healthClient();
                List<ServiceHealth> instances = healthClient
                        .getHealthyServiceInstances(appName).getResponse();
                instanceId = appName + "-" + instances.size();
                int port = Integer.parseInt(System.getProperty("quarkus.http.port"));
                ImmutableRegistration registration = ImmutableRegistration.builder()
                        .id(instanceId)
                        .name(appName)
                        .address("localhost")
                        .port(port)
                        .putMeta("version", appVersion)
                        .build();
                consulClient.get().agentClient().register(registration);
                LOGGER.info("Instance registered: id={}, address=localhost:{}",
                        registration.getId(), port);
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (consulClient.isResolvable()) {
            consulClient.get().agentClient().deregister(instanceId);
            LOGGER.info("Instance de-registered: id={}", instanceId);
        }
    }
}
