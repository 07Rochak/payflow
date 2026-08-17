package com.rochak.payflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TimeZone;

@SpringBootTest
class PayflowApplicationTests {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

	@Test
	void contextLoads() {
	}

}
