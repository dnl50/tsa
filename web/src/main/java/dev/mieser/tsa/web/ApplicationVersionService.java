package dev.mieser.tsa.web;

import org.springframework.stereotype.Service;

/**
 * Service to retrieve the current version of the application.
 */
@Service("applicationVersionService")
public class ApplicationVersionService {

    /**
     * @return The current application version.
     */
    public String getApplicationVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

}
