package hu.mokk.hunglish.domain;

import java.util.ArrayList;
import java.util.List;

public class BisenState {

	/**
	 * Possible operations on <code>Bisen</code>s
	 * Each <code>State</code> is a node and
	 * Each <code>BisenOperation</code> is an edge 
	 * in the state graph <a>http://en.wikipedia.org/wiki/State_diagram</a> 
	 * 
	 * EDIT: N -> D
	 * DUPLUMFILTER: D -> I
	 * ADD2INDEX: I -> N
	 * DELETE: N -> E
	 * DELETEFROMINDEX: E -> N
	 * DELETEFROMINDEX: R -> I
	 * VOTE: N -> R
	 * ADD2INDEX, REINDEX: R -> N
	 * ERROR: [] -> Terminal 
	 * 
	 * @author bpgergo
	 *
	 */
	public enum BisenOperation {
	   ERROR, EDIT, DUPLUMFILTER, ADD2INDEX, DELETE, DELETEFROMINDEX, VOTE 
	}

	/**
	 * The possible states of a <code>Bisen</code>
	 * D: waiting for duplum filtering
	 * I: waiting for adding it to the index
	 * N: neutral state
	 * E: waiting for deleting it from the index
	 * R: wainting from reindexing
	 * O: neutral state with an Error
	 * X: neutral indexed state
	 * @author bpgergo
	 *
	 */
	public enum State {
	    D, I, N, E, R, O, X
	}
	
	
	public static List<State> waitingForOperation(BisenOperation op){
		List<State> result = new ArrayList<State>();
		if (BisenOperation.ADD2INDEX.equals(op)){
			result.add(State.I);
		} else if (BisenOperation.DELETEFROMINDEX.equals(op)){
			result.add(State.E);
			result.add(State.R);
		}
		return result;
	}
	
	
	public static State newState(Bisen bisen, BisenOperation op){
		State result = null;
		if (BisenOperation.DELETEFROMINDEX.equals(op)){
			result = State.N;
			if (State.R.toString().equals(bisen.getState())) {
				result = State.I;
			}
		} else if (BisenOperation.ERROR.equals(op)){
			result = State.O;
		} else if (BisenOperation.ADD2INDEX.equals(op)){
			result = State.X;
		}
		return result;
	}
	
}
