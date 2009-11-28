import java.io.*;
import java.util.*;

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
		File dataDir = new File("experiments/non_corrupted_data/data");
	  	TextBaseLoader loader = new TextBaseLoader(TextBaseLoader.DOC_PER_FILE, true);
	  	loader.load(dataDir);
	  	BasicTextLabels labels = (BasicTextLabels)loader.getLabels();
	  	BasicTextBase textBase = (BasicTextBase)labels.getTextBase();
	  	/*System.out.println("Size="+textBase.size());
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
	  	
	  	// prepare the feature extractor
	  	//TextLabelsAnnotatorTeacher teacher = new TextLabelsAnnotatorTeacher(
	  		//	labels, "NAME");
	  	SpanFE fe = new SpanFE(){
	  		public void extractFeatures(TextLabels labels, Span span){
	  			from(span).tokens().emit();
	  			from(span).left().subSpan(-2, 2).emit();
	  			from(span).right().subSpan(0, 2).emit();
	  		}
	  	};
	  	// training a CRF
	  	AnnotatorLearner learner = new Recommended.CRFAnnotatorLearner();
	  	//learner.setSpanFeatureExtractor(fe);
	  	//learner.setAnnotationType("_prediction");
	  	/*
	  	Annotator ann = teacher.train(learner);
	  	*/
	  	int num_partitions = 10;
	  	CrossValSplitter<Span> splitter=new CrossValSplitter<Span>(num_partitions);
		//splitter.split(labels.getTextBase().documentSpanIterator());	  	
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
