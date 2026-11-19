# Keycloak SPI template

[![CI](https://github.com/ivan-guirola/keycloak-spi-template/actions/workflows/ci.yml/badge.svg)](https://github.com/ivan-guirola/keycloak-spi-template/actions/workflows/ci.yml)

A small, working Keycloak extension to start from: one provider, its factory, the service registration, unit tests and a CI build. Built against Keycloak 26.6 and Java 17.

The example is an event listener, `login-audit`, that writes one line per sign-in event:

```
type=LOGIN_ERROR realm=master client=admin-cli user=8b96c5ec-… ip=172.17.0.1 error=invalid_user_credentials
```

## Build

```
mvn verify
```

The JAR is written to `target/keycloak-spi-template.jar`.

## Run it

```
mvn package
docker compose up
```

Keycloak starts on http://localhost:8080 (admin / admin) with the JAR mounted in `/opt/keycloak/providers`. In the admin console, open **Realm settings → Events → Event listeners**, add `login-audit` and sign in: the lines appear in the container log.

On your own server, copy the JAR to `providers/` and run `kc.sh build`.

## Configuration

| Option | Default | Description |
| --- | --- | --- |
| `--spi-events-listener--login-audit--events` | `LOGIN,LOGIN_ERROR,LOGOUT` | Comma-separated [event types](https://www.keycloak.org/docs-api/latest/javadocs/org/keycloak/events/EventType.html) to log. An unknown type is rejected at startup. |

## Make it yours

1. Rename the package `io.github.ivanguirola.keycloak.audit` and the `groupId` and `artifactId` in `pom.xml`.
2. Replace the listener and its factory with your provider. Keep the factory `ID` stable: it is what administrators type in the console and on the command line.
3. Rename the file in `src/main/resources/META-INF/services` to the factory interface of your SPI and put your factory class in it. `LoginAuditListenerFactoryTest` has a test that fails if the registration is missing.

## What is in the box

- `LoginAuditListener`: the provider. It receives its dependencies in the constructor, so the tests run without a server.
- `LoginAuditListenerFactory`: reads the configuration once at startup, validates it and documents it through `getConfigMetadata()`.
- Tests with JUnit 5 and no mocks.
- A GitHub Actions workflow that runs `mvn verify` on every push and pull request.

## Notes

- Event listeners are an internal Keycloak SPI, so the server logs a `KC-SERVICES0047` warning when it loads this one. It is expected.
- Values are written with whitespace replaced, so a crafted client ID cannot inject extra lines in the log.

## Disclaimer

This project was created for educational purposes or to support my day-to-day work.

## License

MIT
