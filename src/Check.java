
public class Check {
	public static void main(String[] args) {
		int times = 8765;
		long acc = 0;
		for (int i = 0; i <= times; i++) {
			acc += Math.floor(i * Math.sqrt(2));
		}
		System.out.println(acc);
	}
}
