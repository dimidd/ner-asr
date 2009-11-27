#use Data::Dumper;
use XML::Simple;
use strict;
use warnings;

my $xml = new XML::Simple;
#$tagFolder = "../../data/labeled-NamedEntities/ace_tides_multling_train/DATA/ENGLISH/BNEWS";
my $tagFolder = '.';
my @markupFiles = `ls $tagFolder/*_APF.XML`;
foreach my $markupFile (@markupFiles){
  chomp($markupFile);
  if ($markupFile =~ /(.*)_APF\.XML/){
    my $srcFile = $1."\.SGM";
    my $destFile= $1."_SIM.XML"; #SIM for simple
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
          #print $entityMention->{head}->{charseq}->{content}." occurred at ".$entityMention->{head}->{charseq}->{START}."\n";
          my $name = $entityMention->{head}->{charseq}->{content};
          push(@names, $name);
        }
      }
    }
    else{
      if ($entityMentions->{TYPE} eq 'NAM'){
        #print $entityMentions->{head}->{charseq}->{content}." occurred at ".$entityMentions->{head}->{charseq}->{START}."\n";
        my $name = $entityMentions->{head}->{charseq}->{content};
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
  print $doc."\n";
  my $text = "";
  if ($doc =~ /\<TEXT\>(.*)\<\/TEXT\>/g){
    $text = $text.$1;
    $text =~ s/^\s+//g;
    foreach my $name (@names){
      $text =~ s/$name/\<NAME\> $name \<\/NAME\>/g;
    }
    print DEST $text;
  }
  else {
    print "COULDN'T FIND <TEXT> ... </TEXT> PATTERN\n";
  }

  close SRC;
  close DEST;
}
