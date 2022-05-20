package ecommerce.common;

import java.util.Arrays;
import java.util.Random;

public class StringUtils{

	private static final char[] Hex62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKMNOPQRSTUVWXYZ".toCharArray();
	public static String hex62EncodingWithRandom(int fixLength, long... numbers) {
		StringBuilder buffer = new StringBuilder();
		Arrays.stream(numbers).forEach(number->{
			if (number<0) {number=-number;}
			while(number>0&&buffer.length()<fixLength) {
				buffer.append(Hex62[(int) (number%Hex62.length)]);
				number/=Hex62.length;
			}
		});
		Random random = new Random();
		while(buffer.length()<fixLength){
			long number = random.nextLong();
			if (number<0) {number+=Long.MAX_VALUE;}
			while(number>0&&buffer.length()<fixLength) {
				buffer.append(Hex62[(int) (number%Hex62.length)]);
				number/=Hex62.length;
			}
		}
		return buffer.toString();
	}

}