# Time Stamp Authority

This is a Java implementation of a Time Stamp Authority using the Time-Stamp Protocol (_TSP_) as defined
in [RFC 3161](https://tools.ietf.org/html/rfc3161) and
[RFC 5816](https://tools.ietf.org/html/rfc5816). It uses Bouncy Castle, Spring Boot, Spring Data JPA and Thymeleaf under
the hood.

## Features

### Time Stamping

The main purpose of this application is to sign TSP requests using
the [HTTP Protocol](https://datatracker.ietf.org/doc/html/rfc3161.html#section-3.4). The application therefore offers an
HTTP endpoint at the root path which accepts `POST` requests with the content type `application/timestamp-query`. The
ASN.1 DER-encoded Time-Stamp Request must be supplied in the request body.

### Web UI

The application provides a server-side rendered web interface implemented with [Thymeleaf](https://www.thymeleaf.org/).
It is fully self-contained, so all resources (CSS, JavaScript etc.) are provided by the application. HTTP `GET` requests
sent to the root path are automatically redirected to the history page.

#### History

A sortable overview table of all issued Time Stamp Protocol responses utilizing pagination:

![Overview Page](https://raw.githubusercontent.com/dnl50/tsa-server/develop/docs/images/overview-page.png "Overview Page")

#### Time Stamp Response Analysis

You can enter a Base64 encoded ASN.1 DER response of an RFC 3161 / RFC 5816 Time Stamp Protocol Response to see what was
signed and who signed it:

![Validation Page](https://raw.githubusercontent.com/dnl50/tsa-server/develop/docs/images/validation-page.png "Validation Page")

## Configuration

All Parameters mentioned below can be configured in variety of ways. Please refer to the
[Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/2.7.0/reference/html/features.html#features.external-config)
for more information.

### Time Stamp Protocol

| Parameter Name                 | Mandatory | Default Value        | Description                                                                                                                                                                                                               |
|--------------------------------|-----------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tsa.ess-cert-id-algorithm`    | No        | SHA256               | The hash algorithm which is used to calculate the TSA's certificate identifier (ESSCertIDv2).                                                                                                                             |
| `tsa.signing-digest-algorithm` | No        | SHA256               | The hash algorithm which is used to calculate the TSP requests digest, which will be signed by the TSA.                                                                                                                   |
| `tsa.accepted-hash-algorithms` | No        | SHA1, SHA256, SHA512 | Comma-separated list of hash algorithms which are accepted by the Time Stamp Authority.                                                                                                                                   |
| `tsa.policy-oid`               | No        | 1.2                  | The OID of the policy under which the TSP responses are produced.                                                                                                                                                         |
| `tsa.certificate.path`         | Yes       |                      | The path to load the PKCS#12 archive containing the certificate and private key used to sign TSP requests. Prefixing  the path with `classpath:` will result in the PKCS#12 archive from being loaded from the classpath. |
| `tsa.certificate.password`     | No        |                      | The password of the PKCS#12 archive.                                                                                                                                                                                      |
| `tsa.server.port`              | No        | 318                  | The TCP port under which Time Stamp Protocol Query Requests will be answered.                                                                                                                                             |

### Web

| Parameter Name                  | Mandatory | Default Value | Description                                                                                                                                           |
|---------------------------------|-----------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `server.port`                   | No        | 8080          | The TCP port under which HTTP(S) frontend requests will be answered.                                                                                  |
| `tsa.http.port`                 | No        | 80            | The TCP port for which requests will be redirected to HTTPS. Only active when the HTTPS is activated by setting the `server.ssl.key-store` parameter. |
| `server.ssl.key-store`          | No        |               | The absolut path of the keystore containing the TLS certificate to use for HTTPS. Accepts Java Key Stores (`jks`) and PKCS#12 (`p12`) files.          |
| `server.ssl.key-store-password` | No        |               | The password of the aforementioned keystore.                                                                                                          |
| `server.ssl.key-store-type`     | No        |               | The type of the keystore to use (`jks` or `pkcs12`).                                                                                                  |

## Issuing a signing certificate

The signing certificate used by the Time Stamp Authority must be an RSA, DSA or EC certificate with
an [Extended Key Usage](https://datatracker.ietf.org/doc/html/rfc5280#section-4.2.1.12) extension marked as _critical_.
The only `KeyPurposeId` present in the sequence must be `id-kp-timeStamping` (OID `1.3.6.1.5.5.7.3.8`).

The following parameter can be added to the OpenSSL x509 utility to add the required critical _Extended Key Usage_
extension when creating a self-signed certificate:

```bash
openssl x509 ... -addext extendedKeyUsage=critical,timeStamping
```

## Running in Docker

Docker images are automatically published to [Docker Hub](https://hub.docker.com/r/dnl50/tsa-server). The Embedded
Database writes its data to the `/application/db` directory. Mounting a filesystem directory or a named volume under
that path will make the DB data persistent.

## Configure Logging

By default, all log messages will be printed to STDOUT. If you want to configure the logging library used ([Logback](https://logback.qos.ch/)), please refer to the relevant section in the [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/2.6.3/reference/html/features.html#features.logging)

## Development

### Running in development mode

In normal operation, no signing certificate is configured since you probably want to use your own/your organizations
certificate and not a self-signed certificate I issued to use in integration tests. Configuring a file system path to a
valid certificate for development is error-prone though. That's what the development mode is for. It can be enabled by
activating the `dev` Spring profile (e.g. by setting the `spring.profiles.active` JVM System Property to `dev`). In
addition, this also has the following effects:

* application data is written into an in-memory Database which will be scrapped on application shutdown
* enables the H2 Database Console which can be accessed
  under [localhost:8080/h2-console](http://localhost:8080/h2-console)
* uses a self-singed RSA certificate for signing TSP requests

### Using the code formatter

The source code is formatted using the Eclipse Code Formatter. The formatter config file is located
unter `/eclipse-formatter.xml`. A custom import order configuration file os located under `/spotless.importorder`. The
code can also be formatted using the [Spotless Gradle Plugin](https://github.com/diffplug/spotless). Just execute
the `spotlessApply` Gradle Task and you are good to go! 
