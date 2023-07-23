# Time Stamp Authority

This is a Java implementation of a Time Stamp Authority using the Time-Stamp Protocol (_TSP_) as defined
in [RFC 3161](https://tools.ietf.org/html/rfc3161) and [RFC 5816](https://tools.ietf.org/html/rfc5816).
It uses [Bouncy Castle](https://www.bouncycastle.org/java.html) and [Quarkus](https://quarkus.io) under the hood.

The application utilizes GraalVM's Native Image technology, which leads to a very small memory footprint of about
10 MB and a near instant startup in about 50 milliseconds.

## Docker Images

Docker images are automatically published to [Docker Hub](https://hub.docker.com/r/dnl50/tsa-server).

By default, the embedded H2 database writes its data to the `/work/data/tsa.mv.db` file. Mounting a directory or a named
volume to the `/work/data` will make the DB data persistent.

The keystore containing the certificate and private key used to sign the requests with is loaded
from `/work/keystore.p12` by default.

## Features

### Time Stamping

The main purpose of this application is to sign TSP requests using
the [HTTP Protocol](https://datatracker.ietf.org/doc/html/rfc3161.html#section-3.4). The application therefore offers an
HTTP endpoint under `/tsa/sign` which accepts `POST` requests with the content type `application/timestamp-query`. The
ASN.1 DER-encoded Time-Stamp Request must be supplied in the request body.

### Web UI

⚠️ The Web UI ist yet to be reimplemented after the migration to Quarkus ⚠️

### REST API

The available REST Endpoints are documented in a OpenAPI specification which can be explored using a Swagger UI which is
available under `/q/swagger-ui`.

## Configuration

All Parameters mentioned below can be configured in variety of ways. Please refer to
the [Quarkus Documentation](https://quarkus.io/guides/config-reference#configuration-sources) for more information.

| Parameter Name                 | Mandatory | Default Value  | Description                                                                                                                                                                                                                                             |
|--------------------------------|-----------|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tsa.ess-cert-id-algorithm`    | No        | SHA256         | The hash algorithm which is used to calculate the TSA's certificate identifier (ESSCertIDv2).                                                                                                                                                           |
| `tsa.signing-digest-algorithm` | No        | SHA256         | The hash algorithm which is used to calculate the TSP requests digest, which will be signed by the TSA.                                                                                                                                                 |
| `tsa.accepted-hash-algorithms` | No        | SHA256, SHA512 | Comma-separated list of hash algorithms OIDs which are accepted by the Time Stamp Authority.                                                                                                                                                            |
| `tsa.policy-oid`               | No        | 1.2            | The OID of the policy under which the TSP responses are produced.                                                                                                                                                                                       |
| `tsa.keystore.path`            | Yes       |                | The URL to load the PKCS#12 archive containing the certificate and private key used to sign TSP requests. Prefixing the path with `classpath:` will result in the PKCS#12 archive from being loaded from the classpath (not supported in native image). |
| `tsa.keystore.password`        | No        |                | The password of the PKCS#12 archive.                                                                                                                                                                                                                    |

### Logging

By default, all log messages will be printed to STDOUT. Please refer to
the [Quarkus Documentation](https://quarkus.io/guides/logging) for further information on how to configure the log
output.

## Issuing a signing certificate

The signing certificate used by the Time Stamp Authority must be an RSA, DSA or EC certificate with
an [Extended Key Usage](https://datatracker.ietf.org/doc/html/rfc5280#section-4.2.1.12) extension marked as _critical_.
The only `KeyPurposeId` present in the sequence must be `id-kp-timeStamping` (OID `1.3.6.1.5.5.7.3.8`).

The following parameter can be added to the OpenSSL x509 utility to add the required critical _Extended Key Usage_
extension when creating a self-signed certificate:

```bash
openssl x509 ... -addext extendedKeyUsage=critical,timeStamping
```

## Development

### Running in development mode

In normal operation, no signing certificate is configured by default since you probably want to use your own/your
organizations key pair and not a self-signed key pair I issued to use in integration tests. Configuring a file system
path to a valid certificate for development is error-prone though. That's what the development mode is for. It is
automatically enabled when running Quarkus using the `quarkusDev` Gradle Task. The dev mode has the following effects:

* application data is written into an in-memory Database which will be scrapped on application shutdown
* uses a self-singed EC certificate for signing TSP requests

### Using the code formatter

The source code is formatted using the Eclipse Code Formatter. The formatter config file is located
under `/eclipse-formatter.xml`. A custom import order configuration file os located under `/spotless.importorder`. The
code can also be formatted using the [Spotless Gradle Plugin](https://github.com/diffplug/spotless). Just execute
the `spotlessApply` Gradle Task and you are good to go! 
