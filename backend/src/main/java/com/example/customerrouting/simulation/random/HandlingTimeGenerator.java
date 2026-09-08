package com.example.customerrouting.simulation.random;

import com.example.customerrouting.enquiry.*;
import com.example.customerrouting.simulation.*;
import org.springframework.stereotype.*;
import java.util.concurrent.*;

@Component
public class HandlingTimeGenerator {
    private final SimulationProperties p;

    public HandlingTimeGenerator(SimulationProperties p) {
        this.p = p;
    }

    public int seconds(EnquiryCategory c) {
        var r = p.getHandlingTimes().getOrDefault(c.name(),
                p.getHandlingTimes().getOrDefault("DEFAULT", new SimulationProperties.Range()));
        return ThreadLocalRandom.current().nextInt(r.getMinSeconds(), r.getMaxSeconds() + 1);
    }
}
