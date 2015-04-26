package net.samagames.uhcrun.utils;

import java.util.Random;

public class Utils {
	public static int randomInt(int aStart, int aEnd){
		 Random aRandom = new Random();
		 if (aStart > aEnd) {
		    throw new IllegalArgumentException("aStart > aEnd");
		 }
		 //get the range, casting to long to avoid overflow problems
		 long range = (long)aEnd - (long)aStart + 1;
		 // compute a fraction of the range, 0 <= frac < range
		 long fraction = (long)(range * aRandom.nextDouble());
		 int randomNumber =  (int)(fraction + aStart);    
		 return randomNumber;
	}
}
