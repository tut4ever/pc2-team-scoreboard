<?php
$password = "contest3";
	$con = mysql_connect("192.168.0.1","errorsystem",$password);
	mysql_select_db("iphonespoof", $con);

	$ip = $_SERVER['REMOTE_ADDR'];
	$query = mysql_query("SELECT * FROM logins WHERE IP='" . $ip . "'");
	$row = mysql_fetch_row($query);
	
	$teamnumber = $row['team'];
	
	$query = mysql_query("SELECT * FROM errors WHERE team='" . $teamnumber . "'");
	echo '<h1 align="center">Team ' . $teamnumber  . ' Errors</h1><br/>';
	echo "<table >";
	echo "<b><tr><td>Run #</td><td>Team #</td><td>Error</td></tr></b>";
		
	
	while($row = mysql_fetch_array($query))
	{
		echo "<tr><td class=\"auto-style3\">";
		echo $row['run'];
		echo "</td><td class=\"auto-style3\">";
		echo $row['team'];
		echo "</td><td class=\"auto-style3\">";
		echo $row['error'];
		echo "</td></tr>";
		
	}


?>


