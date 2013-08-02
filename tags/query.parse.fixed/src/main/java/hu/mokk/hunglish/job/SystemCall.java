package hu.mokk.hunglish.job;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SystemCall{
	
	private static Log logger = LogFactory.getLog(SystemCall.class);
	
    public static int execute(String command) {
    	int result = 0;
    	if (command == null){
    		throw new RuntimeException("harness command is null");
    	}
        Runtime r = Runtime.getRuntime();

        try {
            Process p = r.exec(command);
            try {
                if (p.waitFor() != 0) {
                    result = p.exitValue();
                }
            } catch (InterruptedException e) {
            	logger.error("System call interupted.", e);
            	//TODO
            } finally {
            	//TODO
            }
        } catch (IOException e) {
        	logger.error("IO error in system call.", e);
        	//TODO
        }
        return result;
     }
 }
 