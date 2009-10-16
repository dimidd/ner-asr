use strict;
my ($cwd, $TDTDir, $txtDir, $wavDir, $outputDir, @newsSources, $newsSource, @allASRFiles, $asr, $filename, @lines, $lineNum, $firstLine, $currentLine, $startTime, $endTime, $numLines, $inputFileName, $outputFileName, $segCount);
$cwd = `pwd`;
chomp($cwd);
$TDTDir = "$cwd/../../data/TDT4_ENG_1";
$txtDir = "$TDTDir/txt";
$wavDir = "$TDTDir/wav";
$outputDir = "$TDTDir/wavSeg";
mkdir $outputDir unless -d $outputDir;

@newsSources = `ls $txtDir`;
foreach $newsSource (@newsSources){
  chomp($newsSource);
  mkdir "$outputDir/$newsSource" unless -d "$outputDir/$newsSource";
  chdir "$txtDir/$newsSource";
  @allASRFiles = `ls *.asr`;
  foreach $asr (@allASRFiles){
    chomp($asr);
    print "Working on $asr\n";
    $filename = substr $asr, 0, -4;
    open ASR, "<$asr" or die ("Cannot open $asr");
    @lines = <ASR>;
    $lineNum = 1;
    $numLines = @lines;
    $segCount = 0;
    while ($lineNum < $numLines){
      #print $lineNum.'\n';
      $firstLine = $lines[$lineNum];
      $firstLine =~ /.+Bsec=(.+) Dur=.*/;
      $startTime = $1;

      $lineNum++;
      while ($lineNum < $numLines && ($currentLine = $lines[$lineNum]) !~ /<X.+/){
        $lineNum ++;
      }
      #now $currentLine contains silence
      $currentLine =~ /.+Bsec=(.+) Dur=.*/;
      $endTime = $1;
      $inputFileName = "$wavDir/$newsSource/$filename.wav";
      $outputFileName = "$outputDir/$newsSource/$filename"."_$segCount.wav";
      system("\$ESTDIR/bin/ch_wave $inputFileName -start $startTime -end $endTime -o $outputFileName");
      print "Done with $filename\_$segCount\n";
      $segCount ++;
      $lineNum ++;
    }
  }
}
