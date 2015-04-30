package uk.gov.openregister.validation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.openregister.domain.Record;

import java.util.*;
import java.util.stream.Collectors;

public class Validator {

    List<String> keys;

    public Validator(List<String> keys) {
        this.keys = keys;
    }

    public ValidationResult validate(Record record) {

        List<String> invalidKeys = checkForInvalidKeys(record);
        List<String> missingKeys = checkForMissingKeys(record);

        return new ValidationResult(invalidKeys, missingKeys);
    }

    private List<String> checkForInvalidKeys(Record record) {
        ArrayList<String> invalidKeys = new ArrayList<>();

        record.getEntry().fieldNames().forEachRemaining(key -> {
            if (!keys.contains(key)) invalidKeys.add(key);
        });

        return invalidKeys;
    }


    private List<String> checkForMissingKeys(Record record) {

        JsonNode entry = record.getEntry();
        return this.keys.stream()
                .filter(k -> !entry.has(k) || (!entry.get(k).isArray() && entry.get(k).asText().isEmpty()))
                .collect(Collectors.toList());

    }

}

