package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import controllers.App;
import play.libs.F;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.openregister.domain.Record;
import uk.gov.openregister.domain.RecordVersionInfo;
import uk.gov.openregister.store.DatabaseException;
import uk.gov.openregister.store.Store;
import uk.gov.openregister.validation.ValidationError;
import uk.gov.openregister.validation.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static controllers.api.Representations.representationFor;
import static java.util.Collections.singletonList;

public class Rest extends Controller {

    public static final String REPRESENTATION_QUERY_PARAM = "_representation";

    private final Store store;
    private final List<String> fieldNames;
    private final String registerName;

    public Rest() {
        store = App.instance.register.store();
        fieldNames = App.instance.register.fieldNames();
        registerName = App.instance.register.name();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result create() throws JsonProcessingException {
        Record r = new Record(request().body().asJson());

        List<ValidationError> validationErrors = new Validator(singletonList(registerName), fieldNames).validate(r);

        if (validationErrors.isEmpty()) {
            try {
                store.save(r);
            } catch (DatabaseException e) {
                return JsonRepresentation.instance.toResponse(400, e.getMessage());
            }

            return JsonRepresentation.instance.toResponse(202, "Record saved successfully");
        }

        return JsonRepresentation.instance.toResponseWithErrors(400, "", validationErrors);

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result update(String hash) {
        Record r = new Record(request().body().asJson());
        List<ValidationError> validationErrors = new Validator(singletonList(registerName), fieldNames).validate(r);

        if (validationErrors.isEmpty()) {
            try {
                store.update(hash, r);
            } catch (DatabaseException e) {
                return JsonRepresentation.instance.toResponse(400, e.getMessage());
            }
            return JsonRepresentation.instance.toResponse(202, "Record saved successfully");
        }

        return JsonRepresentation.instance.toResponseWithErrors(400, "", validationErrors);
    }

    public F.Promise<Result> findByKey(String key, String value) {
        F.Promise<Optional<Record>> recordF = F.Promise.promise(() -> store.findByKV(key, value));
        return recordF.map(this::getResponse);
    }

    public F.Promise<Result> findByHash(String hash) {
        F.Promise<Optional<Record>> recordF = F.Promise.promise(() -> store.findByHash(hash));
        return recordF.map(this::getResponse);
    }

    private Representation representation() {
        return representationFor(request().getQueryString(REPRESENTATION_QUERY_PARAM));
    }

    private Result getResponse(Optional<Record> recordO) {
        return recordO.map(record -> representation().toRecord(record, getHistoryFor(record)))
                .orElse(HtmlRepresentation.instance.toResponse(404, "Entry not found"));
    }

    public F.Promise<Result> search() {

        F.Promise<List<Record>> recordsF = F.Promise.promise(() -> {
            if (request().queryString().containsKey("_query")) {
                return store.search(request().queryString().get("_query")[0]);
            } else {
                HashMap<String, String> map = new HashMap<>();
                request().queryString().entrySet().stream()
                        .filter(queryParameter -> !queryParameter.getKey().startsWith("_"))
                        .forEach(queryParameter -> map.put(queryParameter.getKey(), queryParameter.getValue()[0]));
                return store.search(map);
            }
        });

        Representation representation = representation();
        return recordsF.map(representation::toListOfRecords);
    }

    private List<RecordVersionInfo> getHistoryFor(Record r) {
        return store.history(registerName, r.getEntry().get(registerName).textValue());
    }


}
