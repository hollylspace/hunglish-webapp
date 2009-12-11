/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 9, 2005
 *
 */

package mokk.nlp.dictweb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

/*
 * Process the incoming HTTP request.
 * 
 * @author hp
 *
 *
 */
public interface  RequestHandler 

{
    public String ROLE = RequestHandler.class.getName();
    
    public abstract String handleRequest(HttpServletRequest request,
            HttpServletResponse response, Context context) throws Exception ;
}
