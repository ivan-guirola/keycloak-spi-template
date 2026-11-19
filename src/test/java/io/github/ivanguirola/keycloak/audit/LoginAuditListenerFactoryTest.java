package io.github.ivanguirola.keycloak.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.ServiceLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;

class LoginAuditListenerFactoryTest {

    private static final String PREFIX = "login-audit.test.";

    private final LoginAuditListenerFactory factory = new LoginAuditListenerFactory();

    @AfterEach
    void clearConfig() {
        System.clearProperty(PREFIX + "events");
    }

    @Test
    void hasAStableId() {
        assertEquals("login-audit", factory.getId());
    }

    @Test
    void isRegisteredAsAKeycloakProvider() {
        boolean found = ServiceLoader.load(EventListenerProviderFactory.class).stream()
                .anyMatch(provider -> provider.type() == LoginAuditListenerFactory.class);

        assertTrue(found, "missing entry in META-INF/services");
    }

    @Test
    void auditsSignInsByDefault() {
        EnumSet<EventType> expected = EnumSet.of(EventType.LOGIN, EventType.LOGIN_ERROR, EventType.LOGOUT);

        assertEquals(expected, LoginAuditListenerFactory.parseTypes(null));
        assertEquals(expected, LoginAuditListenerFactory.parseTypes("  "));
    }

    @Test
    void readsACommaSeparatedListOfEventTypes() {
        assertEquals(
                EnumSet.of(EventType.REGISTER, EventType.UPDATE_PASSWORD),
                LoginAuditListenerFactory.parseTypes(" register, UPDATE_PASSWORD ,"));
    }

    @Test
    void rejectsAnUnknownEventTypeByName() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> LoginAuditListenerFactory.parseTypes("LOGIN,NOT_AN_EVENT"));

        assertTrue(error.getMessage().contains("NOT_AN_EVENT"));
    }

    @Test
    void takesTheEventTypesFromTheProviderConfig() {
        System.setProperty(PREFIX + "events", "REGISTER");

        factory.init(new Config.SystemPropertiesScope(PREFIX));

        assertEquals(EnumSet.of(EventType.REGISTER), factory.types());
    }

    @Test
    void createsAListener() {
        factory.init(new Config.SystemPropertiesScope(PREFIX));

        assertInstanceOf(LoginAuditListener.class, factory.create(null));
    }

    @Test
    void documentsItsConfigOption() {
        assertEquals("events", factory.getConfigMetadata().get(0).getName());
    }
}
