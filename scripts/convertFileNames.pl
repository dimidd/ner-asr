$srcDir = "../experiments/all_ref_data/data";
$destDir = "../experiments/non_corrupted_data/data";
$fileList = "../experiments/non_corrupted_data/all_hyps";

open LIST, "<$fileList" or die "Can't open $fileList";
while (<LIST>){
  $name = $_;
  chomp($name);
  if ($name =~ /(\d{8}_\d{4})_\d{4}_(\w{3})_\w{3}_x_(\d{4})\.hyp/){
    $ACEName = "$2$1_$3.txt";
    `cp "$srcDir/$ACEName" "$destDir/"`;
  }
  else{
    print "COULDN'T COPY |$name|\n";
  }
}
