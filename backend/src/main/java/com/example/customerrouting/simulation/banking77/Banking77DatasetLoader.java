package com.example.customerrouting.simulation.banking77;

import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class Banking77DatasetLoader {
    private List<Banking77Sample> samples = List.of();

    @PostConstruct
    void load() {
        List<Banking77Sample> loaded = new ArrayList<>();
        for (String file : List.of("sample-data/banking77/train.csv", "sample-data/banking77/test.csv")) {
            try (InputStream in = new ClassPathResource(file).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() >= 2) {
                        loaded.add(new Banking77Sample(fields.get(0), fields.get(1)));
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("BANKING77 local data missing", e);
            }
        }
        if (loaded.isEmpty()) {
            throw new IllegalStateException("BANKING77 local data contains no samples");
        }
        samples = List.copyOf(loaded);
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    @Cacheable("banking77Samples")
    public List<Banking77Sample> samples() {
        return samples;
    }

    public Banking77Sample random() {
        return samples.get(ThreadLocalRandom.current().nextInt(samples.size()));
    }
}
