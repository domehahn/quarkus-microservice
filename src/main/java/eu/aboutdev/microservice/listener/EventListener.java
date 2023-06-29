package eu.aboutdev.microservice.listener;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventListener {

    private static Logger LOG = LoggerFactory.getLogger(EventListener.class);

    @Incoming("q.fail-queue")
    public void receive(final String eventId) {
        LOG.info("Received Event with Id: {}", eventId);
    }
}
