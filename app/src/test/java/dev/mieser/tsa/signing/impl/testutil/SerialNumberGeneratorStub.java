package dev.mieser.tsa.signing.impl.testutil;

import java.util.Random;
import java.util.function.Supplier;

import lombok.Setter;

import dev.mieser.tsa.signing.impl.serial.SerialNumberGenerator;

@Setter
public class SerialNumberGeneratorStub implements SerialNumberGenerator {

    private Supplier<Long> serialNumberSupplier = () -> new Random().nextLong();

    @Override
    public long generateSerialNumber() {
        return serialNumberSupplier.get();
    }

}
