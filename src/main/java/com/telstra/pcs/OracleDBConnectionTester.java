package com.telstra.pcs;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.jdbc.driver.OracleConnection;

public class OracleDBConnectionTester {

	private static final Logger logger = LoggerFactory.getLogger(OracleDBConnectionTester.class);

	public void test() {
		try {

			/**
			 * <pre>
			 * Please Note, Avaya Proactive Contact Oracle Database 11g Enterprise Edition Release
			 * 11.2.0.3.0 it is configured with secure connection TCPS on port 2484. 
			 * 
			 * it supports SSLv3 and cipher SSL_RSA_EXPORT_WITH_RC4_40_MD5 the wallet RSA key
			 * is less than 1024. 
			 * 
			 * However, SSLv3,  SSL_RSA_EXPORT_WITH_RC4_40_MD5, and RSA key less
			 * than 1024 is disabled default with java 1.8+ (you will not able to connect the database).
			 * 
			 * You can enabled SSLv3, SSL_RSA_EXPORT_WITH_RC4_40_MD5 and RSA Key 512 with follow methods.
			 *  
			 * Option 1
			 * You can set it in the JRE/lib/security/java.security file. by make changes to 
			 * 
			 * jdk.certpath.disabledAlgorithms  (change RSA keySize < 1024 to  RSA keySize < 512)
			 * jdk.tls.disabledAlgorithms  (remove 	 SSLv3, RC4)
			 *  
			 * Option 2
			 * rather than change default java.security, you can have new copy of the file, update
			 * 
			 * jdk.certpath.disabledAlgorithms  (change RSA keySize < 1024 to  RSA keySize < 512)
			 * jdk.tls.disabledAlgorithms  (remove 	 SSLv3, RC4)
			 * and set -Djava.security.properties=location/java.security
			 * 
			 * Option 3
			 * Set it in the code.
			 * Security.setProperty("jdk.certpath.disabledAlgorithms",
					"MD2, MD5, SHA1 jdkCA & usage TLSServer, RSA keySize < 512, DSA keySize < 1024, EC keySize < 224");
					
			   Security.setProperty("jdk.tls.disabledAlgorithms",
					"DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL");
			 * 
			 * 
			 * </pre>
			 */

			logger.debug("load driver oracle.jdbc.driver.OracleDriver");
			// load oracle jdbc driver
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// add oracle pki provider to load cwallet.sso
			Security.insertProviderAt(new oracle.security.pki.OraclePKIProvider(), 3);

			// change RSA keySize < 1024 to RSA keySize < 512
			Security.setProperty("jdk.certpath.disabledAlgorithms",
					"MD2, MD5, SHA1 jdkCA & usage TLSServer, RSA keySize < 512, DSA keySize < 1024, EC keySize < 224");

			// remove SSLv3, RC4
			Security.setProperty("jdk.tls.disabledAlgorithms",
					"DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL");

			String username = "username"; // CHANGE THIS
			String password = "password"; // CHANGE THIS
			// enter the IP address of Dialler.
			String host = "xxx.xxx.xxx.xxx"; // CHANGE THIS

			// define wallet location (copy /opt/dbase/wallet/cwallet.sso and
			// ewallet.ps12 to this location. (or download the wallet files from
			// https://support.avaya.com/)
			// change below location to pcs-oracle\oracle\wallet
			String walletLocation = "C:\\Work\\github\\pcs-oracle\\oracle\\wallet"; // CHANGE THIS

			String connStr = "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCPS)(HOST = "
					+ host + ")(PORT = 2484))) (CONNECT_DATA = (SERVICE_NAME = orastd)))";

			Properties properties = new Properties();
			properties.setProperty("user", username);
			properties.setProperty("password", password);
			properties.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_NET_CONNECT_TIMEOUT, "10000");
			properties.setProperty(OracleConnection.CONNECTION_PROPERTY_WALLET_LOCATION,
					"(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY=" + walletLocation + ")))");
			properties.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_SSL_CIPHER_SUITES,
					"(SSL_RSA_EXPORT_WITH_RC4_40_MD5)");

			logger.info("****** Starting JDBC Connection test *******");
			// String sqlQuery = "select sysdate from dual";
			String sqlQuery = "select * from mo_agent";

			Connection conn = DriverManager.getConnection(connStr, properties);
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();
			logger.info("Running SQL query: [{}]", sqlQuery);
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (resultSet.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1)
						System.out.print(",  ");
					String columnValue = resultSet.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}

			statement.close();
			conn.close();
			logger.info("JDBC connection test successful!");
		} catch (Exception ex) {
			logger.error("Exception occurred connecting to database: {}", ex.getMessage());
			ex.printStackTrace();
		}
	}
}
