import java.io.*;
import java.util.*;

import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.classify.Instance;
import edu.cmu.minorthird.text.Span;
import edu.cmu.minorthird.text.TextLabels;
import edu.cmu.minorthird.text.learn.SpanFE;
import edu.cmu.minorthird.util.gui.ViewerFrame;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;


public class CustomFE {
	private static Map<String,String> phonemicSpelling ;
	public static void loadPhonemicSpellings(String dictFileName){
		try{
			phonemicSpelling  = new HashMap<String,String>();
			BufferedReader reader = new BufferedReader(new FileReader(dictFileName));
			String str;
			while((str=reader.readLine())!=null)
			{
				String word = str.split("\\s+")[0];
				String phone_spelling = str.substring(word.length());
				phonemicSpelling.put(word, phone_spelling.trim());
			}
			reader.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	public static class PhoneFE extends SpanFE{
		// prepare the feature extractor
		protected int windowSize = 2;
		protected boolean useCurrentSpan = false;
		protected boolean usePhoneUnigrams = true;
		protected boolean usePhoneBigrams = false;
		protected boolean usePhoneTypes = true;
		protected boolean usePhoneTypePattern = true;
		
		public PhoneFE(int windowSize, boolean useCurrentSpan,boolean usePhoneUnigrams,
				boolean usePhoneBigrams, boolean usePhoneTypes, boolean usePhoneTypePattern){
			this.windowSize = windowSize;
			this.useCurrentSpan = usePhoneBigrams ? true : useCurrentSpan;
			this.usePhoneUnigrams = usePhoneUnigrams;
			this.usePhoneBigrams = usePhoneBigrams;
			this.usePhoneTypes = usePhoneTypes;
			this.usePhoneTypePattern = usePhoneTypePattern;
		}
		
		public void extractFeatures(TextLabels labels, Span span){
			//System.out.println("Currently in document: "+span.getDocumentId());
			
			ArrayList<String> tokens = getTokenSequence(span);
			if (usePhoneUnigrams){
				HashMap<String, Integer> phoneUnigramCounts = getPhoneUnigramCounts(tokens);
				for (String phone : phoneUnigramCounts.keySet())
					instance.addNumeric(new Feature(phone), phoneUnigramCounts.get(phone));
			}
			if (usePhoneBigrams){
				HashMap<String, Integer> phoneBigramCounts = getPhoneBigramCounts(tokens);
				for (String phone : phoneBigramCounts.keySet())
					instance.addNumeric(new Feature(phone), phoneBigramCounts.get(phone));
			}
			if (usePhoneTypes){
				for (String token : tokens){
					ArrayList<String> phoneTypeSequences = getPhoneTypeSequences(token);
					for (String sequence : phoneTypeSequences)
						instance.addBinary(new Feature(sequence));
				}
			}
			if (usePhoneTypePattern){
				for (String token : tokens){
					ArrayList<String> phoneTypePatterns = getPhoneTypePattern(token);
					for (String pattern : phoneTypePatterns)
						instance.addBinary(new Feature(pattern));
				}
			}
		}
		
		/** 
		 * given a span, return the sequence of token in the window surrounding it with radius windowSize
		 * 
		 * @param span  the Span in consideration
		 * @return ArrayList of tokens in the desired window
		 */
		public ArrayList<String> getTokenSequence(Span span)
		{
			ArrayList<String> tokens = new ArrayList<String>();
			//add token to list of tokens in left to right order
			for (int i = windowSize; i > 0; i--){
				String token = from(span).left().subSpan(-i, 1).getSpan().asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token);
			}
			if (useCurrentSpan){
				for (int i = 0; i < span.size(); i++){
					String token = span.subSpan(i, 1).asString();
					if (!token.equals("_UNALIGNED_") && !token.equals(""))
						tokens.add(token);
				}
			}
			for (int i = 0; i < windowSize; i ++){
				String token = from(span).right().subSpan(i, 1).getSpan().asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token);
			}
			
			return tokens;
		}
		
		/** 
		 * given a sequence of tokens, return the phone unigrams in that sequence, together with their counts
		 * 
		 * @param tokens  ArrayList of String of tokens being analyzed
		 * @return a map from the phone name (String) to its counts (Integer)
		 */
		private HashMap<String, Integer> getPhoneUnigramCounts(ArrayList<String> tokens){
			HashMap<String, Integer> phoneUnigramCounts = new HashMap<String, Integer>();
			for (String token : tokens){
				String tokenPhones = phonemicSpelling.get(token);
				//System.out.println(token+":"+tokenPhones);
				for (String phoneUnigram : tokenPhones.split("\\s+")){
					int count = !phoneUnigramCounts.containsKey(phoneUnigram) ? 0 : 
						phoneUnigramCounts.get(phoneUnigram);
					phoneUnigramCounts.put(phoneUnigram, count+1); 
				}
			} 
			return phoneUnigramCounts;
		}
		
		/** 
		 * given a sequence of tokens, return the phone bigrams in that sequence, together with their counts
		 * 
		 * @param tokens  ArrayList of String of tokens being analyzed
		 * @return a map from the phone bigram (String) to its counts (Integer)
		 */
		private HashMap<String, Integer> getPhoneBigramCounts(ArrayList<String> tokens){
			HashMap<String, Integer> phoneBigramCounts = new HashMap<String, Integer>();
			ArrayList<String> phoneUnigrams = new ArrayList<String>();
			for (String token : tokens){
				String tokenPhones = phonemicSpelling.get(token);
				for (String phoneUnigram : tokenPhones.split("\\s+"))
					phoneUnigrams.add(phoneUnigram);
			}
			for (int i = 0; i < phoneUnigrams.size() - 1; i++){
				String phoneBigram = phoneUnigrams.get(i)+ " " + phoneUnigrams.get(i+1);
				int count = !phoneBigramCounts.containsKey(phoneBigram) ? 0 :
					phoneBigramCounts.get(phoneBigram);
				phoneBigramCounts.put(phoneBigram, count+1);
			}
			return phoneBigramCounts;
		}
		
		/** 
		 * given a token, produce the different phone type patterns that can be produced
		 * 
		 * @param token  a String for the token
		 * @return ArrayList of phone type sequences, each produced using a partition of phones into phone classes
		 */
		private ArrayList<String> getPhoneTypeSequences(String token){
			String tokenPhones = phonemicSpelling.get(token);
			tokenPhones += " ";
			ArrayList<String>results = new ArrayList<String>();
			//each "partition" is a partitioning of the set of phones into equivalence phone classes
			//one example of a partition is {"vowel", "consonant"}
			String[][] partitions = getPartitions();
			for (String[] partition : partitions){
				String currentResult = tokenPhones;
				// for each phone type in the partition, replace all phones of that type
				// with the type name
				for (String type : partition){
					String[] phoneTypeAssoc = getPhoneTypeAssoc(type);
					currentResult = currentResult.replaceAll(phoneTypeAssoc[0], phoneTypeAssoc[1]);
				}
				results.add(currentResult);
			}
			return results;
		}
		
		/** 
		 * given a token, produce the different phone type patterns that can be produced
		 * 
		 * @param token  a String for the token
		 * @return ArrayList of phoneTypePatterns, each produced using a partition of phones into phone classes
		 */
		private ArrayList<String> getPhoneTypePattern(String token){
			String tokenPhones = phonemicSpelling.get(token);
			tokenPhones += " ";
			ArrayList<String> results = new ArrayList<String>();
			//each "partition" is a partitioning of the set of phones into equivalence phone classes
			//one example of a partition is {"vowel", "consonant"}
			String[][] partitions = getPartitions();
			for (String[] partition : partitions){
				String currentResult = tokenPhones;
				// for each phone type in the partition, replace all sequences of phones of that type
				// with the corresponding regular expression, e.g. "phone" to "c+v+c+v+", 
				// where c is short for "consonant", v is short for "vowel"
				for (String type : partition){
					String[] phoneTypeAssoc = getPhoneTypeAssoc(type);
					currentResult = currentResult.replaceAll(phoneTypeAssoc[0]+"+", phoneTypeAssoc[1]+"+");
				}
				results.add(currentResult);
			}
			return results;
		}
		
		/** 
		 * each "partition" is a partitioning of the set of phones into equivalence phone classes
		 * one example of a partition is {"vowel", "consonant"}
		 * 
		 * @return array of partitions, each partition represented as an array of strings
		 */
		private String[][] getPartitions(){
			String[] partition1 = {"vowel", "consonant"}; 
			String[] partition2 = {"temp1", "temp2", "temp3", "temp4", "temp5", "temp6", "temp7",
					"temp8", "temp9", "temp10", "temp11", "temp12", "temp13", "temp14"};
			String[] partition3 = {"non-sibilant", "sibilant","vowel", "sonorant"};
			String[] partition4 = {"non-sibilant", "sibilant","sonorant","vowel-front","vowel-mid","vowel-back",};
			String[] partition5 = {"glide","nasal","plosive","fricative","approximant","trill","glide","vowel-front","vowel-mid","vowel-back"};
			
			String[][] results = {partition1, partition2};
			return results;
		}
		/** 
		 * for each phone class (type) supplied, return the phones in that class and the symbol 
		 * to be used for that class
		 * 
		 * @param type  String that tells the name of the phone class
		 * @return array of 2 strings, the first of which is the regular expression used to find a phone in this class, the second is the symbol used for the class
		 */
		private String[] getPhoneTypeAssoc(String type){
			String surfacePattern = "";
			String typePattern = "";
			if (type.equals("vowel")){
				surfacePattern = "((iy|ih|eh|ae|aa|er|ah|ax|ao|uw|uh|ow|axr|ax-h) )";
				typePattern = "v";
			}
			else if (type.equals("consonant")){
				surfacePattern = "((b|d|g|p|q|t|k|dx|bcl|dcl|gcl|pcl|tcl|kcl|jh|ch|z|zh|" +
						"v|dh|s|sh|f|v|th|m|n|nx|ng|em|en|eng|l|r|y|w|el|hh|hv) )";
				typePattern = "c";
			}
			else if (type.equals("temp1")){
				surfacePattern = "((b|p) )";
				typePattern = "b";
			}
			else if (type.equals("temp2")){
				surfacePattern = "((d|t|dx) )";
				typePattern = "d";
			}
			else if (type.equals("temp3")){
				surfacePattern = "((g|k|q) )";
				typePattern = "g";
			}
			else if (type.equals("temp4")){
				surfacePattern = "((jh|ch|) )";
				typePattern = "jh";
			}
			else if (type.equals("temp5")){
				surfacePattern = "((s|sh|z|zh) )";
				typePattern = "s";
			}
			else if (type.equals("temp6")){
				surfacePattern = "((dh|th) )";
				typePattern = "dh";
			}
			else if (type.equals("temp7")){
				surfacePattern = "((f|v) )";
				typePattern = "f";
			}
			else if (type.equals("temp8")){
				surfacePattern = "((l|r|ely|w) )";
				typePattern = "l";
			}
			else if (type.equals("temp9")){
				surfacePattern = "((m|n|nx|ng|em|en|eng) )";
				typePattern = "m";
			}
			else if (type.equals("temp10")){
				surfacePattern = "((hh|hv) )";
				typePattern = "hh";
			}
			else if (type.equals("temp11")){
				surfacePattern = "((iy|ih|eh|ae) )";
				typePattern = "iy";
			}
			else if (type.equals("temp12")){
				surfacePattern = "((aa|er|ah|ax|ao) )";
				typePattern = "aa";
			}
			else if (type.equals("temp13")){
				surfacePattern = "((uw|uh|ow) )";
				typePattern = "uw";
			}
			else if (type.equals("temp14")){
				surfacePattern = "((axr|ax-h) )";
				typePattern = "axr";
			}
			//TODO: add more phone classes
			
			String[] patternAssoc = {surfacePattern, typePattern};
			return patternAssoc;
		}
	}
	
	public static class ParserFE extends SpanFE{
		
		protected int windowSize = 3;
		protected boolean useCurrentSpan = true;
		
		protected LexicalizedParser lp;
		public ParserFE()
		{
			lp = new LexicalizedParser("lib/parser/englishPCFG.ser.gz");
		    lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

		}
		public void setWindowSize(int n){
			windowSize = n;
		}
		public void setUseCurrentSpan(boolean useCurrentSpan){
			this.useCurrentSpan = useCurrentSpan;
		}
		public void extractFeatures(TextLabels labels, Span span)
		{   
			from(span).tokens().emit();
			from(span).left().subSpan(- windowSize, windowSize).emit();
			from(span).right().subSpan(0, windowSize).emit();
			
			ArrayList<String> tokens = getTokenSequence(span);
			String parseStr = getParse(tokens);
			
			
			if(parseStr.startsWith("(ROOT (NP "))
			{
				System.out.println(parseStr);
				instance.addBinary(new Feature("NP"));
			}
			
			
		}
		private String getParse(ArrayList<String> phrase)
		{
			
		    //String[] sent = { "This", "is", "an", "easy", "sentence", "." };
		    //sent = phrase.split(" ");
		    Tree parse = (Tree) lp.apply(phrase);
		    //parse.pennPrint();
		    //System.out.println();

		    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		    Collection tdl = gs.typedDependenciesCollapsed();
		    //System.out.println(tdl);
		    //System.out.println();

		    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
		    //tp.printTree(parse);
		    
		    return parse.flatten().toString();
		}
		private ArrayList<String> getTokenSequence(Span span)
		{
			ArrayList<String> tokens = new ArrayList<String>();
			//add token to list of tokens in left to right order
			for (int i = windowSize; i > 0; i--){
				String token = from(span).left().subSpan(-i, 1).getSpan().asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token.toLowerCase());
			}
			if (useCurrentSpan){
				for (int i = 0; i < span.size(); i++){
					String token = span.subSpan(i, 1).asString();
					if (!token.equals("_UNALIGNED_") && !token.equals(""))
						tokens.add(token.toLowerCase());
				}
			}
			for (int i = 0; i < windowSize; i ++){
				String token = from(span).right().subSpan(i, 1).getSpan().asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token.toLowerCase());
			}
			
			return tokens;
		}
		
	}

	public static class CompositeFE extends SpanFE{
		private ArrayList<SpanFE> featureList;
		public CompositeFE(){
			featureList = new ArrayList<SpanFE>();
		}
		public void addFeature(SpanFE fe){
			featureList.add(fe);
		}
		public void extractFeatures(TextLabels labels, Span span){
			for (SpanFE fe : featureList){
				Instance inst = fe.extractInstance(labels, span);
				Iterator<Feature> binaryIt = inst.binaryFeatureIterator();
				Iterator<Feature> numericIt = inst.numericFeatureIterator();
				while (binaryIt.hasNext()){
					instance.addBinary(binaryIt.next());
				}
				while (numericIt.hasNext()){
					Feature f = numericIt.next();
					instance.addNumeric(f, inst.getWeight(f));
				}
			}
		}
	}
}