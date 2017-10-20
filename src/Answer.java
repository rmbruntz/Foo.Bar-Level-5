import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public class Answer {
	
	
	
	public static String answer(String str_n) { 
		final String root2Str = "1.414213562373095048801688724209698078569671875376948073176679737990732478462107038850387534327641572735013846230912297024924836055850737212644121497099935831413222665927505592755799950501152782060571470109559971605970274534596862014728517418640889198609552329230484308714321450839762603"; 
		// from NASA. Won't work after some point beyond 10^100 (a string 1/4 the length failed 4 of the tests)
		final int preCalculatedCutOff = 3;
		BigInteger sumTo = new BigInteger(str_n);
		BigDecimal root2 = new BigDecimal(root2Str);		
		
		BigDecimal[] preCalculated = new BigDecimal[preCalculatedCutOff]; // pre-calculate a number of multiples of sqrt(2). 
		for (int i = 0; i < preCalculatedCutOff; i++) {
			preCalculated[i] = root2.multiply(new BigDecimal(i));
		}
		
		BigInteger[] preAdded = new BigInteger[preCalculatedCutOff];  // pre-calculate the sums for the first number of terms in the series
		BigInteger total = BigInteger.ZERO;
		for (int i = 0; i < preCalculatedCutOff; i++) {
			BigInteger toAdd = preCalculated[i].toBigInteger();
			total = total.add(toAdd);
			preAdded[i] = total;
		}
		
		BigDecimal lowestPosDelta = BigDecimal.ONE;
		BigInteger lowestPosIndex = BigInteger.ZERO;
		BigDecimal lowestNegDelta = BigDecimal.ONE;
		BigInteger lowestNegIndex = BigInteger.ZERO;
		
		// continually adds the values that are closest to an integer on each side (closest positive, closest negative) to generate new values with smaller deltas
		
		for (int i = 1; i < preCalculatedCutOff; i++) { // algorithm must be past a certain point (around when smallest distance to integer is .25 in both directions) to work,
			BigDecimal d = preCalculated[i];            // therefore the first few values will be generated manually
			d = d.subtract(new BigDecimal(Math.floor(d.doubleValue())));
			
			if (d.compareTo(new BigDecimal(0.5)) == 1) {
				d = d.subtract(BigDecimal.ONE);
				if (d.abs().compareTo(lowestNegDelta) == -1) {
					lowestNegDelta = d.abs();
					lowestNegIndex = new BigInteger(new Integer(i).toString());
				}
			} else {
				if (d.abs().compareTo(lowestPosDelta) == -1) {
					lowestPosDelta = d.abs();
					lowestPosIndex = new BigInteger(new Integer(i).toString());
				}
				
			}
		}
		
		
		ArrayList<Block> blocks = new ArrayList<Block>(); // dynamic: build blocks from the last two largest blocks. this method is guranteed to never push a value over 1, and modify its floor() value, 
													      // since it starts with a region where the smallest delta is at the end, and that delta is smaller than the smallest delta of opposite sign in the next region.
		
		// log(n) if you ignore decreasing speed of BigDecimal calculations
		while (blocks.isEmpty() || blocks.get(blocks.size()-1).size.compareTo(sumTo) <= 0) {
			boolean posSmaller = (lowestPosDelta.compareTo(lowestNegDelta) < 0);
			BigDecimal smallest = lowestPosDelta.min(lowestNegDelta); // smaller delta
			BigInteger smallestIndex = posSmaller? lowestPosIndex : lowestNegIndex; // index of smaller delta
			BigDecimal largest = lowestPosDelta.max(lowestNegDelta); // larger delta
			BigInteger largestIndex = posSmaller? lowestNegIndex : lowestPosIndex; // index of larger delta
			
			BigDecimal newDelta = largest.subtract(smallest); // new smallest delta after subtraction
			BigInteger newIndex = largestIndex.add(smallestIndex); // index of new smallest delta
			//Now find the size of the current block up to the new index
			BigInteger size = BigInteger.ZERO;
			boolean crosses1 = !posSmaller;

			size = size.add(getSize(smallestIndex, blocks, preAdded, preCalculatedCutOff)); // add block with smaller delta
			size = size.add(getSize(largestIndex, blocks, preAdded, preCalculatedCutOff)); // add block with larget delta
			size = size.add(getRectangle(largestIndex, smallestIndex, root2, crosses1)); // get rectangle, add 1 to height of rectangular part if adding the larger value crosses the value over 1
			
			// now sum the sizes and rectangles of the multiple (or single) smaller blocks
			
			if (!posSmaller) { // new delta will have the same sign as the largest
				lowestPosDelta = newDelta;
				lowestPosIndex = newIndex;
			} else {
				lowestNegDelta = newDelta;
				lowestNegIndex = newIndex;
				newDelta = newDelta.negate(); //invert newDelta for storage
			}
			
			
			blocks.add(new Block(newIndex, size, newDelta));
		}
		
		blocks.remove(blocks.size()-1);
		// block list is generated but the last block > problem size, so it is discarded
		
		BigInteger totalSize = BigInteger.ZERO;
		BigInteger numLeft = sumTo;
		BigDecimal curDelta = BigDecimal.ZERO;
		 // add largest blocks, then recursively fills in the remainder with a smaller block which is guranteed to have a smaller min distance to integer than the previous block
		// which means that no value will be pushed over an integer and yield a different floor() value.
		
		// log(n) if you again ignore things like BigInteger multiplication 
		while (!blocks.isEmpty() && numLeft.compareTo(blocks.get(0).size) >= 0) {
			Block biggest = getIndOrLower(numLeft, blocks);
			curDelta = curDelta.add(biggest.delta);
			boolean crosses1 = curDelta.compareTo(BigDecimal.ZERO) < 0;
			
			totalSize = totalSize.add(biggest.area);
			numLeft = numLeft.subtract(biggest.size);

			BigInteger rectSize = getRectangle(numLeft, biggest.size, root2, crosses1);
			totalSize = totalSize.add(rectSize);
			
			
		}
		
		// fill in the last bit with a pre-generated value from the beginning
		totalSize = totalSize.add(preAdded[numLeft.intValue()]);
		
		
		
		
		
		
		return totalSize.toString();
		// yeet!
    } 
	
	static BigInteger getRectangle(BigInteger width, BigInteger index, BigDecimal root2, boolean add1) { // computes the rectangle to fill in the space to the right of the current block and under the new one, adding a row if the previous block ended just below an integer
		BigDecimal heightBeforeFloor = new BigDecimal(index.toString()).multiply(root2);
		BigInteger actualHeight = heightBeforeFloor.toBigInteger();
		if (add1) actualHeight = actualHeight.add(BigInteger.ONE);
		return actualHeight.multiply(width);
	}
	
	static BigInteger getSize(BigInteger ind, ArrayList<Block> blocks, BigInteger[] preAdded, int cutOff) { // gets the size of a given block
		if (ind.compareTo(new BigInteger(Integer.toString(cutOff))) < 0) {
			return preAdded[ind.intValue()];
		} else {
			return getIndOrLower(ind, blocks).area;
		}
		
	}
	
	static Block getIndOrLower(BigInteger ind, ArrayList<Block> blocks) { // gets the index from blocks, or returns the next lowest one
		Block toReturn = null;
		for (Block b : blocks) {
			if (b.size.compareTo(ind) > 0) return toReturn;
			toReturn = b;
		}
		return toReturn;
	}
	
	static class Block {
		BigInteger size;
		BigInteger area;
		BigDecimal delta;
		
		public Block(BigInteger size, BigInteger area, BigDecimal delta) {
			this.size = size;
			this.area = area;
			this.delta = delta;
		}
		
		
	}
	
	
	public static void main(String[] args) {
		
		
		System.out.println(answer("1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
	}
	
}
