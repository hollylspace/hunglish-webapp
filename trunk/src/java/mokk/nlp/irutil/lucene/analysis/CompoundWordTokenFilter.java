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
public class CompoundWordTokenFilter extends CompoundWordTokenFilterBase {

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
	public CompoundWordTokenFilter(TokenStream input, Lemmatizer lemmatizer,
			boolean returnOrig, boolean returnOOVOrig, boolean returnPOS) {
		super(input);
		this.returnOrig = returnOrig;
		this.lemmatizer = lemmatizer;
		this.returnOOVOrig = returnOOVOrig;
		this.returnPOS = returnPOS;

	}

	@Override
	protected void decomposeInternal(final Token token) {
		// TODO FIXME Only words longer than minWordSize get processed ?
		if (token.termLength() < this.MIN_WORD_SIZE) {
			return;
		}

		List<Lemma> lemmas = lemmatizer.lemmatize(new String(token.termBuffer()));		
		// az eredeti tokent csak akkor adjuk vissza, ha a szo ismeretlen
		// es kertek
		if ((lemmas.size() == 0) 
		    && (returnOOVOrig || returnOrig)) {
		        tokens.add(token.clone());
		} else {
			if (returnOrig){
				tokens.add(token.clone());
			}
			boolean isFirst = true;
			for (Lemma lemma : lemmas){
				Token stemToken = null;
				String text;
				if(returnPOS) {
				    text = lemma.getWord() + "/" + lemma.getPOS(); 
				} else {
					text = lemma.getWord();
				}
			    stemToken = new Token(text, 
						   token.startOffset(), 
						   token.endOffset(), 
						   token.type());
				
				// put the token representing the stem to the same position as
				// the original word if the orig word won't be returned
				if(returnOrig || !isFirst) {
				    stemToken.setPositionIncrement(0);
				}
				tokens.add(stemToken);
				isFirst = false;
			}
		}
		
	}
}
