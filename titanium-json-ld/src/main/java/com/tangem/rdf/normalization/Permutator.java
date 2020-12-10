package com.tangem.rdf.normalization;

import com.tangem.rdf.RdfResource;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterate over all possible permutations of an array.
 *
 * @author Simon Greatrix on 06/10/2020.
 */
class Permutator implements Iterator<com.tangem.rdf.RdfResource[]> {

  /** The array we are permuting. */
  private final com.tangem.rdf.RdfResource[] array;

  /** Counts for Heap's algorithm. */
  private final short[] count;

  /** Does another permutation exist?. */
  private boolean nextExists = true;

  /** State for Heap's algorithm. */
  private int state = 0;


  Permutator(com.tangem.rdf.RdfResource[] input) {
    array = input.clone();
    count = new short[array.length];
  }


  @Override
  public boolean hasNext() {
    return nextExists;
  }


  @Override
  public com.tangem.rdf.RdfResource[] next() {
    if (!nextExists) {
      throw new NoSuchElementException();
    }

    com.tangem.rdf.RdfResource[] output = array.clone();

    // Implementation of Heap's Algorithm
    while (state < array.length) {
      if (count[state] < state) {
        if ((state & 1) == 0) {
          swap(0, state);
        } else {
          swap(count[state], state);
        }

        count[state]++;
        state = 0;

        return output;
      }

      count[state] = 0;
      state++;
    }

    nextExists = false;
    return output;
  }


  private void swap(int i, int j) {
    RdfResource t = array[i];
    array[i] = array[j];
    array[j] = t;
  }

}
