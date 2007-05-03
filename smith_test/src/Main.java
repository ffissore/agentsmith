public class Main {

	public static void main(String[] args) throws Exception {
		Bar bar = new Bar();
		do {
			bar.doSomething();
			Thread.sleep(1000);
		} while (true);
	}
}
