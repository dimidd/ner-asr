import java.io.*;
import java.util.*;

import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.text.Span;
import edu.cmu.minorthird.text.TextLabels;
import edu.cmu.minorthird.text.learn.SpanFE;


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
	public static class PhoneUnigramFE extends SpanFE{
		// prepare the feature extractor
		protected int windowSize = 2;
		protected boolean useCurrentSpan = true;
		public void setWindowSize(int n){
			windowSize = n;
		}
		public void setUseCurrentSpan(boolean useCurrentSpan){
			this.useCurrentSpan = useCurrentSpan;
		}
		public void extractFeatures(TextLabels labels, Span span){
			// add bag of words for all tokens in this span and in the surrounding
			// window of size windowSize
			from(span).tokens().emit();
			from(span).left().subSpan(- windowSize, windowSize).emit();
			from(span).right().subSpan(0, windowSize).emit();
			
			//System.out.println("Currently in document: "+span.getDocumentId());
			
			ArrayList<String> tokens = getTokenSequence(span);
			HashMap<String, Integer> phoneCounts = getPhoneCounts(tokens);
			for (String phone : phoneCounts.keySet()){
				instance.addNumeric(new Feature(phone), phoneCounts.get(phone));
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
		
		
		private HashMap<String, Integer> getPhoneCounts(ArrayList<String> tokens){
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
		
		
	}
	public static class PhoneBigramFE extends PhoneUnigramFE{
		private HashMap<String, Integer> getPhoneCounts(ArrayList<String> tokens){
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
	}	
	public static class
}

