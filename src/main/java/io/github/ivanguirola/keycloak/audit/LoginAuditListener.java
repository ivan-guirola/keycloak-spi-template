package io.github.ivanguirola.keycloak.audit;

import java.util.Set;
import java.util.function.Consumer;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

public class LoginAuditListener implements EventListenerProvider {

    private final Set<EventType> types;
    private final Consumer<String> sink;

    LoginAuditListener(Set<EventType> types, Consumer<String> sink) {
        this.types = types;
        this.sink = sink;
    }

    @Override
    public void onEvent(Event event) {
        if (types.contains(event.getType())) {
            sink.accept(format(event));
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {}

    @Override
    public void close() {}

    static String format(Event event) {
        StringBuilder line = new StringBuilder()
                .append("type=").append(event.getType())
                .append(" realm=").append(clean(event.getRealmName()))
                .append(" client=").append(clean(event.getClientId()))
                .append(" user=").append(clean(event.getUserId()))
                .append(" ip=").append(clean(event.getIpAddress()));
        if (event.getError() != null) {
            line.append(" error=").append(clean(event.getError()));
        }
        return line.toString();
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? "-" : value.replaceAll("\\s", "_");
    }
}
