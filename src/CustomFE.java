import java.io.*;
import java.util.*;

import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.text.Span;
import edu.cmu.minorthird.text.TextLabels;
import edu.cmu.minorthird.text.learn.SpanFE;
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
		protected boolean usePhoneTypePattern = true;
		
		public PhoneFE(int windowSize, boolean useCurrentSpan,boolean usePhoneUnigrams,
				boolean usePhoneBigrams, boolean usePhoneTypePattern){
			this.windowSize = windowSize;
			this.useCurrentSpan = usePhoneBigrams ? true : useCurrentSpan;
			this.usePhoneUnigrams = usePhoneUnigrams;
			this.usePhoneBigrams = usePhoneBigrams;
			this.usePhoneTypePattern = usePhoneTypePattern;
		}
		
		public void extractFeatures(TextLabels labels, Span span){
			// add bag of words for all tokens in this span and in the surrounding
			// window of size windowSize
			from(span).tokens().emit();
			from(span).left().subSpan(- windowSize, windowSize).emit();
			from(span).right().subSpan(0, windowSize).emit();
			
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
			if (usePhoneTypePattern){
				for (String token : tokens){
					ArrayList<String> phoneTypePatterns = getPhoneTypePattern(token);
					for (String pattern : phoneTypePatterns)
						instance.addBinary(new Feature(pattern));
				}
			}
		}
		
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
		private ArrayList<String> getPhoneTypePattern(String token){
			String tokenPhones = phonemicSpelling.get(token);
			tokenPhones += " ";
			ArrayList<String> results = new ArrayList<String>();
			//replace phones in a certain class with its class name
			String[][] partitions = {{"vowel", "consonant"}, 
					{"non-sibilant", "sibilant","vowel", "sonorant"},
					{"non-sibilant", "sibilant","sonorant","vowel-front","vowel-mid","vowel-back",},
					{"glide","nasal","plosive","fricative","approximant","trill","glide","vowel-front","vowel-mid","vowel-back"}};
			for (String[] partition : partitions){
				String currentResult = tokenPhones;
				for (String type : partition){
					String[] patternAssoc = getPatternAssoc(type);
					currentResult = currentResult.replaceAll(patternAssoc[0], patternAssoc[1]);
				}
				results.add(currentResult);
			}
			return results;
		}
		private String[] getPatternAssoc(String type){
			String surfacePattern = "";
			String typePattern = "";
			if (type.equals("vowel")){
				surfacePattern = "((iy|ih|eh|ae|aa|er|ah|ax|ao|uw|uh|ow|axr|ax-h) )+";
				typePattern = "v+";
			}
			else if (type.equals("consonant")){
				surfacePattern = "((b|d|g|p|q|t|k|dx|bcl|dcl|gcl|pcl|tcl|kcl|jh|ch|z|zh|" +
						"v|dh|s|sh|f|v|th|m|n|nx|ng|em|en|eng|l|r|y|w|el|hh|hv|"+
						"pau|epi|wb|sb )+";
				typePattern = "c+";
			}
			else if (type.equals("voiced")){
				//TODO : fill out voiced phones
			}
			else if (type.equals("unvoiced")){
				//TODO : fill out unvoiced phones
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

}