package functional.html;

import functional.ApplicationTests;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import play.libs.ws.WSResponse;
import uk.gov.openregister.domain.Record;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;

public class SearchPagesTest extends ApplicationTests {

    @Test
    public void testGetSearchPage() throws Exception {

        WSResponse response = get("/");

        assertThat(response.getStatus()).isEqualTo(OK);
        String body = response.getBody();
        assertThat(body).contains("The official register of test entries throughout the UK");
        assertThat(body).contains("Part of");
        assertThat(body).contains("Test registry");
        assertThat(body).contains("This register exists as an accurate list of all test entries");
        assertThat(body).contains("Search register entries");
    }

    @Test
    public void testGetSearchPageShowsTheTotalAmountOfEntries() throws Exception {
        postJson("/create","{\"name\":\"The Entry\",\"key1\": \"value1\",\"key2\": [\"A\",\"B\"]}");
        postJson("/create", "{\"name\":\"The Entry\",\"key1\": \"value2\",\"key2\": [\"A\",\"B\"]}");
        postJson("/create", "{\"name\":\"The Entry\",\"key1\": \"value3\",\"key2\": [\"A\",\"B\"]}");


        WSResponse response = get("/");

        assertThat(response.getStatus()).isEqualTo(OK);
        String body = response.getBody();
        assertThat(body).contains("<p class=\"big-number\">3</p>");
    }

    @Test
    public void testSubmitSearchQueryAndReturnsListOfEntries() throws Exception {
        postJson("/create", "{\"name\":\"The Entry1\",\"key1\": \"value1\",\"key2\": [\"A\",\"B\"]}");
        postJson("/create", "{\"name\":\"The Entry2\",\"key1\": \"value2\",\"key2\": [\"C\",\"D\"]}");
        postJson("/create", "{\"name\":\"The Entry3\",\"key1\": \"value1\",\"key2\": [\"E\",\"F\"]}");

        WSResponse response = get("/search?_query=value1");
        assertThat(response.getStatus()).isEqualTo(OK);

        org.jsoup.nodes.Document html = Jsoup.parse(response.getBody());

        Element table = html.getElementById("entries-table");
        assertThat(table).isNotNull();
        Elements tr = table.select("tr");
        Elements th = tr.first().select("th");
        assertThat(th.get(0).text()).isEqualTo("hash");
        assertThat(th.get(1).text()).isEqualTo("name");
        assertThat(th.get(2).text()).isEqualTo("key1");
        assertThat(th.get(3).text()).isEqualTo("key2");

        Elements td1 = tr.get(1).select("td");
        assertThat(td1.get(0).select("a").first().toString()).isEqualTo("<a href=\"/hash/8b3d7a3327874ec4bf14070a1f8e79bca1acfd2b\">8b3d7a3</a>");
        assertThat(td1.get(1).text()).isEqualTo("The Entry1");
        assertThat(td1.get(2).text()).isEqualTo("value1");
        assertThat(td1.get(3).text()).isEqualTo("['A', 'B']");

        Elements td2 = tr.get(2).select("td");
        assertThat(td2.get(0).select("a").first().toString()).isEqualTo("<a href=\"/hash/a5c541460c7962ee36544840e671800b8ab202f1\">a5c5414</a>");
        assertThat(td2.get(1).text()).isEqualTo("The Entry3");
        assertThat(td2.get(2).text()).isEqualTo("value1");
        assertThat(td2.get(3).text()).isEqualTo("['E', 'F']");
    }

    @Test
    public void testRenderFieldWithoutKeyValueAndAFieldWithValueIsAList() throws Exception {
        String json = "{\"name\":\"The Entry\",\"key1\": \"value1\",\"key2\": [\"A\",\"B\"]}";
        String hash =new Record(json).getHash();
        postJson("/create", json);

        WSResponse response = get("/hash/" + hash);

        assertThat(response.getStatus()).isEqualTo(OK);

        org.jsoup.nodes.Document html = Jsoup.parse(response.getBody());

        Element dl = html.select("dl").first();
        assertThat(dl).isNotNull();

        Elements dt = dl.select("dt");
        Elements dd = dl.select("dd");

        assertThat(dt.get(0).text()).isEqualTo("hash");
        assertThat(dd.get(0).text()).isEqualTo(hash);

        assertThat(dt.get(1).text()).isEqualTo("name");
        assertThat(dd.get(1).text()).isEqualTo("The Entry");

        assertThat(dt.get(2).text()).isEqualTo("key1");
        assertThat(dd.get(2).text()).isEqualTo("value1");

        assertThat(dt.get(3).text()).isEqualTo("key2");
        assertThat(dd.get(3).text()).isEqualTo("['A', 'B']");
    }


    @Test
    public void testRenderFieldAsListInEntries() throws Exception {
        postJson("/create", "{\"name\":\"The Entry\",\"key1\": \"value1\",\"key2\": [\"A\",\"B\"]}");

        WSResponse response = search("key1", "value1", "html");

        assertThat(response.getStatus()).isEqualTo(OK);

        org.jsoup.nodes.Document html = Jsoup.parse(response.getBody());

        Element key2 = html.getElementsByClass("key2").first();
        assertThat(key2).isNotNull();

        assertThat(key2.text()).isEqualTo("['A', 'B']");

    }

    @Test
    public void testEntryShowsNameIfPresent() throws Exception {
        String json = "{\"name\":\"The Entry\",\"key1\": \"value1\",\"key2\": [\"A\",\"B\"]}";
        String hash =new Record(json).getHash();
        postJson("/create", json);

        WSResponse response = get("/hash/" + hash);

        assertThat(response.getStatus()).isEqualTo(OK);

        org.jsoup.nodes.Document html = Jsoup.parse(response.getBody());

        Element h1 = html.getElementById("entry_name");
        assertThat(h1).isNotNull();
        assertThat(h1.text()).isEqualTo("The Entry");
    }
}