package hu.mokk.hunglish.job;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SystemCall{
	
	private static Log logger = LogFactory.getLog(SystemCall.class);
	
    public static int execute(String command) {
    	int result = 0;
        Runtime r = Runtime.getRuntime();

        try {
            Process p = r.exec(command);
            // Check for failure
            try {
                if (p.waitFor() != 0) {
                    result = p.exitValue();
                }
            } catch (InterruptedException e) {
            	logger.error("System call interupted.", e);
            } finally {
            }
        } catch (IOException e) {
        	logger.error("IO error in system call.", e);
        }
        return result;
     }
 }
 