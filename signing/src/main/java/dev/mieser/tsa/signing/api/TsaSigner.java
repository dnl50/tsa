package dev.mieser.tsa.signing.api;

import java.io.InputStream;

public interface TsaSigner {

    byte[] signRequest(InputStream tspRequest)

}
