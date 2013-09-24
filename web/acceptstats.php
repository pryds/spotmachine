<?php
require("db.php"); //must specify the four variables $host, $user, $pass, and $db
$con = mysqli_connect($host, $user, $pass, $db);
if (mysqli_connect_errno()) {
  echo "acceptstats.php: Failed to connect to DB\n";
}

$labels = array(
  "installationID",
  //"reportReceived", calculated by phpscript
  "reportCreated",
  "systemUptime",
  "spotmachineUptime",
  "systemTimeZone",
  "systemLocale",
  "spotmachineLanguage",
  "spotmachineVersion",
  "osName",
  "osVersion",
  "osArch",
  "jreVendor",
  "jreVersion",
  "jreInstallDir",
  "workingDir",
  "classPath",
  "availableProcessorCores",
  "totalMemInBytes",
  "freeMemInBytes",
  "maxMemToBeUsedInBytes",
  "fileRoot",
  "fileRootTotalSpaceInBytes",
  "fileRootFreeSpaceInBytes",
  "fileRootUsableSpaceInBytes"
);

// Parse data parameter and put known key/value pairs in $sql
// Unknown keys: Value goes to "extra"
$lines = preg_split('/\r\n|\r|\n/', $_POST['data']);

for ($i = 0; $i < count($lines); $i++) {
  $foundMatch = false;
  $labelAndValue = explode(':', $lines[$i]);
  $labelAndValue[0] = trim($labelAndValue[0]);
  $labelAndValue[1] = trim($labelAndValue[1]);
  
  for ($j = 0; $j < count($labels); $j++) {
    if ($labels[$j] == $labelAndValue[0]) {
      $sql[$labelAndValue[0]] = $labelAndValue[1];
      $foundMatch = true;
      echo "acceptstats.php: Key |" . $labelAndValue[0] . "| is known. Adding value |" . $labelAndValue[1] . "|\n";
      break;
    }
  }
  if (!$foundMatch && $labelAndValue[0] != "") {
    echo "acceptstats.php: Key |" . $labelAndValue[0] . "| is NOT known. Adding key and value |" . $labelAndValue[1] . "| to extra\n";
    $sql['extra'] = $sql['extra'] . $labelAndValue[0] . "=" . $labelAndValue[1] . ", ";
  }
}
// Extra on-server key/value pairs:
$sql['ip'] = $_SERVER['REMOTE_ADDR'];
$sql['host'] = $_SERVER['REMOTE_HOST'];

// Build SQL string
$sqlStrOne = "INSERT INTO spot_stats (reportReceived";
$sqlStrTwo = ") VALUES ('" . round(microtime(true)*1000) . "'";
foreach ($sql as $key => $value) {
  $sqlStrOne = $sqlStrOne . ", " . $key;
  $sqlStrTwo = $sqlStrTwo . ", '" . $value . "'";
}
$query = $sqlStrOne . $sqlStrTwo . ")";

// Send SQL query
mysqli_query($con, $query);

echo "acceptstats.php: Query: ||" . $query . "||\n";

?>

