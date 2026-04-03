package com.redpatitas.demo;

import com.redpatitas.redPatitas.redPatitas;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
	classes = redPatitas.class,
	properties = {
		"spring.autoconfigure.exclude=" +
			"org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration," +
			"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
		"spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.jpa.open-in-view=false",
		"springdoc.api-docs.enabled=false"
	}
)
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
