use strict;

my $phoneSpellingFile = "../experiments/220_asr/phone_spellings";

open IN, "<$phoneSpellingFile" or die("Can't open $phoneSpellingFile");
my %phoneSet;
foreach my $line (<IN>){
  chomp($line);
  my @components = split(/\s{2,}/, $line);
  if (scalar(@components) == 2){
    my $phoneString = $components[1];
    #print $phoneString."\n";
  $phoneString =~ s/(^\s+|\s+$)//g;
  foreach my $phone (split(/\s+/, $phoneString)){
    if (exists($phoneSet{$phone})){
      $phoneSet{$phone} ++;
    }
    else{
      $phoneSet{$phone} = 1;
    }
  }
}
else {
  print "FAILED\n";
}
}
foreach my $phone (keys(%phoneSet)){
  print "$phone\t$phoneSet{$phone}\n";
}
