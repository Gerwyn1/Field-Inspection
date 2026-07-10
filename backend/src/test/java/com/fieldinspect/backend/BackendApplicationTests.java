package com.fieldinspect.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // -> boots against H2, so this test needs no Docker either
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
