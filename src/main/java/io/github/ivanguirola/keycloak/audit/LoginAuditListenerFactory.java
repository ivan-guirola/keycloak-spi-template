package io.github.ivanguirola.keycloak.audit;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class LoginAuditListenerFactory implements EventListenerProviderFactory {

    public static final String ID = "login-audit";

    private static final String EVENTS = "events";
    private static final Logger LOG = Logger.getLogger(LoginAuditListener.class);

    private Set<EventType> types = defaultTypes();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(Config.Scope config) {
        types = parseTypes(config.get(EVENTS));
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new LoginAuditListener(types, LOG::info);
    }

    @Override
    public void close() {}

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(EVENTS)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("Comma-separated event types to write to the log.")
                .defaultValue("LOGIN,LOGIN_ERROR,LOGOUT")
                .add()
                .build();
    }

    Set<EventType> types() {
        return types;
    }

    static Set<EventType> parseTypes(String value) {
        if (value == null || value.isBlank()) {
            return defaultTypes();
        }
        Set<EventType> parsed = EnumSet.noneOf(EventType.class);
        for (String name : value.split(",")) {
            if (name.isBlank()) {
                continue;
            }
            try {
                parsed.add(EventType.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown event type: " + name.trim());
            }
        }
        return parsed;
    }

    private static Set<EventType> defaultTypes() {
        return EnumSet.of(EventType.LOGIN, EventType.LOGIN_ERROR, EventType.LOGOUT);
    }
}
