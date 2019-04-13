package Pager;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class LRU {
	
	public static void LRU(String[] args) throws FileNotFoundException {
		File file = new File("random-numbers.txt");
		Scanner in = new Scanner(file);
		
		int m = Integer.parseInt(args[0]);	//machine size
		int p = Integer.parseInt(args[1]);	//page size
		int s = Integer.parseInt(args[2]);	//process size
		int j = Integer.parseInt(args[3]);	//job mix
		int n = Integer.parseInt(args[4]);	//number of references
		int frameNum = m/p;	//number of frames
		int pageNum = s/p;		//number of pages
		int processNum = 0;	//number of processes
		double A = 0;
		double B = 0;
		double C = 0;
		boolean hasEmpty = true;

		
		ArrayList<int[]> frametable = new ArrayList<int[]>(frameNum);
		for(int i=0;i<frameNum;i++) {
			frametable.add(new int[] {-1,-1,-1});
		}
		
		int[]lastRef = new int[frameNum];
		String r = args[5];
		
		if(j==1) {
			processNum = 1;
			A = 1;
		}
		else {
			if(j==2)
				A = 1;
			processNum = 4;
		}
		
		int fault[] = new int[processNum+1];
		int residency[] = new int[processNum+1];
		int eviction[] = new int[processNum+1];
		
		int currentWord[]= new int[processNum+1];
		int nextWord[]= new int[processNum+1];
		int ref[]= new int[processNum+1];
		
		int time = 0;
		int word = 0;
		int currentProcess = 1;
		while(time!=processNum*n) {
			if(ref[currentProcess]==n)
				currentProcess++;
			else if(j!=1&&time%3==0) {
				currentProcess = (time/3)%4+1;
			}
			
			time++;
			ref[currentProcess]++;
			if(j==4) {
				if(currentProcess==1) {
					A = 0.75;
					B = 0.25;
				}
				else if(currentProcess==2) {
					A = 0.75;
					B = 0;
					C = 0.25;
				}
				else if(currentProcess==3) {
					A = 0.75;
					B = 0.125;
					C = 0.125;
				}
				else if(currentProcess==4) {
					A = 0.5;
					B = 0.125;
					C = 0.125;
				}
			}
			
			
			
			//process current reference
			int page = 0;
			if((time==1&&j==1)||((time==1||time==4||time==7||time==10)&&j!=1)) {	//the first word in current process
				word = 111*currentProcess%s;
				page = word/p;
			}
			else {
				word = nextWord[currentProcess];
				page = word/p;
			}
			
			int []pair = {currentProcess, page, time};
			
			int hitFrame = isHit(pair, frametable);
			//if no pairs in frametable matches, it is a page fault
			if(hitFrame==-1) {
				//if there is an empty frame, put the pair in it
				if(hasEmpty) {
					for(int i=frameNum-1;i>=0;i--) {
						if(Arrays.equals(frametable.get(i), new int[]{-1,-1,-1})) {
							frametable.set(i,pair);
							fault[currentProcess]++;
							lastRef[i]=time;
							//System.out.println(currentProcess+" references word "+word+" (page "+page+") at time "+time+": Fault, using free frame "+i);
							if(i==0)
								hasEmpty = false;
							break;
						}
					}
				}
				//there is no empty frame, choose the least recently used page to evict
				else {
					int min = min(lastRef);
					lastRef[min]=time;
					int[]prev = frametable.get(min);
					frametable.set(min, pair);
					fault[currentProcess]++;
					residency[prev[0]]+=time-prev[2];
					eviction[prev[0]]++;
					//System.out.println(currentProcess+" references word "+word+" (page "+page+") at time "+time+": Fault, evicting page "+prev[1]+" from "+prev[0]+ " from frame "+min);
				}
			}
			
			else {
				//System.out.println(currentProcess+" references word "+word+" (page "+page+") at time "+time+ ": Hit in frame "+hitFrame);
				lastRef[hitFrame]=time;
			}
			
			
			int rd = in.nextInt();	//get the next number from random number list
			//System.out.println(currentProcess+" uses random: "+rd);
			
			//calculate next reference
				double y = rd/(Integer.MAX_VALUE+1d);
				if(y<A) {
					nextWord[currentProcess] = (word+1)%s;
				}
				else if(y<A+B) {
					nextWord[currentProcess] =  (word-5+s)%s;
				}
				else if(y<A+B+C) {
					nextWord[currentProcess] = (word+4)%s;
				}
				else {
					rd = in.nextInt();
					nextWord[currentProcess] = rd%s;
				}
			
		}
		
		//print
		System.out.println("The machine size is "+m);
		System.out.println("The page size is "+p);
		System.out.println("The process size is "+s);
		System.out.println("The job mix number is "+j);
		System.out.println("The number of references per process is "+n);
		System.out.println("The replacement algorithm is "+r);
		
		int totalFault = 0;
		int totalRes = 0;
		int totalEvic = 0;
		System.out.println();
		
		DecimalFormat df = new DecimalFormat(".###############");
		for(int i=1;i<=processNum;i++) {
			if(eviction[i]==0) {
				System.out.println("Process "+i+" had "+fault[i]+" faults");
				totalFault+=fault[i];
				System.out.println("     With no evictions, the average residency is undefined.");
			}
			else {
				double res = Double.parseDouble(df.format(residency[i]))/eviction[i];
				//System.out.println(residency[i]+"    "+eviction[i]);
				totalFault+=fault[i];
				totalRes+=residency[i];
				totalEvic+=eviction[i];
				System.out.println("Process "+i+" had "+fault[i]+" faults and "+res+" average residency.");
			}
			
		}
		
		System.out.println();
		if(totalEvic==0) {
			System.out.println("The total number of faults is "+totalFault);
			System.out.println("     With no evictions, the overall average residence is undefined.");
		}
		else {
			double totalAvgRes = Double.parseDouble(df.format(totalRes))/totalEvic;
			System.out.println("The total number of faults is "+totalFault+" and the overall residency is "+totalAvgRes);
		}
		
	}
	
	public static int isHit(int[]a, ArrayList<int[]> frametable) {
		int size = frametable.size();
		for(int i = 0;i<size;i++) {
			int[]b = frametable.get(i);
			if(a[0]==b[0]&&a[1]==b[1])
				return i;
		}
		return -1;
	}
	
	public static int min(int[]lastRef) {
		int min = 0;
		for(int i =0;i<lastRef.length;i++) {
			if(lastRef[i]<lastRef[min])
				min = i;
		}
		return min;
	}
	
}
