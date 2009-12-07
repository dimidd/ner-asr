$dataDir = "../experiments/all_ref_data/data";
@refFiles = `ls $dataDir/*.txt`;
foreach $refFile (@refFiles){
  chomp($refFile);
  open IN, "<$refFile" or die ("Can't open $refFile");
  $/ = "";
  $content = <IN>;
  if ($content =~ /\#/){
    print $refFile."\n";
  }
}
