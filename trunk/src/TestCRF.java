import java.io.*;
import edu.cmu.minorthird.classify.experiments.CrossValSplitter;
import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.text.*;
import edu.cmu.minorthird.text.learn.*;
import edu.cmu.minorthird.text.learn.experiments.ExtractionEvaluation;
import edu.cmu.minorthird.text.learn.experiments.TextLabelsExperiment;
import edu.cmu.minorthird.ui.Recommended;
import edu.cmu.minorthird.util.gui.SmartVanillaViewer;
import edu.cmu.minorthird.util.gui.ViewerFrame;

import java.util.*;
import java.io.*;

public class TestCRF {
	private static Map<String,String> phonemicSpelling ;

	public static void main(String[] args){
		try{
			// load data from dataDir into labels
			String dirName = "experiments/nice_asr/labeled";

			phonemicSpelling  = new HashMap<String,String>();
			BufferedReader reader = new BufferedReader(new FileReader(dirName+"/../phone_spellings"));
			String str;
			while((str=reader.readLine())!=null)
			{
				String word = str.split("\\s+")[0];
				String phone_spelling = str.substring(word.length());
				phonemicSpelling.put(word, phone_spelling.trim());
			}
			reader.close();
			File dataDir = new File(dirName);

			TextBaseLoader loader = new TextBaseLoader(TextBaseLoader.DOC_PER_FILE, true);
			loader.load(dataDir, new SplitTokenizer("\\s+"));
			BasicTextLabels labels = (BasicTextLabels)loader.getLabels();
			//BasicTextBase textBase = (BasicTextBase)labels.getTextBase();

			/*
	  	System.out.println(labels.getTypes());
	  	Iterator<Span> it = labels.instanceIterator("NAME");
	  	while (it.hasNext()){
	  		Span s = it.next();
	  		System.out.println(s.toString());
	  	}
	  	String format = labels.getFormatNames()[0];
	  	File allData = new File("allData.labels");
	  	labels.saveAs(allData, format);
			 */

			//TextLabelsAnnotatorTeacher teacher = new TextLabelsAnnotatorTeacher(
			//	labels, "NAME");

			// learner that will be used in the experiment
			AnnotatorLearner learner = new Recommended.CRFAnnotatorLearner();
			// vanillaFE that's slightly different from the default FE, using a 
			// window size of 4 instead of 3, and not using charTypePattern
			Recommended.MultitokenSpanFE vanillaFE = new Recommended.MultitokenSpanFE();
			vanillaFE.setUseCharTypePattern(false);
			vanillaFE.setFeatureWindowSize(4);

			// set the features that this learner will use
			//learner.setSpanFeatureExtractor(vanillaFE);
			//learner.setSpanFeatureExtractor(new PhoneUnigramFE());
			learner.setSpanFeatureExtractor(new PhoneBigramFE());
			learner.setAnnotationType("_prediction");
			int num_partitions = 10;
			CrossValSplitter<Span> splitter=new CrossValSplitter<Span>(num_partitions);
			TextLabelsExperiment 
			expt = new TextLabelsExperiment(labels, splitter, learner, "NAME", "_prediction");

			expt.doExperiment();
			ExtractionEvaluation evaluation = expt.getEvaluation();	
			new ViewerFrame("Experimental Result",new SmartVanillaViewer(evaluation));
			/*Tokenizer t = new SplitTokenizer("\\s+");
			String[] results = t.splitIntoTokens("WAS TAKEN OVER AREN'T. TAKEN OVER");
			for (String s : results){
				System.out.println(s);
			}*/
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	public static class PhoneUnigramFE extends SpanFE{
		// prepare the feature extractor
		protected int windowSize = 4;
		public void extractFeatures(TextLabels labels, Span span){
			// add bag of words for all tokens in this span and in the surrounding
			// window of size windowSize
			from(span).tokens().emit();
			from(span).left().subSpan(- windowSize, windowSize).emit();
			from(span).right().subSpan(0, windowSize).emit();
			
			//System.out.println("Currently in document: "+span.getDocumentId());
			ArrayList<String> tokens = new ArrayList<String>();
			//add token to list of tokens in left to right order
			for (int i = windowSize; i > 0; i--){
				String token = from(span).left().subSpan(-i, 1).getSpan().asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token);
			}
			for (int i = 0; i < span.size(); i++){
				String token = span.subSpan(i, 1).asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token);
			}
			for (int i = 0; i < windowSize; i ++){
				String token = from(span).right().subSpan(i, 1).getSpan().asString();
				if (!token.equals("_UNALIGNED_") && !token.equals(""))
					tokens.add(token);
			}
			
			HashMap<String, Integer> phoneCounts = getPhoneCounts(tokens);
			for (String phone : phoneCounts.keySet()){
				instance.addNumeric(new Feature(phone), phoneCounts.get(phone));
			}
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
}
