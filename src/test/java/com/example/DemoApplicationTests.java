package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {

	}

}

@SpringBootTest
class ConnectionTest {

	@Autowired
	private DataSource dataSource;
	@Test
	void passwordEncoder() throws SQLException {
		System.out.println(new BCryptPasswordEncoder().encode("user"));
	}

	@Test
	void testConnection() throws SQLException {
		try (Connection conn = dataSource.getConnection()) {
			System.out.println("✅ 数据库连接成功！");
			System.out.println("⛁ 数据库名称: " + conn.getCatalog()); // 应输出 jdata
			System.out.println("🚀 驱动版本: " + conn.getMetaData().getDriverVersion());
		}
	}
}
