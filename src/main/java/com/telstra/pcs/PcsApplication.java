package com.telstra.pcs;

public class PcsApplication {

	public static void main(String[] args) {

		OracleDBConnectionTester tester = new OracleDBConnectionTester();

		tester.test();
	}

}
