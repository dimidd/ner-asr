use strict;
use warnings;
use XML::Simple;

my $xml = new XML::Simple;
my $tagFolder = "../../data/labeled-NamedEntities/ace_tides_multling_train/DATA/ENGLISH/BNEWS";
#my $tagFolder = ".";
my $ASRFolder = "../../data/TDT4_ENG_2005/Text-Annotations/tdt4_aem_txt/data/asr_sgm";
my $destFolder = "../experiments/nice_asr/unlabeled";
my @markupFiles = `ls $tagFolder/*_APF.XML`;
foreach my $markupFile (@markupFiles){
  chomp($markupFile);
  if ($markupFile =~ /$tagFolder\/([A-Z]{3})(\d{8})_(\d{4})_(\d{4})_APF\.XML/){
    my ($newsSource, $date, $startTime, $endTime) = ($1, $2, $3, $4);
    my $docNo = "$newsSource$date.$startTime.$endTime";
    my $newFile = $destFolder."/".$newsSource.$date."_".$startTime."_".$endTime."_unlabeled.asr";
    my $asrFilePattern = $date."_".$startTime."_*_".$newsSource;
    my $asrFileName = `ls $ASRFolder/$asrFilePattern*.asr_sgm`;
    chomp($asrFileName);
    open ASR, "<$asrFileName" or die ("Can't open $asrFileName");
    $/ = "";
    my $allASR = <ASR>;
    $allASR =~ s/\n/ /g;
    if ($allASR =~ /$docNo/){
      if ($' =~ /<TEXT> (.*?) <\/TEXT>/){
        my $text = $1;
        open OUT, ">$newFile" or die ("Can't open $newFile");
        print OUT $text;
      }
      else {
        print "COULDN'T FIND STUFF BETWEEN <TEXT> TAGS\n";
      }
    }
    else {
      print "COULDN'T FIND $docNo\n";
    }
  }
  else {
    print "COULDN'T MATCH PATTERN AGAINST $markupFile\n";
  }
}
