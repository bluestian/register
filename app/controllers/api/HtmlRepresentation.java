package controllers.api;

import controllers.App;
import play.mvc.Result;
import uk.gov.openregister.domain.Record;
import uk.gov.openregister.domain.RecordVersionInfo;

import java.util.List;
import java.util.Optional;

import static play.mvc.Results.ok;
import static play.mvc.Results.status;

public class HtmlRepresentation implements Representation {
    @Override
    public Result toResponse(int status, String message) {
        return status(status, views.html.error.render(message));
    }

    @Override
    public Result toListOfRecords(List<Record> records) {
        return ok(views.html.entries.render(App.instance.register.fields(), records));
    }

    @Override
    public Result toRecord(Optional<Record> recordO, List<RecordVersionInfo> history) {
        return recordO.map(record ->
                (Result) ok(views.html.entry.render(App.instance.register.fields(), record, history)))
                .orElse(toResponse(404, "Entry not found"));
    }

    public static Representation instance = new HtmlRepresentation();
}
