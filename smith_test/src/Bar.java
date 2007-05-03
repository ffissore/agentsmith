public class Bar {

	private Foo foo;

	public Bar() {
		foo = new Foo();
	}

	public void doSomething() {
		System.out.println(getClass().getName() + ": I'm doing something");
		foo.doSomethingElse();
	}

	class Foo {
		void doSomethingElse() {
			System.out.println(getClass().getName() + ": What else???");
		}
	}
}
