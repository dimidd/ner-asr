#use Data::Dumper;
use XML::Simple;
use strict;
use warnings;

my $xml = new XML::Simple;
my $tagFolder = "../../data/labeled-NamedEntities/ace_tides_multling_train/DATA/ENGLISH/BNEWS";
#my $tagFolder = '.';
my @markupFiles = `ls $tagFolder/*_APF.XML`;
foreach my $markupFile (@markupFiles){
  chomp($markupFile);
  if ($markupFile =~ /(.*)_APF\.XML/){
    my $srcFile = "$1\.SGM";
    my $destFile= "$1.txt"; #SIM for simple
    my @names = extractNamesFromStandoff($markupFile);
    my $namesRef = \@names;
    addTagsToSource($namesRef, $srcFile, $destFile);
  }
  else {
    print "COULDN'T FIND _APF.XML PATTERN";
  }
}

sub extractNamesFromStandoff{
  my @names;
  my $markupFile = $_[0];
  my $data = $xml->XMLin("$markupFile");
  my $entities= $data->{document}->{entity};
  foreach my $entity (@{$entities}){
    my $entityMentions = $entity->{entity_mention};
    if (ref($entityMentions) eq "ARRAY"){
    #if there are more than one entity mentions in this entity
      foreach my $entityMention (@{$entityMentions}){
        if ($entityMention->{TYPE} eq 'NAM'){
          my $name = $entityMention->{head}->{charseq}->{content};
          $name =~ s/\n/ /g;
          push(@names, $name);
        }
      }
    }
    else{
      if ($entityMentions->{TYPE} eq 'NAM'){
        my $name = $entityMentions->{head}->{charseq}->{content};
        $name =~ s/\n/ /g;
        push(@names, $name);
      }
    }
  }
  my %hash;
  @hash{@names} = ();
  my @uniqueNames = keys %hash;
  return @uniqueNames;
}

sub addTagsToSource{
  my ($namesRef, $srcFile, $destFile) = @_;
  my @names = @{$namesRef};
  open SRC, '<:utf8', $srcFile or die("Can't open $srcFile");
  open DEST, ">$destFile" or die ("Can't open $destFile");

  my @lines = <SRC>;
  chomp(@lines);
  my $doc = join(" ", @lines);
  $doc =~ s/ \<TURN\> //g;
  if ($doc =~ /\<TEXT\>(.*)\<\/TEXT\>/g){
    my $text = $1;
    $text =~ s/^\s+//g;
    $text =~ s/<ANNOTATION>.*?<\/ANNOTATION>//g;
    foreach my $name (@names){
      #handle overlappings of tags by allowing XML tags
      #between words in the name
      $name =~ s/(^\s+|\s+$)//g;
      $name =~ s/\s/\(\?:\\s\|\\s\*\\<\[\^>\]\*\?\\>\\s\*\)/g;
      $text =~ s/([^a-zA-Z])($name)([^a-zA-Z])/$1<NAME>$2<\/NAME>$3/g;
    }
    print DEST $text;
  }
  else {
    print "COULDN'T FIND <TEXT> ... </TEXT> PATTERN\n";
  }

  close SRC;
  close DEST;
}
