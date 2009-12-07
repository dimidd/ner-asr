open IN, "<$ARGV[0]" or die "Can't open $ARGV[0]";

$/="";
$file = <IN>;
while ($file =~ /( TYPE=".*?" )/g){
  print $1."\n";
}
