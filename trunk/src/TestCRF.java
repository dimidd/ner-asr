import java.io.*;
import edu.cmu.minorthird.classify.experiments.CrossValSplitter;
import edu.cmu.minorthird.text.*;
import edu.cmu.minorthird.text.learn.*;
import edu.cmu.minorthird.text.learn.experiments.ExtractionEvaluation;
import edu.cmu.minorthird.text.learn.experiments.TextLabelsExperiment;
import edu.cmu.minorthird.ui.Recommended;
import edu.cmu.minorthird.util.gui.SmartVanillaViewer;
import edu.cmu.minorthird.util.gui.ViewerFrame;

public class TestCRF {

	public static void main(String[] args){
		try{
			String dirName = "experiments/nice_asr/labeled";
			//String dirName = "experiments/bad_asr/data";

			//load phone pronunciations into memory
			CustomFE.loadPhonemicSpellings(dirName+"../phone_spellings");

			// load data from dataDir into labels
			File dataDir = new File(dirName);
			TextBaseLoader loader = new TextBaseLoader(TextBaseLoader.DOC_PER_FILE, true);
			loader.load(dataDir, new SplitTokenizer("\\s+"));
			BasicTextLabels labels = (BasicTextLabels)loader.getLabels();

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


			// learner that will be used in the experiment
			AnnotatorLearner learner = new Recommended.CRFAnnotatorLearner();
			// vanillaFE that's slightly different from the default FE, using a 
			// window size of 4 instead of 3, and not using charTypePattern
			Recommended.MultitokenSpanFE vanillaFE = new Recommended.MultitokenSpanFE();
			vanillaFE.setUseCharTypePattern(false);
			vanillaFE.setFeatureWindowSize(4);

			// set the features that this learner will use
			//learner.setSpanFeatureExtractor(vanillaFE);
			CustomFE.PhoneUnigramFE fe = new CustomFE.PhoneUnigramFE();
			fe.setUseCurrentSpan(true);
			//fe.setWindowSize(3);

			learner.setSpanFeatureExtractor(fe);
			learner.setAnnotationType("_prediction");
			int num_partitions = 10;
			CrossValSplitter<Span> splitter=new CrossValSplitter<Span>(num_partitions);
			TextLabelsExperiment 
			expt = new TextLabelsExperiment(labels, splitter, learner, "NAME", "_prediction");

			expt.doExperiment();
			ExtractionEvaluation evaluation = expt.getEvaluation();	
			new ViewerFrame("Experimental Result",new SmartVanillaViewer(evaluation));
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}





}
