package main.java.bgu.spl.mics.impl;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * this is an implementation of Iterable created so we can support round -robin is send request
 *
 * @param <E>
 */
public class myList<E> implements Iterable<E> {

	LinkedList<E> myList;
	int size;
	int index;

	/**
	 * constructs myList with a new LinkedList
	 * @size - equivalent to the size of the LinkedList
	 * @index - so the iterator could return to the beginning of the list
	 */
	public myList() {
		myList = new LinkedList<E>();
		size = 0;
		index=0;
	}

	public void add(E arg0) {
		myList.add(arg0);
		size++;
	}

	public boolean contains(E arg0) {
		myList.contains(arg0);
		return false;
	}

	public E get() {
		if (!myList.isEmpty())
			return myList.getFirst();
		return null;
	}

	public E get(int index) {
		if (index < size)
			return myList.get(index);
		return null;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	/* return our private class myIterator that supports the round-robin manner
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<E> iterator() {
		return new myIterator<E>(); 
	}
	
	private class myIterator<E> implements Iterator<E>{
	public boolean hasNext() {
		return size>0;
	}

	public synchronized E next() {
		if(!hasNext())
			return null;
		E temp = (E) myList.get(index);
		index = (index + 1) % size();
		return temp;
	}
}

	public void remove(E e) { 								
		if (!myList.isEmpty()){
			if(myList.remove(e)){
			   size--;
			   if(index>0)
				   index--;}
			}

	}

	public int size() {
		return size;
	}
	
	

}