package nl.teslanet.mule.connectors.coap.automation.functional;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import nl.teslanet.mule.connectors.coap.client.CoapClientConnector;

public class GreetTestCases extends
		AbstractTestCase<CoapClientConnector> {

	public GreetTestCases() {
		super(CoapClientConnector.class);
	}

	@Before
	public void setup() {
		// TODO
	}

	@After
	public void tearDown() {
		// TODO
	}

	@Test
	public void verify() {
		java.lang.String expected = null;
		java.lang.String friend = null;
		//assertEquals(getConnector().greet(friend), expected);
	}

}