package dev.mieser.tsa.quarkus;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * {@link QuarkusTestProfile} which sets the {@code tsa.keystore.path} to load a keystore from classpath.
 */
public class TsaTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("tsa.keystore.path", "classpath:keystore/ec.p12");
    }

}
