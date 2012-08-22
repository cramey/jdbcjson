jdbcjson
========

JDBCJSON is a simple utility for generating JSON from SQL queries
using JDBC. JDBCJSON takes a simple properties file for it's
configuration.


basic usage
-----------

First, create your properties file. JDBCJSON breaks properties down
by jobs. Each job has three parameters, url, sql and out. url is
the JDBC connection URL. sql is the SQL statement executed whose result
is used to generate the JSON. out is the path for resulting JSON. Take
the following example:

    mytable.url = jdbc:postgresql://localhost/mydb
		mytable.sql = SELECT * FROM mytable
		mytable.out = mytable.json


This properties file will create a job called "mytable" that will query
a postgresql database on localhost, selecting all the columns from
mytable, and output the result to mytable.json. Multiple jobs may be
specified in a single properties file.


Next, run JDBCJSON. It's always a good idea to specify the debug switch
the first time you run a new properties file against JDBC. The debug
switch will give you additional warnings about things like unsupported
fields (BLOBs, for example.)

    java -jar jdbcjson.jar -d mytable.properties


advanced usage
--------------

Only the url parameter of a properties file is required. The other two
properties have default values, based on the job name. The sql parameter
defaults to "SELECT * FROM <jobname>". The out parameter defaults to
"<jobname>.json". Thus,

    mytable.url = jdbc:postgresql://localhost/mydb

Is the same as

    mytable.url = jdbc:postgresql://localhost/mydb
		mytable.sql = SELECT * FROM mytable
		mytable.out = mytable.json
