package mokk.nlp.bidictionary;



public class BiFrequency {
    public static void main(String[] args) throws Exception {
    	if(args.length < 3) {
    		System.err.println("usage: BiFrequency index-dir dict-file wordlist");
    		System.exit(-1);
    		
    	}
    	/*
    	LuceneBiCorpusSearcher searcher = new LuceneBiCorpusSearcher(args[0]);
    	
    	BiDictionaryReader dictReader = new BiDictionaryReader();
   	BiDictionary bidict =  dictReader.readFromText(args[1]);
    	
    	
    	BufferedReader r = new BufferedReader(
				new InputStreamReader(new FileInputStream(args[2]), "ISO-8859-2"));
    	
    	PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "ISO-8859-2"));
		String line ;
		
		while((line = r.readLine()) != null) {
			
			List trans = bidict.getRightTranslation(line);
			if(trans == null) {
				System.out.println(line + "nincs a sz?t?rban");
				continue;
			}
			System.out.println("sz?t?rban tal?ltam:");
			
			Iterator it = trans.iterator();
			while(it.hasNext()) {
				String h = (String) it.next();
				List result = null; // searcher.search(h, line, 20); 
				System.out.println(line + "\t" + h + "\t" + result.size());
			}
		}
    	searcher.close();
    	    */
    }

}
