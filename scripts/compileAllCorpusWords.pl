#my $dataDir = "../experiments/nice_asr/labeled/*.asr";
#my $outputFile = "../experiments/nice_asr/setOfWords.txt";

my $dataDir = "../experiments/220_asr/labeled/*.asr";
my $outputFile = "../experiments/220_asr/setOfWords.txt";

my @files = `ls $dataDir`;
my %wordSet;
foreach my $file (@files){
  chomp($file);
  open FILE, "<$file" or die("Can't open $file");
  $/ = "";
  my $content = <FILE>;
  $content =~ s/\n/ /g;
  $content =~ s/<NAME>|<\/NAME>|_UNALIGNED_//g;
  foreach my $word(split(/\s+/, $content)){
    $wordSet{$word} = 1;
  }
  close FILE;
}
open OUT, ">$outputFile" or die("Can't open $outputFile");
foreach my $word (keys %wordSet){
  if ($word ne ""){
    print OUT $word."\n";
  }
}
close OUT;
