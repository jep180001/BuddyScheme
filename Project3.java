import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class Project3 {
	
	//variables
	private ArrayList<Memory> memory;
	static char[] assignRequests = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P'};
	final static int maxMemorySize = 1024;
	
	//constructor
	Project3(int size){
		
		memory = new ArrayList<>();
		memory.add(new Memory(size));
	}
	//the class that deals with each block of memory
	private class Memory{
		
		//variables
		private int size;
		private boolean free;
		private char owner;
		private Memory buddy;
		
		//constructor
		public Memory(int size) {
			
			this.size = size;
			this.free = true;
			
		}
		//function to get the memory block size
		public int getBlockSize() {
			
			return size;
		}
		//function to get the owner of the block
		public char getBlockOwner() {
			
			return owner;
		}
		//function to see if a block is assigned
		public boolean isFree() {
			
			return free;
		}
		//function to assign a block of memory
		public void assignBlock(char owner) {
			
			this.free = false;
			this.owner = owner;
		}
		//function to release a block of memory
		public void releaseBlock() {
			
			this.owner = '\u0000';
			this.free = true;
			this.buddy = null;
		}
		//function to merge blocks
		public Memory mergeBlocks() {
			
			//merge blocks by doubling the size
			buddy = null;
			size = size * 2;
			Memory merged = new Memory(size);
			
			return merged;
		}
		//function to split a block of memory
		public Memory[] splitBlock() {
			
			//get the hole and split into two equal sizes of half the original
			Memory[] splitHole = new Memory[2];
			splitHole[0] = new Memory(size/2);
            splitHole[1] = new Memory(size/2);
            splitHole[0].buddy = splitHole[1];
            splitHole[1].buddy = splitHole[0];
            
            return splitHole;
		}
		//function to format the blocks of memory
		public String toString() {
			
			if(free) {
				
				return "|" + "   " + size + "K";
				
			} else {
				
				return "|" + owner + "  " + size + "K";
		
			}
		}
	}
	//memory sizes for the blocks of the Buddy system
	//2^20 = = 1 M = 1024 K is our initial block size - largest memory size
	//2^19 = 512 K
	//2^18 = 256 K
	//2^17 = 128K
	//2^16 = 64K - smallest memory size
	//function to assign memory based on request
	public void allocation(int size, char owner) {
		
	    int j = 0;
	    int x = (int) Math.ceil(Math.log(size)/Math.log(2));
	    x = (int) Math.pow(2, x);

	    int lastIndex = -1;

	    while(j < memory.size()) {

	        Memory temp = memory.get(j);

	        //find a larger block or one of the same size
	        if(temp.isFree() && temp.getBlockSize() >= x) {

	            //if the block size matches the request
	            if(temp.getBlockSize() == x) {

	                //assign the block
	                temp.assignBlock(owner);

	                return;

	            } else {

	                //check for existing split blocks before splitting a new block
	                for (int i = lastIndex + 1; i < memory.size(); i++) {

	                    Memory existBlock = memory.get(i);
	                    Memory smallBlock = memory.get(i);

	                    if (existBlock.isFree() && existBlock.getBlockSize() == x) {

	                        existBlock.assignBlock(owner);
	                        lastIndex = i;
	                        return;

	                    //check to split the smallest block size available needed to get request size
	                    } else if(smallBlock.isFree() && smallBlock.getBlockSize() < temp.getBlockSize()) {
	                    	
	                        Memory[] splitBlocks = smallBlock.splitBlock();
	                        memory.set(i, splitBlocks[0]);
	                        memory.add(i+1, splitBlocks[1]);

	                        //then assign to owner based on which block is available
	                        if (splitBlocks[1].isFree() && splitBlocks[1].getBlockSize() == x) {
	                        	
	                            splitBlocks[1].assignBlock(owner);
	                            return;
	                            
	                        } else if (splitBlocks[0].isFree() && splitBlocks[0].getBlockSize() == x) {
	                        	
	                            splitBlocks[0].assignBlock(owner);
	                            return;
	                        }
	                    }
	                }
	                
	                //else split the memory block
	                Memory[] blocks = temp.splitBlock();
	                memory.set(j, blocks[0]);
	                memory.add(j+1, blocks[1]);

	            }

	        } 
	        //update index
	        j++;
	    }
	    //otherwise, request could not be filled
	    System.out.println("Error: Memory request could not be processed.");

	}
	//function to release memory and merge based on request
	public void deallocation(char owner) {
		
		//iterate through the list
	    int i = 0;
	    while (i < memory.size()) {
	    	
	        Memory te = memory.get(i);
	        
	        //if it matches the release request
	        if (te.getBlockOwner() == owner) {
	        	
	        	//free the block of memory
	            te.releaseBlock();
	            
	            //determine if the block can be merged with its buddy
	            boolean merged = true;
	            while (merged) {
	                
	                merged = false;
	                
	                //if the buddy is to its left
	                if (i > 0 && memory.get(i-1).getBlockOwner() == '\u0000') {
	                	
	                    Memory leftAdjacent = memory.get(i-1);
	                    
	                    //if the buddy has the same size
	                    if (leftAdjacent.getBlockSize() == te.getBlockSize()) {
	                    	
	                    	//remove the block and its buddy
	                        memory.remove(i);
	                        memory.remove(i-1);
	                        
	                        //merge both blocks together and add back to list
	                        Memory blockMerged = leftAdjacent.mergeBlocks();
	                        memory.add(i-1, blockMerged);
	                        i--;
	                        merged = true;
	                    }
	                }
	                //if the buddy is to its right
	                if (i < memory.size()-1 && memory.get(i+1).getBlockOwner() == '\u0000') {
	                	
	                    Memory rightAdjacent = memory.get(i+1);
	                    
	                    //if the buddy has the same size
	                    if (rightAdjacent.getBlockSize() == te.getBlockSize()) {
	                    	
	                    	//remove both blocks
	                        memory.remove(i+1);
	                        memory.remove(i);
	                        
	                        //merge both blocks together and add back to list
	                        Memory blockMerged = rightAdjacent.mergeBlocks();
	                        memory.add(i, blockMerged);
	                        merged = true;
	                    }
	                }
	            }
	            
	            //continue merging other free adjacent blocks
	            boolean continueMerging = true;
	            while (continueMerging) {
	            	
	                continueMerging = false;
	                
	                //look for other free adjacent blocks that can be merged in the list
	                for (int j = 0; j < memory.size()-1; j++) {
	                	
	                	//get a block and its adjacent block
	                    Memory block = memory.get(j);
	                    Memory blockAdjacent = memory.get(j+1);
	                    
	                    //if two blocks are free and the same size
	                    if (block.getBlockOwner() == '\u0000' && blockAdjacent.getBlockOwner() == '\u0000' && block.getBlockSize() == blockAdjacent.getBlockSize()) {
	                    	
	                    	//remove both blocks
	                        memory.remove(j+1);
	                        memory.remove(j);
	                        
	                        //add merged blocks back to list
	                        Memory blockMerged = block.mergeBlocks();
	                        memory.add(j, blockMerged);
	                        continueMerging = true;
	                        break;
	                    }
	                }
	            }
	            
	            return;
	        }
	        
	        i++;
	    } 
	    //otherwise, could not process release request
	    System.out.println("Error: Release request could not be processed.");
	}
	//function to display the buddy scheme for memory requests
	public void display() {
		
		//to print the top line for the output
		for(int i=0; i<memory.size()+1; i++) {
			
			System.out.print("-------");
		}
		System.out.print("\n");
		
		//print the memory block
		for(int i = memory.size() - 1; i>= 0; i--) {
			
			System.out.print(memory.get(i));
		}
        
        System.out.print(" |");
        System.out.print("\n");
        
        //print the bottom line for the output
        for(int i=0; i<memory.size()+1; i++) {
        	
        	System.out.print("-------");
        }
	}
	//main function
	public static void main(String[] args) throws FileNotFoundException {
		
		//variables
		String input;
		int y = 0;
		int i = 0;
		
		//get the input file as a command line argument
		String fileName = args[0];
		File file = new File(fileName);
		Scanner scan = new Scanner(file);

		//create initial memory size of 1M
		Project3 buddy = new Project3(maxMemorySize);
		buddy.display();
		
		//read input file
		//get requests from the file
		while(scan.hasNextLine()) {
			
			//determine by type of request
			input = scan.nextLine();
			
			//if the request is a memory request
			if(input.contains("Request")) {
				
				//get the memory amount and store it
				String x = input.replaceAll("[^0-9]", "");
				y = Integer.parseInt(x);
				
				//start buddy scheme to get memory
				System.out.println("\nRequest " + y + "K");
				char owner = assignRequests[i];
				buddy.allocation(y, owner);
				
				//display the new memory allocation
				buddy.display();
				i++;
				
			//if the request is a memory release
			} else if (input.contains("Release")) {
				
				//get the block to release
				char ch = input.charAt(8);
				
				//release the block of memory
				System.out.println("\nRelease " + ch);
				buddy.deallocation(ch);
				
				//display new memory allocation
				buddy.display();
			}
		}
	}

}