package Pager;

import java.io.FileNotFoundException;

public class main {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		if(args[5].toLowerCase().equals("lru"))
			LRU.LRU(args);
		else if(args[5].toLowerCase().equals("lifo"))
			LIFO.LIFO(args);
		else
			RANDOM.RANDOM(args);
	}

}
