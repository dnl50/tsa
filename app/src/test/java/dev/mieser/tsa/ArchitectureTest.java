package dev.mieser.tsa;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class ArchitectureTest {

    private static final String ROOT_PACKAGE_NAME = "dev.mieser.tsa";

    @Test
    void implementationsAreOnlyAccessedInternallyAndFromConfigPackages() {
        JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT_PACKAGE_NAME);

        ArchRule rule = classes().that().resideInAPackage("..impl..")
            .should(onlyBeAccessedInternallyOrFromConfigPackage());

        rule.check(productionClasses);
    }

    private ArchCondition<JavaClass> onlyBeAccessedInternallyOrFromConfigPackage() {
        return new ArchCondition<>("should only be accessed internally or from config package") {
            @Override
            public void check(JavaClass checkedClass, ConditionEvents events) {
                JavaPackage implPackage = findPackageInHierarchy(checkedClass.getPackage(), "impl");
                JavaPackage configPackage = findPackageInHierarchy(checkedClass.getPackage(), "config");
                if (configPackage == null || implPackage == null) {
                    events.add(SimpleConditionEvent.violated(checkedClass,
                        String.format("Configuration or Implementation package was not found in package hierarchy of class '%s'",
                            checkedClass.getName())));
                    return;
                }

                checkedClass.getAccessesToSelf().forEach(access -> {
                    JavaClass accessor = access.getOriginOwner();
                    JavaPackage accessorPackage = accessor.getPackage();

                    boolean internalAccess = implPackage.equals(accessorPackage)
                        || implPackage.getSubpackagesInTree().contains(accessorPackage);
                    boolean accessFromConfigPackage = configPackage.equals(accessorPackage);

                    if (!internalAccess && !accessFromConfigPackage) {
                        events.add(SimpleConditionEvent.violated(checkedClass,
                            String.format("Class is accessed from from disallowed package: %s", access.getDescription())));
                    }
                });
            }

            private JavaPackage findPackageInHierarchy(JavaPackage startingPackage, String packageName) {
                JavaPackage currentPackage = startingPackage;
                while (currentPackage != null && !ROOT_PACKAGE_NAME.equals(currentPackage.getName())) {
                    if (currentPackage.containsPackage(packageName)) {
                        return currentPackage.getPackage(packageName);
                    }

                    currentPackage = currentPackage.getParent().orElse(null);
                }

                return null;
            }

        };
    }

}
