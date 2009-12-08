package mokk.nlp.irutil.lucene.analysis;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

import net.sf.jhunlang.jmorph.lemma.Lemma;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;

import org.apache.commons.collections.set.CompositeSet.SetMutator;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * A {@link TokenFilter} that decomposes compound words found in many Germanic
 * languages.
 * <p>
 * "Donaudampfschiff" becomes Donau, dampf, schiff so that you can find
 * "Donaudampfschiff" even when you only enter "schiff". It uses a brute-force
 * algorithm to achieve this.
 * </p>
 */
public class CompoundStemmerTokenFilter extends CompoundWordTokenFilterBase {

	protected Lemmatizer lemmatizer = null;

	protected boolean returnOOVOrig;

	protected boolean returnPOS;

	protected boolean returnOrig;
	
	private static int MIN_WORD_SIZE = 3;

	/**
	 * 
	 * @param input
	 *            the {@link TokenStream} to process
	 */
	public CompoundStemmerTokenFilter(TokenStream input, Lemmatizer lemmatizer,
			boolean returnOrig, boolean returnOOVOrig, boolean returnPOS) {
		super(input);
		this.returnOrig = returnOrig;
		this.lemmatizer = lemmatizer;
		this.returnOOVOrig = returnOOVOrig;
		this.returnPOS = returnPOS;

	}

	private void add(Object token){
//System.out.println("#token:"+token.toString());
		tokens.add(token);
	}
	
	@Override
	protected void decomposeInternal(final Token token) {
//System.out.println("### incoming token:"+token.toString());
		
		// TODO FIXME Only words longer than minWordSize get processed ?
		if (token.termLength() < this.MIN_WORD_SIZE) {
			return;
		}
		//this was a nasty bug: token.termBuffer()
		String origWord = new String(token.term());
//System.out.println("### incoming origWord:"+origWord);
		List<Lemma> lemmas = lemmatizer.lemmatize(origWord);
		// az eredeti tokent csak akkor adjuk vissza, ha a szo ismeretlen
		// es kertek
		if ((lemmas.size() == 0)){
//System.out.println("%%%%%% lemma size == 0 origWord:"+origWord);		    
			if (returnOOVOrig || returnOrig){
		        add(token.clone());
		    }
		} else {
//System.out.println("YYYYYY lemma size > 0 origWord:"+origWord);			
			if (returnOrig){
				add(token.clone());
			}
			boolean isFirst = true;
			for (Lemma lemma : lemmas){
				Token stemToken = null;
				String stemmedText;
				if(returnPOS) {
				    stemmedText = lemma.getWord() + "/" + lemma.getPOS(); 
				} else {
					stemmedText = lemma.getWord();
				}
//System.out.println("$$$temmed:"+stemmedText);				
			    stemToken = new Token(stemmedText, 
						   token.startOffset(), 
						   token.endOffset(), 
						   token.type());
				
				// put the token representing the stem to the same position as
				// the original word if the orig word won't be returned
				if(returnOrig || !isFirst) {
				    stemToken.setPositionIncrement(0);
				}
				// if the original token is the same as the stemmed text
				// and the origian token was returned as well
				// then no need to return the stemmed token
				if (!(returnOrig && origWord.toLowerCase().equals(stemmedText.toLowerCase()))){
					add(stemToken);
				} 
				isFirst = false;
			}
		}
		
	}
}
