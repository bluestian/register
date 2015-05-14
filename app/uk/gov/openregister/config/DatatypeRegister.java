package uk.gov.openregister.config;

import uk.gov.openregister.model.Field;

import java.util.Arrays;
import java.util.List;

public class DatatypeRegister extends Register {

    public static final List<Field> FIELDS = Arrays.asList(new Field("datatype"), new Field("name"), new Field("text"));

    @Override
    public String friendlyName() {
        return "Datatype";
    }

    @Override
    public String name() {
        return "datatype";
    }

    @Override
    public List<Field> fields() {
        return FIELDS;
    }

    @Override
    public boolean isStarted() {
        return true;
    }
}
