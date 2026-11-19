package io.github.ivanguirola.keycloak.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

class LoginAuditListenerTest {

    private final List<String> lines = new ArrayList<>();
    private final LoginAuditListener listener =
            new LoginAuditListener(EnumSet.of(EventType.LOGIN, EventType.LOGIN_ERROR), lines::add);

    private static Event event(EventType type) {
        Event event = new Event();
        event.setType(type);
        event.setRealmName("demo");
        event.setClientId("account-console");
        event.setUserId("7f3c");
        event.setIpAddress("10.0.0.7");
        return event;
    }

    @Test
    void writesOneLineForASelectedEvent() {
        listener.onEvent(event(EventType.LOGIN));

        assertEquals(List.of("type=LOGIN realm=demo client=account-console user=7f3c ip=10.0.0.7"), lines);
    }

    @Test
    void appendsTheErrorWhenThereIsOne() {
        Event failed = event(EventType.LOGIN_ERROR);
        failed.setError("invalid_user_credentials");

        listener.onEvent(failed);

        assertEquals(
                "type=LOGIN_ERROR realm=demo client=account-console user=7f3c ip=10.0.0.7 error=invalid_user_credentials",
                lines.get(0));
    }

    @Test
    void usesADashForMissingValues() {
        Event anonymous = event(EventType.LOGIN_ERROR);
        anonymous.setUserId(null);
        anonymous.setClientId(null);

        listener.onEvent(anonymous);

        assertEquals("type=LOGIN_ERROR realm=demo client=- user=- ip=10.0.0.7", lines.get(0));
    }

    @Test
    void ignoresEventsThatWereNotSelected() {
        listener.onEvent(event(EventType.REGISTER));

        assertTrue(lines.isEmpty());
    }

    @Test
    void ignoresAdminEvents() {
        listener.onEvent(new AdminEvent(), false);

        assertTrue(lines.isEmpty());
    }

    @Test
    void keepsLineBreaksOutOfTheLog() {
        Event forged = event(EventType.LOGIN);
        forged.setClientId("app\ntype=LOGIN realm=master");

        listener.onEvent(forged);

        assertEquals(1, lines.size());
        assertEquals("type=LOGIN realm=demo client=app_type=LOGIN_realm=master user=7f3c ip=10.0.0.7", lines.get(0));
    }
}
