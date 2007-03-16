package it.fridrik.agent.test;

import it.fridrik.agent.SmithArgs;
import junit.framework.TestCase;

public class TestSmithArgsNew extends TestCase {

	public void testClasses() {
		String agentargs = " classes = /home/federico/classes ";
		SmithArgs args = new SmithArgs(agentargs);

		assertEquals("/home/federico/classes/", args.getClassFolder());
		assertNull(args.getJarFolder());
		assertEquals(-1, args.getPeriod());
		assertTrue(args.isValid());
	}

	public void testJars() {
		String agentargs = " jars = /home/federico/jars ";
		SmithArgs args = new SmithArgs(agentargs);

		assertNull(args.getClassFolder());
		assertEquals("/home/federico/jars/", args.getJarFolder());
		assertEquals(-1, args.getPeriod());
		assertFalse(args.isValid());
	}

	public void testPeriod() {
		String agentargs = " period = 500 ";
		SmithArgs args = new SmithArgs(agentargs);

		assertNull(args.getClassFolder());
		assertNull(args.getJarFolder());
		assertEquals(500, args.getPeriod());
		assertFalse(args.isValid());
	}

	public void testAllArgs() {
		String agentargs = " classes = /home/federico/classes , jars = /home/federico/jars , period = 39 ";
		SmithArgs args = new SmithArgs(agentargs);

		assertEquals("/home/federico/classes/", args.getClassFolder());
		assertEquals("/home/federico/jars/", args.getJarFolder());
		assertEquals(39, args.getPeriod());
		assertTrue(args.isValid());
	}

	public void testToString() {
		String agentargs = " classes = /home/federico/classes , jars = /home/federico/jars , period = 39 ";
		SmithArgs args = new SmithArgs(agentargs);

		assertEquals(
				"classes=/home/federico/classes/,jars=/home/federico/jars/,period=39",
				args.toString());
	}

}
