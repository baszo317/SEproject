package logistics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsCoreTest {

    @Test
    void smoke() {
        LogisticsCore core = new LogisticsCore();
        assertNotNull(core);
    }
}
