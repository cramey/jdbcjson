jdbcjson
========

JDBCJSON is a simple utility for generating JSON from SQL queries
using JDBC. JDBCJSON takes a simple properties file for it's
configuration.


basic usage
-----------

First, create your properties file. JDBCJSON breaks properties down
by jobs. Each job has four parameters: `driver`, `url`, `sql`, and `out`.
`driver` is an optional parameter for specifying the driver class to be
registered. `driver` is only required for non-JDBC4 drivers. JDBC4 drivers
should self register. `url` is the JDBC connection URL (see your driver
documentation for more details.) `sql` is the SQL statement executed
whose result is used to generate the JSON. `out` is the path for
resulting JSON. Take the following example:

		people.driver = net.sourceforge.jtds.jdbc.Driver
		people.url = jdbc:jtds:sqlserver://myserver/db;user=sa;password=sa
		people.sql = SELECT * FROM people
		people.out = people.json


This properties file will create a job called _people_ that will query
a SQL Server database on localhost, selecting all the columns from
_people_, and output the result to _people.json_. Multiple jobs may be
specified in a single properties file.


Next, run JDBCJSON. It's always a good idea to specify the debug switch
the first time you run a new properties file against JDBC. The debug
switch will give you additional warnings about things like unsupported
fields (BLOBs, for example.)

    java -jar jdbcjson.jar -d people.properties


advanced usage
--------------

Only the url parameter of a properties file is required. The `url` and `out`
properties have default values, based on the job name. The `sql` parameter
defaults to `SELECT * FROM <jobname>`. The `out` parameter defaults to
`<jobname>.json`. Thus:

    mytable.url = jdbc:postgresql://localhost/mydb?user=postgres&password=postgres

Is the same as:

    mytable.url = jdbc:postgresql://localhost/mydb?user=postgres&password=postgres
		mytable.sql = SELECT * FROM mytable
		mytable.out = mytable.json
