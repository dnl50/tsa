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

_TODO_

#### Time Stamp Response Analysis

_TODO_

## Configuration

| Parameter Name               | Mandatory | Default Value        | Description                                                                                                                                                                                                               |
|------------------------------|-----------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| tsa.ess-cert-id-algorithm    | No        | SHA256               | The hash algorithm which is used to calculate the TSA's certificate identifier (ESSCertIDv2).                                                                                                                             |
| tsa.signing-digest-algorithm | No        | SHA256               | The hash algorithm which is used to calculate the TSP requests digest, which will be signed by the TSA.                                                                                                                   |
| tsa.accepted-hash-algorithms | No        | SHA1, SHA256, SHA512 | Comma-separated list of hash algorithms which are accepted by the Time Stamp Authority.                                                                                                                                   |
| tsa.policy-oid               | No        | 1.2                  | The OID of the policy under which the TSP responses are produced.                                                                                                                                                         |
| tsa.certificate.path         | Yes       |                      | The path to load the PKCS#12 archive containing the certificate and private key used to sign TSP requests. Prefixing  the path with "classpath:" will result in the PKCS#12 archive from being loaded from the classpath. |
| tsa.certificate.password     | No        |                      | The password of the PKCS#12 archive.                                                                                                                                                                                      |
| tsa.server.port              | No        | 318                  | The TCP port under which Time Stamp Protocol Query Requests will be answered.                                                                                                                                             |
| server.port                  | No        | 8080                 | The TCP port under which HTML requests will be answered.                                                                                                                                                                  |

## Issuing a signing certificate

The signing certificate used by the Time Stamp Authority must be an RSA, DSA or EC certificate with
an [Extended Key Usage](https://datatracker.ietf.org/doc/html/rfc5280#section-4.2.1.12) extension marked as _critical_.
The only `KeyPurposeId` present in the sequence must be `id-kp-timeStamping` (OID `1.3.6.1.5.5.7.3.8`).

The following parameter can be added to the OpenSSL x509 utility to add the required critical _Extended Key Usage_
extension when creating a self-signed certificate:

```bash
openssl x509 ... -addext extendedKeyUsage=critical,timeStamping
```
