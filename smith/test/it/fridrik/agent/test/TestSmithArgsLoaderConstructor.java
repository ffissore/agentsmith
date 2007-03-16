package it.fridrik.agent.test;

import it.fridrik.agent.SmithArgs;
import junit.framework.TestCase;

public class TestSmithArgsLoaderConstructor extends TestCase {

	public void testLoaderConstructor() {
		SmithArgs args = new SmithArgs(" /home/federico/classes ",
				" /home/federico/jars ", -4);

		assertEquals(
				"classes=/home/federico/classes/,jars=/home/federico/jars/,period=-4",
				args.toString());

	}

}
