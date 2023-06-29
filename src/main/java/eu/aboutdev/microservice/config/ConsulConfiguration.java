package eu.aboutdev.microservice.config;

import com.orbitz.consul.Consul;
import io.quarkus.arc.properties.IfBuildProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ConsulConfiguration {

    @Produces
    @IfBuildProperty(name = "quarkus.consul-discovery.enabled", stringValue = "true")
    Consul consulClient = Consul.builder().build();
}
