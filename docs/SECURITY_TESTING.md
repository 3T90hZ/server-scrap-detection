# Security testing

## Device and account authorization boundary

Valid device headers establish a `ROLE_DEVICE` principal. The protected
operations currently implemented for that role are the camera ingestion
endpoints:

- `POST /api/detections`
- `POST /api/detections/frame`

The filter-chain fallback, `/api/account/**`, and `POST /api/auth/logout`
require an account role: `ADMIN`, `YARD_OWNER`, `STAFF`, or `CUSTOMER`. A valid
device credential therefore receives `403 Forbidden` when it is used on those
protected account endpoints. Public authentication endpoints such as login
remain public and do not grant extra access based on `ROLE_DEVICE`.

## Targeted authorization test

Run the HTTP authorization regression test without starting MySQL:

```powershell
mvn -Dtest=SecurityConfigTest test
```

`SecurityConfigTest` verifies that:

1. `ROLE_DEVICE` can submit a detection.
2. An account role cannot submit a detection as a device.
3. `ROLE_DEVICE` cannot access representative protected device and transaction
   endpoints that rely on the filter-chain fallback rule.
4. An account role such as `ROLE_STAFF` can still access a protected account
   endpoint and logout.
5. An anonymous request cannot access a protected account endpoint.
6. An anonymous request can still access the public login endpoint.

The test uses MockMvc endpoints and injected authenticated principals. It tests
the authorization rules themselves; it does not validate JWT parsing, device API
key hashing, persistence, or the production controllers.

## Full Backend test suite

The Spring context test uses the configured MySQL datasource. Start MySQL, use a
local or dedicated test database, and provide its connection settings only
through the environment:

```powershell
$env:SPRING_DATASOURCE_PASSWORD="<local-mysql-password>"
# Override these too when the test database is not the configured default:
# $env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/<test-database>"
# $env:SPRING_DATASOURCE_USERNAME="<test-user>"
mvn test
```

Do not point this test at a production database. With Hibernate schema update
enabled, application startup may synchronize table definitions.

Mockito may print a warning about dynamically attaching its inline mock-maker on
current JDKs. This warning does not fail the tests. A Maven Java-agent setup will
be required before upgrading to a JDK that disables dynamic agent loading.
