package com.example.customerrouting.simulation;

import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;
import java.util.*;

@Component
@ConfigurationProperties("simulation")
public class SimulationProperties {
    private boolean enabled = true;
    private Traffic traffic = new Traffic();
    private Map<String, Range> handlingTimes = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean x) {
        enabled = x;
    }

    public Traffic getTraffic() {
        return traffic;
    }

    public void setTraffic(Traffic t) {
        traffic = t;
    }

    public Map<String, Range> getHandlingTimes() {
        return handlingTimes;
    }

    public void setHandlingTimes(Map<String, Range> x) {
        handlingTimes = x;
    }

    public static class Traffic {
        private boolean enabled;
        private long minArrivalIntervalMs = 500, maxArrivalIntervalMs = 3000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean x) {
            enabled = x;
        }

        public long getMinArrivalIntervalMs() {
            return minArrivalIntervalMs;
        }

        public void setMinArrivalIntervalMs(long x) {
            minArrivalIntervalMs = x;
        }

        public long getMaxArrivalIntervalMs() {
            return maxArrivalIntervalMs;
        }

        public void setMaxArrivalIntervalMs(long x) {
            maxArrivalIntervalMs = x;
        }
    }

    public static class Range {
        private int minSeconds = 10, maxSeconds = 60;

        public int getMinSeconds() {
            return minSeconds;
        }

        public void setMinSeconds(int x) {
            minSeconds = x;
        }

        public int getMaxSeconds() {
            return maxSeconds;
        }

        public void setMaxSeconds(int x) {
            maxSeconds = x;
        }
    }
}
