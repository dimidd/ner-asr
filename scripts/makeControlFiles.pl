$wavDir = "../../data/TDT4_ENG_1/wavSeg/ABC_ENG";
chdir $wavDir or die("Can't change into $wavDir");
$cwd = `pwd`;
@wavFiles = `ls *.wav`;
$i = 0;
while ($i < @wavFiles){
  $j = 0;
  $fileName = "control_$i.ctl";
  open CONTROL, "> $fileName";
  while ($j < 500){
    chomp($wavFiles[$i]);
    print CONTROL "$wavFiles[$i]\n";
    $j++;
    $i++;
  }
  close CONTROL;
}

