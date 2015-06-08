## What we plan to do with what data ##

In this project, we plan to
**investigate and analyze the performance of named entity recognition in broadcast news speech output.**

**explore various rescoring techniques and usefulness of external resources to provide additional information along with local contexts surrounding the candidate named entities.**


**We will use the TDT4 corpus on LDC:
  * [Text annotations and transcripts](http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2005T16)**

  * [Speech](http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2005S11)

## Why we think it’s interesting ##

**Recognizing Named Entities from speech or at least detecting the ASR errors caused by Named Entities have been a challenging problem in itself.**

**Although, this problem has been around for a while, it is still interesting given the trend that spoken dialog interfaces like Google Voice Search would compete with traditional textual interfaces for search.**

**This problem may tunnel into the domain of less well-behaved texts and other non-conventional usage of Named Entities like in the case of spontaneous speech, OCR etc.**

**Miller et al. '97 observed a motivational reason to consider NE extraction from speech as a problem of interest.
They found that OOV rate for words that are part of named-entities can be as much as a factor of ten greater than the baseline OOV for non-name words.**


**Related work
  * Miller et al. '97 (Named Entity Extraction from Noisy Input)
  * Benoit et al. '05 (similar work on french broadcast news)
  * Palmer and Ostendorf '05(Improving out of vocabulary name resolution)
  * Palmer and Ostendorf '01 (Improving Information Extraction by Modeling Errors in Speech Recognizer Output)
  * Chung et al. '04(A Dynamic Vocabulary Spoken Dialogue Interface)**

## What techniques we plan to use. How we plan to evaluate our work. ##

**We plan to first do an implementation of a good published method to use as the baseline. From there, we will try to come up with ways to improve it, either with feature engineering or with the model. Currently, we are still surveying the literature to search for good known methods. Once we know what has already been explored, we will have a better idea of what to try.**

**To obtain labels for the training data, we will first run a vanilla NER model on the transcribed version of the data (which doesn't have punctuation and capitalization information). Doing this will give labels at almost the same accuracy level as that obtained from running on data with punctuation and capitalization, as shown by an early work (Miller et al. '97).**


## What question you want to answer? ##
**Whether external resources "really" help improve speech based IE systems?**

**How much would acoustic confidence information help?**

## References ##
**David Miller, Scan Boisen, Richard Schwartz, Rebecca Stone, Ralph Weischedel. ''Named Entity Extraction from Noisy Input: Speech and OCR.'' ACL 1997**

**David D. Palmer, Mari Ostendorf. ''Improving out-of-vocabulary name resolution'', Computer Speech and Language 19 (2005) 107-128.**

**David D. Palmer, Mari Ostendorf. ''Improving Information Extraction by Modeling Errors in Speech Recognizer Output.'' Proceedings of the first international conference on Human language technology research, 2001.**

**G Chung, S Seneff, C Wang, L Hetherington - ''A dynamic vocabulary spoken dialogue interface'', Proc. ICSLP, 2004.**

