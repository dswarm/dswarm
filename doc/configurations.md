Configuration of the system
===========================

d:swarm is configured via
[typesafe config](https://github.com/typesafehub/config#readme).
The syntax — HOCON — is quite flexible and based on a mix between JSON and Java Properties (In fact, typesafe config can parse Json and properties files as well).
Please refer to the
[summary](https://github.com/typesafehub/config#using-hocon-the-json-superset) or
[documentation](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation) of
HOCON to get familiar with the format.


All keys for d:swarm are in the namespace `dswarm` and possibly further
sub-grouped by their module (e.g. `db` or `http`).

The modules `controller`, `init`, and `persistence` have a `reference.conf` and a `application.conf`
defined in their `src/main/resources` directory.
These files are merged together into a config tree, of which you can override any key.

### complete config

The following is the merged tree for all configurations, each value is the default setting.
Every part can be overridden, conveniently so by using the properties-like syntax.


    {
        "dswarm" : {

            # definitions related to swagger
            "api" : {

                # the reported base url, that is displayed in the swagger UI
                "base-url" : "http://127.0.0.1:8087/dmp",

                # the version for our API
                "version" : "1.0.1"
            },

            # settings for the persistence module
            "db" : {

                # settings for the RDBMS used for the metadata storage
                "metadata" : {

                    # the name of the JPA unit, matching the one in persistence.xml
                    "jpa-unit" : "DMPApp",
    
                    # if 'on', verbosely log every SQL statement
                    "log-sql" : "on",

                    "host" : "127.0.0.1",
                    "port" : 3306,
                    "schema" : "dmp",
                    "username" : "not set - override in your application.conf",
                    "password" : "not set - override in your application.conf",

                    # the JDBC URI
                    # Here you can add connection parameters, e.g. the character set to use
                    "uri" : "jdbc:mysql://127.0.0.1:3306/dmp?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"
                },

                # settings for the GDBMS (Neo4j)
                "graph" : {

                    # URL to our Neo4j extension
                    "endpoint" : "http://localhost:7474/graph"
                }
            },

            # settings for the embedded web server
            "http" : {

                "context-path" : "/dmp",
                "host" : "127.0.0.1",
                "port" : 8087
            },

            # make a full dump of the configuration during startup (at INFO level)
            "log-config-on-start" : "off",

            # paths to use in various places
            "paths" : {

                # the root path of the project
                "root" : ".",

                # the path for the log files
                "logging" : "./log",

                # the path for temporary files
                "tmp" : "./tmp"
            },

            # settings for metrics and statistics reporting
            "reporting" : {

                # settings for ES, the target for reporting
                "elasticsearch" : {

                    # this is the elasticsearch address, that will be used for the connection
                    "host" : "localhost:9200",

                    "port" : 9200,
                    "server" : "localhost"
                },

                # whether to actually do the reporting
                "enabled" : "on",

                # report every ...
                "interval" : "60s"
            }
        }
    }




Some of these settings are defined in terms of other settings; for example `dswarm.db.metadata.uri` is actually defined as
`"jdbc:metadata://"${dswarm.db.metadata.host}":"${dswarm.db.metadata.port}"/"${dswarm.db.metadata.schema}"?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"`
which means, that you can either just override the setting `dswarm.db.metadata.schema` to use a different schema,
or you can override the full URI to tune the connection parameters.

The following values are such substitutions:

key  |  unresolved value
-----|--------
`dswarm.db.metadata.uri` | `"jdbc:metadata://"${dswarm.db.metadata.host}":"${dswarm.db.metadata.port}"/"${dswarm.db.metadata.schema}"?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"`
`dswarm.reporting.elasticsearch.host` | `${dswarm.reporting.elasticsearch.server}":"${dswarm.reporting.elasticsearch.port}`
`dswarm.api.baseurl` | `"http://"${dswarm.http.host}":"${dswarm.http.port}${dswarm.http.context-path}`
`dswarm.paths.logging` | `${dswarm.paths.root}/log`
`dswarm.paths.tmp` | `${dswarm.paths.root}/tmp`


### override values

You can override any value in three ways (one of, or any of these combined — they're not mutually exclusive).

You can  put a `.conf` file anywhere and start d:swarm with the system property `config.file` pointing to this file.

You can override single keys with system properties of the same name (e.g. `-Ddswarm.db.metadata.log-sql=false`).

You can set the JNDI property `configFile` (accessible at `java:comp/env/configFile`) and point to a `.conf` file anywhere on the system.

There is actually a fourth way, where you place an `application.conf` file in your classpath, but this one is used by d:swarm itself to allow for staged resolving of substituted config values.
You can still do this, but you'll probably not be able to override any of the substituted values.

#### config.file system property

Place a file named `dswarm.conf` somewhere in your filesystem and put in the following content

    cat <<EOF>>dswarm.conf
    dswarm.db.metadata.username=foo
    dswarm.db.metadata.password=bar
    dswarm.log-config-on-start=on
    EOF

Then, start d:swarm the following way

    mvn exec:java -Dconfig.file=/path/to/dswarm.conf

You should not put this file under version control. Name it either `dswarm.conf` or `dmp.conf`, and place it in the root directory — These files are already ignored by git.

#### single system properties

You can override any settings with the `-D` parameter to specify system properties. For example, the previous overrides could be achieved by starting d:swarm with this line as well

    mvn exec:java -Ddswarm.db.metadata.username=foo -Ddswarm.db.metadata.password=bar -Ddswarm.log-config-on-start=on

#### JNDI config

This one is the way to go when you use containers, such as tomcat, that would not allow system properties to be set.

For example, for tomcat: In your `$CATALINA_HOME`, create the file `conf/Catalina/localhost/dmp.xml`.
More general speaking, create the file `conf/<Engine>/<Host>/<App>.war`.
`<Engine>` is likely `Catalina`, `<Host>` is likely `localhost` and d:swarm is deployed as a `dmp.war`, so `<App>` is `dmp`

Put the following content into the file

    cat <<EOF>> dmp.xml
    <Context>
      <Environment name="configFile" value="/path/to/dswarm.conf" type="java.lang.String" />
    </Context>
    EOF

`/path/to/dswarm.conf` should be the same as in the previous example, where it was specified using `-Dconfig.file`.

This is a one-time configuration for the container


***

At any rate, any of these ways will override the specified values, and thus log the config on start and use foo:bar as credentials for the MySQL connection.


## Migration from dmp.properties

In order to move to the new configuration system, perform the following steps.

1. create a `dswarm.conf` or `dmp.conf` file

2. At least, specify the keys `dswarm.db.metadata.username` and `dswarm.db.metadata.password`

3. I'd advise, to also specify `db.paths.root` to point to an absolute directory. It is otherwise depending on the directory you started d:swarm from.

4. replace these old keys with their new keys, or remove them if you're OK with the defaults

    old key | new key | default value
    --------|---------|--------------
    `logging_root_path` | `dswarm.paths.logging` | `${dswarm.paths.root}/log`
    `backend_http_server_host` | `dswarm.http.host` | `"localhost"`
    `backend_http_server_port` | `dswarm.http.port` | `8087`
    `ehcache_disk_store_dir` | N/A | N/A
    `tmp_path` | `dswarm.paths.logging` | `${dswarm.paths.root}/log`
    `dmp_graph_endpoint` | `dswarm.db.graph.endpoint` | `"http://localhost:7474/graph"`
    `reporting_es_host` | `dswarm.reporting.elasticsearch` | `"localhost:9200"`
    `db.mysql.url` | `dswarm.db.metadata.uri` | `"jdbc:metadata://"${dswarm.db.metadata.host}":"${dswarm.db.metadata.port}"/"${dswarm.db.metadata.schema}"?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"`
    `db.mysql.username` | `dswarm.db.metadata.username` | N/A
    `db.mysql.password` | `dswarm.db.metadata.password` | N/A

5. Remove all references to maven profiles in all start scripts/configurations you might have. That is `mvn -PDEV ...` becomes `mvn ...`

6. Add `-Dconfig.file=/path/to/dswarm.conf` to all maven start scripts/configurations you might have. That is `mvn ...` becomes `mvn -Dconfig.file=/path/to/dswarm.conf ...`

7. Provide the /path/to/dswarm.conf to your IDE. In eclipse, e.g., open `eclipse.ini`, go to `-vmargs` section and add the line `-Dconfig.file=/path/to/dswarm.conf`. 

8. At last, remove the dmp.properties file (Which might have been done already, as this step was committed via git)
