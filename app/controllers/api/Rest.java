package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import controllers.conf.Register;
import play.libs.F;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.openregister.domain.Record;
import uk.gov.openregister.store.DatabaseException;
import uk.gov.openregister.store.Store;
import uk.gov.openregister.validation.ValidationError;
import uk.gov.openregister.validation.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Rest extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result create() throws JsonProcessingException {
        Record r = new Record(request().body().asJson());

        List<ValidationError> validationErrors = new Validator(singletonList(Register.instance.name()), Register.instance.fields()).validate(r);

        if (validationErrors.isEmpty()) {
            try {
                Register.instance.store().save(r);
            } catch (DatabaseException e) {
                return JsonRepresentation.instance.toResponse(400, e.getMessage());
            }

            return JsonRepresentation.instance.toResponse(202, "Record saved successfully");
        }

        return JsonRepresentation.toResponseWithErrors(400, "", validationErrors);

    }

    //TODO: do validation
    @BodyParser.Of(BodyParser.Json.class)
    public static Result update(String hash) {
        Record r = new Record(request().body().asJson());
        List<ValidationError> validationErrors = new Validator(singletonList(Register.instance.name()), Register.instance.fields()).validate(r);

        if (validationErrors.isEmpty()) {
            try {
                Register.instance.store().update(hash, r);
            } catch (DatabaseException e) {
                return JsonRepresentation.instance.toResponse(400, e.getMessage());
            }
            return JsonRepresentation.instance.toResponse(202, "Record saved successfully");
        }

        return JsonRepresentation.toResponseWithErrors(400, "", validationErrors);
    }

    public static F.Promise<Result> findByKey(String key, String value) {
        F.Promise<Optional<Record>> recordF = F.Promise.promise(() -> Register.instance.store().findByKV(key, value));
        return recordF.map(record -> Representations.toRecord(request(), record, emptyList()));
    }

    public static F.Promise<Result> findByHash(String hash) {
        Store store = Register.instance.store();
        String primaryKey = Register.instance.name();
        F.Promise<Optional<Record>> recordF = F.Promise.promise(() -> store.findByHash(hash));
        return recordF.map(record ->
                        Representations.toRecord(
                                request(),
                                record,
                                record.map(r -> store.history(primaryKey, r.getEntry().get(primaryKey).textValue())).orElse(emptyList())
                        )
        );
    }

    public static F.Promise<Result> search() {

        F.Promise<List<Record>> recordsF = F.Promise.promise(() -> {
            if (request().queryString().containsKey("_query")) {
                return Register.instance.store().search(request().queryString().get("_query")[0]);
            } else {
                HashMap<String, String> map = new HashMap<>();
                request().queryString().entrySet().stream()
                        .filter(queryParameter -> !queryParameter.getKey().startsWith("_"))
                        .forEach(queryParameter -> map.put(queryParameter.getKey(), queryParameter.getValue()[0]));
                return Register.instance.store().search(map);
            }
        });

        return recordsF.map(records -> Representations.toListOfRecords(request(), records));
    }


}
