/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Mar 23, 2005
 *
 */

package mokk.nlp.irutil.io;



/*
 * @author hp
 *
 * A dokumentumokat beolvaso DocumentSource atadja a DocumentHandler-nek a dokumentumokat
 * Ha barmi hibat csinal az a szerencsetlen, akkor ezt dobja.
 */
public class ProcessingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3258129159078426933L;

    /**
     * 
     */
    public ProcessingException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public ProcessingException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     */
    public ProcessingException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public ProcessingException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
