$wavDir = "../../data/TDT4_ENG_1/wav/ABC_ENG";
chdir $wavDir or die("Can't change into $wavDir");
$cwd = `pwd`;
@wavFiles = `ls *.wav`;
$i = 0;
while ($i < @wavFiles){
  $j = 0;
  $fileName = "control_$i.ctl";
  open CONTROL, "> $fileName";
  while ($j < 500){
    chomp($wavFiles[$j]);
    print CONTROL "$wavFiles[$j]\n";
    $j++;
    $i++;
  }
  close CONTROL;
}

