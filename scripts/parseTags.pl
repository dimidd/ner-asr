#use module
use XML::Simple;
use Data::Dumper;

$xml = new XML::Simple;
$tagFolder = "../../data/labeled-NamedEntities/ace_tides_multling_train/DATA/ENGLISH/BNEWS";
@markupFiles = `ls $tagFolder/*_APF.XML`;
foreach $markupFile (@markupFiles){
	chomp($markupFile);
	$data = $xml->XMLin("$markupFile");
	$entities= $data->{document}->{entity};
	foreach $entity (@{$entities}){
		$entityMentions = $entity->{entity_mention};
		if (ref($entityMentions) eq "ARRAY"){
		#if there are more than one entity mentions in this entity
			foreach $entityMention (@{$entityMentions}){
				if ($entityMention->{TYPE} eq 'NAM'){
					print  $entityMention->{head}->{charseq}->{content}." occurred at ".$entityMention->{head}->{charseq}->{START}."\n";
				}
			}
		}
		else{
			if ($entityMentions->{TYPE} eq 'NAM'){
					print  $entityMentions->{head}->{charseq}->{content}." occurred at ".$entityMentions->{head}->{charseq}->{START}."\n";
			}
		}
	}
}
