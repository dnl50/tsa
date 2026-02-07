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

There are two Docker Image variants: _Native_ and _JVM_.
Native Images (e.g. `dnl50/tsa-server:3.1.0`) are only available for `x86-64`. The JVM variant
(e.g. `dnl50/tsa-server:3.1.0-jvm`) is available for `x86-64` and `arm64`.

## Features

### Time Stamping

The main purpose of this application is to sign TSP requests using
the [HTTP Protocol](https://datatracker.ietf.org/doc/html/rfc3161.html#section-3.4). The application therefore offers an
HTTP endpoint under `/sign` which accepts `POST` requests with the content type `application/timestamp-query`. The
ASN.1 DER-encoded Timestamp Request must be supplied in the request body.

The following OpenSSL commands can be used to send a timestamp request for an existing file:

```bash
# create a timestamp request
openssl ts -query -data /path/to/file -sha512 -cert -out request.tsq

# send the request using cURL
curl -X POST --data-binary @request.tsq --header "Content-Type: application/timestamp-query" http://localhost:8080/sign -o response.tsr
````

### Web UI

⚠️ The Web UI ist yet to be reimplemented after the migration to Quarkus ⚠️

### REST API

The available REST endpoints are documented in a OpenAPI specification which can be downloaded from
the [release page](https://github.com/dnl50/tsa/releases).

### WebSocket Endpoint

The application exposes an WebSocket endpoint under `/history/responses`. The JSON representation of every TSP
response will be broadcast there.

## Configuration

All Parameters mentioned below can be configured in variety of ways. Please refer to
the [Quarkus Documentation](https://quarkus.io/guides/config-reference#configuration-sources) for more information.

| Parameter Name                 | Mandatory | Default Value | Description                                                                                                                                                                                                                                         |
|--------------------------------|-----------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tsa.ess-cert-id-algorithm`    | No        | SHA256        | The hash algorithm which is used to calculate the TSA's certificate identifier (ESSCertIDv2).                                                                                                                                                       |
| `tsa.signing-digest-algorithm` | No        | SHA256        | The hash algorithm which is used to calculate the TSP requests digest, which will be signed by the TSA.                                                                                                                                             |
| `tsa.accepted-hash-algorithms` | No        | SHA256,SHA512 | Comma-separated list of hash algorithm names/OIDs which are accepted by the Time Stamp Authority.                                                                                                                                                   |
| `tsa.policy-oid`               | No        | 1.2           | The OID of the policy under which the TSP responses are produced.                                                                                                                                                                                   |
| `tsa.keystore.path`            | Yes       |               | The path of the PKCS#12 archive containing the certificate and private key used to sign TSP requests. Prefixing the path with `classpath:` will result in the PKCS#12 archive from being loaded from the classpath (not supported in native image). |
| `tsa.keystore.password`        | No        |               | The password of the PKCS#12 archive.                                                                                                                                                                                                                |
| `tsa.include-tsa-name`         | No        | true          | Specifies whether the [`tsa` Field in the `TSTInfo`](https://datatracker.ietf.org/doc/html/rfc3161#autoid-8) should include the subject of the certificate.                                                                                         |

### Logging

By default, all log messages will be printed to STDOUT. Please refer to
the [Quarkus Documentation](https://quarkus.io/guides/logging) for further information on how to configure the log
output.

## Issuing a signing certificate

The signing certificate used by the Time Stamp Authority must be an RSA, DSA or EC certificate with
an [Extended Key Usage](https://datatracker.ietf.org/doc/html/rfc5280#section-4.2.1.12) extension marked as _critical_.
The only `KeyPurposeId` present in the sequence must be `id-kp-timeStamping` (OID `1.3.6.1.5.5.7.3.8`).

### Issuing a CA and TSA certificate with OpenSSL

> You should use a certificate issued by a trusted third party for production use

To issue a signing certificate using a custom CA, you can use the following commands:

First, create a file named `tsa-x509-extensions.cnf` with the following content:

```
[v3_ca]
basicConstraints = CA:TRUE
keyUsage = digitalSignature, keyCertSign

[usr_timestamping]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, nonRepudiation
extendedKeyUsage = critical, timeStamping
```

This file contains the extension profiles which are used later.

Then create a new private key for the CA:

```bash
openssl ecparam -genkey -name secp384r1 -out ca.privkey
```

After that, create a CSR (_Certificate Signing Request_) for the CA certificate

```bash
openssl req -new -key ca.privkey -out cacertreq.pem
```

and sign it with CA's private key created before

```bash
openssl x509 -req -in cacertreq.pem -extfile tsa-x509-extensions.cnf -extensions v3_ca -key ca.privkey -out cacert.pem
```

After that you can create a new private key which will be used by the TSA to sign the timestamp requests:

```bash
openssl ecparam -genkey -name secp384r1 -out tsa.privkey
```

Then create a CSR for it

```bash
openssl req -new -key tsa.privkey -out tsacertreq.pem
```

and issue a certificate using the CA certificate and private key created before:

```bash
openssl x509 -req -in tsacertreq.pem -extfile tsa-x509-extensions.cnf -extensions usr_timestamping -CA cacert.pem -CAkey ca.privkey -CAcreateserial -out tsacert.pem
```

The TSA certificate and private key can then be put into a PKCS#12 keystore which can be used by the application:

```bash
openssl pkcs12 -export -CAfile cacert.pem -chain -in tsacert.pem -inkey tsa.privkey -out tsa-keystore.p12 
```

## Development

### Building an OCI image with a GraalVM native executable

Due to restrictions of the [Quarkus Gradle Plugin](https://github.com/quarkusio/quarkus/discussions/40679)
you can either build an OCI image with a GraalVM native executable or a JVM based OCI image.

The name of the `testNative` Gradle task is therefore extremely misleading, because it can either use the JVM based
image or the GraalVM native executable image.

By default, a JVM based OCI image is built and used in the `testNative` task. To build a GraalVM native executable
image, set the `nativeImage` Gradle property to `true` when executing Gradle tasks:

```bash
# build the native image without executing end-to-end-tests (just execute the regular JUnit tests)
./gradlew build -PnativeImage=true

# build the native image and execute the end-to-end-tests (without the regular JUnit tests)
./gradlew testNative -PnativeImage=true
```

This Gradle property will properly configure the `quarkus.package.jar.enabled`, `quarkus.native.enabled` and
`quarkus.container-image.tag` configuration parameters.

### Running in development mode

In normal operation, no signing certificate is configured by default since you probably want to use your own/your
organizations key pair and not a self-signed key pair I issued to use in integration tests. Configuring a file system
path to a valid certificate for development is error-prone though. That's what the development mode is for. It is
automatically enabled when running Quarkus using the `quarkusDev` Gradle Task. The dev mode has the following effects:

* application data is written into an in-memory Database which will be scrapped on application shutdown
* uses a self-signed EC certificate for signing TSP requests

### Using the code formatter

The source code is formatted using the Eclipse Code Formatter. The formatter config file is located
under `/eclipse-formatter.xml`. A custom import order configuration file os located under `/spotless.importorder`. The
code can also be formatted using the [Spotless Gradle Plugin](https://github.com/diffplug/spotless). Just execute
the `spotlessApply` Gradle Task and you are good to go!

## License

This project is licensed under the terms of the MIT license.