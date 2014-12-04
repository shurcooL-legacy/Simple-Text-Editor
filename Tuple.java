/*
 * Tuple.java
 *
 * Created on November 29, 2007, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author shuralydm
 */
public class Tuple<E> {

	private E first;
    private E second;

    /** Creates a new instance of Tuple */
    public Tuple(E first, E second) {
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return this.first;
    }

    public E getSecond() {
        return this.second;
    }

    public void setFirst(E first) {
        this.first = first;
    }

    public void setSecond(E second) {
        this.second = second;
    }

	public boolean doesOverlap(Tuple<Integer> other)
	{
		return !((other.getSecond() <= (Integer)getFirst()) || ((Integer)getSecond() <= other.getFirst()));
	}

	@Override
	public String toString() {
		return "(" + getFirst() + ", " + getSecond() + ")";
	}

	public static Tuple<Integer> valueOfInteger(String tupleString) {
		String[] tuple = tupleString.split("\\D");
		return new Tuple<Integer>(Integer.valueOf(tuple[1]), Integer.valueOf(tuple[3]));
	}

}
