package dev.mieser.tsa.quarkus;

import java.util.List;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * {@link QuarkusTestProfile} which adds the {@link SigningCertificateResourceLifecycleManager} as a test resource
 * manager.
 */
public class TsaTestProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(SigningCertificateResourceLifecycleManager.class));
    }

}
