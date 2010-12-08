/**
 * 
 */
package hu.mokk.hunglish.jmorph;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.analysis.StemmerAnalyzer;
import hu.mokk.hunglish.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jhunlang.jmorph.analysis.Analyser;
import net.sf.jhunlang.jmorph.analysis.AnalyserContext;
import net.sf.jhunlang.jmorph.analysis.AnalyserControl;
import net.sf.jhunlang.jmorph.factory.Definition;
import net.sf.jhunlang.jmorph.factory.JMorphFactory;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;
import net.sf.jhunlang.jmorph.parser.ParseException;
import net.sf.jhunlang.jmorph.sample.AnalyserConfig;
import net.sf.jhunlang.jmorph.sword.parser.EnglishAffixReader;
import net.sf.jhunlang.jmorph.sword.parser.EnglishReader;
import net.sf.jhunlang.jmorph.sword.parser.SwordAffixReader;
import net.sf.jhunlang.jmorph.sword.parser.SwordReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * This class will provide the {@link Analyzer} for 
 * the indexer and the searcher.
 * 
 * We use {@link PerFieldAnalyzerWrapper} to assign 
 * a Lucene {@link StandardAnalyzer} to the non-stemmed fields,
 * and for the stemmed fields on the other hand, 
 * we assign Lucene {@link StemmerAnalyzer}
 * @author bpgergo
 * 
 */
@Configurable
public class AnalyzerProvider {

	private String resourceEncodingEn;
	private String resourceEncodingHu;
	private String huAff;
	private String huDic;
	private String huDerivatives;
	private String huCompounds;
	private int huRecursionDepth;
	private String enAff;
	private String enDic;
	private int enRecursionDepth;

	/*******************************************/
	/**
	 * morphological analyser for Hungarian
	 */
	private Analyser huAnalyser;
	/**
	 * morphological analyser for English
	 */
	private Analyser enAnalyser;

	
	private Map<String, LemmatizerWrapper> lemmatizerMap;
	
	/**
	 * Lucene analyzer.
	 * This will be a per field analyzer wrapper
	 */
	Analyzer analyzer;

	/*******************************************/
	/*
	private void convertPaths(){
		huAff = Utils.convertPath(getClass(), huAff);
		huDic = Utils.convertPath(getClass(), huDic);
		huDerivatives = Utils.convertPath(getClass(), huDerivatives);
		huCompounds = Utils.convertPath(getClass(), huCompounds);
		enAff = Utils.convertPath(getClass(), enAff);
		enDic = Utils.convertPath(getClass(), enDic);
	} //*/
	
	private void initHuAnalyser() throws IOException, IllegalAccessException,
			InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		AnalyserConfig acHu = new AnalyserConfig();
		Definition affixDef = acHu.createDefinition(huAff, resourceEncodingHu,
				SwordAffixReader.class);
		Definition dicDef = acHu.createDefinition(huDic, resourceEncodingHu,
				SwordReader.class);
		acHu.setDerivativeFile(affixDef, huDerivatives);
		acHu.setRecursionDepth(affixDef, huRecursionDepth);
		acHu.setCompoundFile(affixDef, huCompounds);
		JMorphFactory jf = new JMorphFactory();
		huAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	private void initEnAnalyser() throws IOException, IllegalAccessException,
			InstantiationException, ParseException {
		AnalyserConfig acEn = new AnalyserConfig();
		Definition affixDef = acEn.createDefinition(enAff, resourceEncodingEn,
				EnglishAffixReader.class);
		Definition dicDef = acEn.createDefinition(enDic, resourceEncodingEn,
				EnglishReader.class);
		acEn.setRecursionDepth(affixDef, enRecursionDepth);
		JMorphFactory jf = new JMorphFactory();
		enAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	/**
	 * This method initialize the Analyzers.
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ParseException
	 */
	public void initAnalyzerProvider() throws IOException, IllegalAccessException,
			InstantiationException, ParseException {

		try {
			lemmatizerMap = new HashMap<String, LemmatizerWrapper>();
			//convertPaths();
			initHuAnalyser();
			initEnAnalyser();
			AnalyserControl acHu = new AnalyserControl(
					AnalyserControl.ALL_COMPOUNDS);
			AnalyserContext analyserContextHu = new AnalyserContext(acHu);
			boolean huStripDerivates = true;
			Lemmatizer huLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
					huAnalyser, huStripDerivates, analyserContextHu);

			AnalyserControl acEn = new AnalyserControl(
					AnalyserControl.ALL_COMPOUNDS);
			AnalyserContext analyserContextEn = new AnalyserContext(acEn);
			boolean enStripDerivates = true;
			Lemmatizer enLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
					enAnalyser, enStripDerivates, analyserContextEn);

			LemmatizerWrapper huLemmatizerWrapper = new LemmatizerWrapper(huLemmatizer,false/*returnOOVOrig*/,true/*returnOrig*/,false/*returnPOS*/);
			lemmatizerMap.put(Bisen.huSentenceStemmedFieldName, huLemmatizerWrapper);

			LemmatizerWrapper enLemmatizerWrapper = new LemmatizerWrapper(enLemmatizer,false/*returnOOVOrig*/,true/*returnOrig*/,false/*returnPOS*/);
			lemmatizerMap.put(Bisen.enSentenceStemmedFieldName, enLemmatizerWrapper);

			StemmerAnalyzer stemmerAnalyzer = new StemmerAnalyzer(Version.LUCENE_30, lemmatizerMap);
			
			PerFieldAnalyzerWrapper analyzerWrapper =
			      new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_CURRENT));
			analyzerWrapper.addAnalyzer(Bisen.huSentenceStemmedFieldName, stemmerAnalyzer);
			analyzerWrapper.addAnalyzer(Bisen.enSentenceStemmedFieldName, stemmerAnalyzer);
			
			this.analyzer = analyzerWrapper;
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (ParseException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		}

	}

	/*******************************************/
	public Analyzer getAnalyzer() {
		return analyzer;
	}
	public Map<String, LemmatizerWrapper> getLemmatizerMap() {
		return lemmatizerMap;
	}

	/*******************************************/
	public void setHuAff(String huAff) {
		this.huAff = huAff;
	}

	public void setHuDic(String huDic) {
		this.huDic = huDic;
	}

	public void setHuDerivatives(String huDerivatives) {
		this.huDerivatives = huDerivatives;
	}

	public void setHuCompounds(String huCompounds) {
		this.huCompounds = huCompounds;
	}

	public void setHuRecursionDepth(int huRecursionDepth) {
		this.huRecursionDepth = huRecursionDepth;
	}

	public void setEnAff(String enAff) {
		this.enAff = enAff;
	}

	public void setEnDic(String enDic) {
		this.enDic = enDic;
	}

	public void setEnRecursionDepth(int enRecursionDepth) {
		this.enRecursionDepth = enRecursionDepth;
	}

	public void setResourceEncodingEn(String resourceEncodingEn) {
		this.resourceEncodingEn = resourceEncodingEn;
	}

	public void setResourceEncodingHu(String resourceEncodingHu) {
		this.resourceEncodingHu = resourceEncodingHu;
	}

	
	
}
