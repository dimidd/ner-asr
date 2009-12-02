use strict;
use warnings;

my $exptDir = "../experiments";
my $destDir = "$exptDir/nice_asr/labeled";
my $hypFile = "$exptDir/hyps_79_tdt4.txt";
my $offsetFile = "$exptDir/hyp_tdt4_offsets";
#my $destDir = "$exptDir/bad_asr/data";
#my $hypFile = "$exptDir/hyps_79_mod.txt";
#my $offsetFile = "$exptDir/hyp_offsets";
my $namesFile = "$exptDir/file_names.log";

open HYP, "<$hypFile" or die("Can't open $hypFile");
open OFF, "<$offsetFile" or die("Can't open $offsetFile");
open NAMES, "<$namesFile" or die("Can't open $namesFile");

my @hyps = <HYP>;
my @offs = <OFF>;
my @names = <NAMES>;

for (my $i = 0; $i < scalar(@hyps); $i++){
  my $curHyp = $hyps[$i];
  my $curOff = $offs[$i];
  my $curName = $destDir."/".substr($names[$i], 0, -5)."_labeled.asr";
  #my $curName = $destDir."/".$names[$i];
  my (@starts, @ends);
  while ($curOff =~ /beg: (\d*) end: (\d*)/g){
    push(@starts, $1);
    push(@ends, $2);
  }
  my $curIncrement = 0;
  print "currently processing file #$i\n";
  for (my $j = 0; $j < scalar(@starts); $j++){
    #my ($curStart, $curEnd) = (0, 0);
    my $curStart = $starts[$j] + $curIncrement;
    #add <NAME> to $curHyp at $curStart
    #print $curStart."\n";
    $curHyp = substr($curHyp, 0, $curStart)." <NAME> ".substr($curHyp, $curStart);
    $curIncrement += 8;
    my $curEnd = $ends[$j] + $curIncrement;
    #print $curEnd."\n";
    #add </NAME> to $curHyp at $curEnd
    $curHyp = substr($curHyp, 0, $curEnd)." </NAME> ".substr($curHyp, $curEnd);
    $curIncrement += 9;
  }
  #convert empty or meaningless regions within tags into _UNALIGNED_
  $curHyp =~ s/<NAME>[ *]+<\/NAME>/<NAME>_UNALIGNED_<\/NAME>/g;
  #remove all * from $curHyp
  $curHyp =~ s/\*//g;
  $curHyp =~ s/\s+/ /g;
  open OUT, ">$curName" or die("Can't open $curName");
  print OUT $curHyp;
  close OUT;
}
close HYP;
close OFF;
