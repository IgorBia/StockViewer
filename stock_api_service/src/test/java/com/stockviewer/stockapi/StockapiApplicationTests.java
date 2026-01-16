package com.stockviewer.stockapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest()
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
@Import(TestSecurityConfig.class)
class StockapiApplicationTests {

	// @Test
	// void contextLoads() {
	// }

}
