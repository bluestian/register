package functional;

import controllers.conf.Register;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

public class RegisterInitTest extends ApplicationTests {

    @Test
    public void testKeysAreReadOnStartup() throws Exception {
        assertThat(Register.instance.keys()).isEqualTo(Arrays.asList("name", "key1", "key2"));
    }

    @Test
    public void testNameIsReadOnStartup() throws Exception {
        assertThat(Register.instance.name()).isEqualTo("test-register");
    }

    @Test
    public void testFriendlyNameIsReadOnStartup() throws Exception {
        assertThat(Register.instance.friendlyName()).isEqualTo("Test Register");
    }
}
