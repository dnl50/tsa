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

_TODO_

## Issuing a signing certificate

The signing certificate used by the Time Stamp Authority must be an RSA, DSA or EC certificate with
an [Extended Key Usage](https://datatracker.ietf.org/doc/html/rfc5280#section-4.2.1.12) extension marked as _critical_.
The only `KeyPurposeId` present in the sequence must be `id-kp-timeStamping` (OID `1.3.6.1.5.5.7.3.8`).

The following parameter can be added to the OpenSSL x509 utility to add the required critical _Extended Key Usage_
extension:

```bash
openssl x509 ... -extensions extendedKeyUsage=critical,timeStamping
```