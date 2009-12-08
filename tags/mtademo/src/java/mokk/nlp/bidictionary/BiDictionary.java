/*
 * Created on Dec 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bidictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hp
 *
 * K?tnyelv? sz?t?r, mely a baloldali ?s a jobb oldali szavak alapj?n is kereshet?.
 */
public class BiDictionary {

	/**
	 * A baloldali termekkel indexelt sz?t?r. Minden baloldali termhez tartozik egy sz?lista.
	 */
	protected HashMap left2right;
	
	
	protected HashMap right2left;
	
	public BiDictionary () {
		left2right = new HashMap();
		right2left = new HashMap();
	}
	/**
	 * Hozz?ad egy sz?p?rt a sz?t?rhoz. Felveszi a balr?l jobbra ?s
	 * jobbr?l balra ford?t?st is.
	 * @param left A baloldali sz? (pl. angol)
	 * @param right A jobboldali sz? (pl. magyar)
	 */
	public void addTranslation(String left, String right) {
		addWordPair(left2right, left, right);
		addWordPair(right2left, right, left);
	}
	
	private void addWordPair(Map map, String key, String value) {
		List valueList = (List) map.get(key);
		if(valueList == null) {
			valueList = new LinkedList();
		}
		
		valueList.add(value);
		map.put(key, valueList);
	
	}
	/**
	 * Megadja, hogy h?ny k?l?nb?z? jobb oldali term van a sz?t?rban.
	 * @return 
	 */
	public int getRightCount() {
		return right2left.size();
	}
	
	/**
	 * Megadja, hogy h?ny k?l?nb?z? bal oldali term van a sz?t?rban.
	 * @return 
	 */
	public int getLeftCount() {
		return left2right.size();
	}
	/**
	 * Egy baloldali termhez tartoz? termeket adja vissza.
	 * @param left Sz?, aminek ford?t?sait keress?k
	 * @return Jobb oldali szavak List-?ja t?mbje, amiben minden String
	 */
	public List getRightTranslation(String left) {
		List rightList = (List) left2right.get(left);
		
		return rightList;
		
	}
}
