package com.alexcatarau.hba;

import com.alexcatarau.hba.config.IntegrationTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
class HbaApplicationTests {

	@Test
	public void mainMethodTest() {
		HbaApplication.main(new String[]{"--spring.main.web-environment=false"});
	}
}
