package hu.mokk.hunglish.job;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SystemCall{
	
	private static String defaultCommand = "/big3/Work/HunglishMondattar/deployment/harness_cronjob.sh";
	// private static String defaultCommand = "/bin/bash /big3/Work/HunglishMondattar/hunglish-webapp/src/main/python/harness_cronjob.sh";
	
	private static Log logger = LogFactory.getLog(SystemCall.class);
	
    public static int execute(String command) {
    	int result = 0;
    	if (command == null){
    		logger.error("Command is null, using default command: "+defaultCommand);
    		command = defaultCommand;
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
            } finally {
            }
        } catch (IOException e) {
        	logger.error("IO error in system call.", e);
        }
        return result;
     }
 }
 