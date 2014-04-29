# initial installation

premise:
- empty trusty (14.04)
- three additional partitions: /data/log, /data/mysql, /data/neo4j
- commands starting with `#` are to be executed as root
- commands starting with `$` are to be executed as a less privileged user
- let the $HOME of the less privileged user be '/home/user'

_Note: some commands require user input, this is no unattended installation_


**1**. install system packages required for running the software

```
# apt-get install --no-install-recommends --yes mysql-server nginx tomcat7 openjdk-7-jdk
```

**2**. install system packages required for building the software

```
# apt-get install --no-install-recommends --yes git-core maven nodejs npm build-essential
```

**3**. install Neo4j

```
# wget -O - http://debian.neo4j.org/neotechnology.gpg.key| apt-key add -
# echo 'deb http://debian.neo4j.org/repo stable/' > /etc/apt/sources.list.d/neo4j.list # Create an Apt sources.list file
# apt-get update
# apt-get install --no-install-recommends --yes neo4j
```

**4**. install build environment for frontend

```
# ln -s /usr/bin/nodejs /usr/bin/node
# npm install -g grunt-cli karma bower
```

**5**. setup MySQL

```
# echo <<EOT | mysql -uroot -p
CREATE DATABASE IF NOT EXISTS dmp DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_bin;
CREATE USER 'dmp'@'localhost' IDENTIFIED BY 'dmp';
GRANT ALL PRIVILEGES ON dmp.* TO 'dmp'@localhost IDENTIFIED BY 'dmp';
FLUSH PRIVILEGES;
EOT
```

then, add `wait_timeout = 1209600` to the section `[mysqld]` and restart mysql.

**6**  setup Nginx

edit `/etc/nginx/sites-available/default` and add this just below the `location /` block

```
location /dmp {
        client_max_body_size 100M;
        proxy_pass http://127.0.0.1:8080$uri$is_args$args;
}
```

move old content root and link the new one. lookupt for the correct user path! (the directory will be created later on)

```
# mv /usr/share/nginx/{html,.old}
# ln -s /home/user/dmp-backoffice-web/yo/publish /usr/share/nginx/html
```

**7**. setup tomcat

open /etc/tomcat7/server.xml at line 33 and add a `driverManagerProtection="false"` so that the line reads

```
<Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" driverManagerProtection="false" />
```

then give tomcat some more memory

```
# echo 'CATALINA_OPTS="-Xms4G -Xmx4G -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:MaxPermSize=512M"' >> /usr/share/tomcat7/bin/setenv.sh
```

**8**. setup neo4j

edit `/etc/neo4j/neo4j-server.properties` around line 75, the following line has to be enabled and set

```
org.neo4j.server.thirdparty_jaxrs_classes=de.avgl.dmp.graph.resources=/graph
```


**9**. create ssh key

```
$ ssh-keygen -t rsa -b 2048 -f ~/.ssh/id_rsa -N ''
```

**10**. add ssh key do deployment hooks in gitlab

- copy the contents of the public key at `~/.ssh/id_rsa.pub`
- open https://git.slub-dresden.de/dmp/datamanagement-platform/deploy_keys/new to add a new deploy key
  - enter a title (e.g. user@host)
  - paste the public key
- open https://git.slub-dresden.de/dmp/dmp-graph/deploy_keys and click on `Enable` next to the just added key
- repeat for https://git.slub-dresden.de/dmp/dmp-backoffice-web/deploy_keys

**11**. clone repositories

```
$ git clone --depth 1 --branch builds/unstable git@git.slub-dresden.de:dmp/datamanagement-platform.git
$ git clone --depth 1 --branch master git@git.slub-dresden.de:dmp/dmp-graph.git
$ git clone --depth 1 --branch builds/unstable git@git.slub-dresden.de:dmp/dmp-backoffice-web.git
```

**12**. build neo4j extension

```
$ pushd dmp-graph
$ mvn -U -PRELEASE -DskipTests clean package
$ popd
$ mv dmp-graph/target/graph-1.0-jar-with-dependencies.jar dmp-graph.jar
```

**13**. build backend

TODO: create new profile

edit `datamanagement-platform/init/src/test/filters/dmp-sdvdmpdev.properties` with the following chages
```
logging_root_path=/data/log/logs/dmp
tmp_path=/data/log/tmp
```

then

```
$ pushd datamanagement-platform
$ mvn -U -PSDVDMPDEV -DskipTests clean install -Ddb.mysql.username=dmp -Ddb.mysql.password=dmp
$ pushd controller
$ mvn -U -PSDVDMPDEV -DskipTests war:war -Ddb.mysql.username=dmp -Ddb.mysql.password=dmp
$ popd; popd
$ mv datamanagement-platform/controller/target/controller-0.1-SNAPSHOT.war dmp.war
```

**14**. build frontend

_Note:_ as of 2014-04-30, npm has a rather [nasty](https://github.com/npm/npm/issues/5157) [bug](https://github.com/npm/npm/issues/5162), making it unable to build the frontend the short way.
However,when  the missing transitive dependency doesn't seem to be required, you can try build the long way


short way:
```
$ pushd dmp-backoffice-web
$ make dist
$ popd
```

longer way:
```
$ pushd dmp-backoffice-web
$ pushd yo
$ npm install
$ bower install
$ STAGE=unstable DMP_HOME=../../datamanagement-platform grunt build
$ popd
$ rsync --delete --verbose --recursive yo/dist/ yo/publish
$ popd
```

**15**. wire everything together

lookout for the correct path (/home/user)

```
# rm -r /var/lib/tomcat7/webapps/dmp
# rm /var/lib/tomcat7/webapps/dmp.war
# cp /home/user/dmp.war /var/lib/tomcat7/webapps/
# cp /home/user/dmp-graph.jar /usr/share/neo4j/plugins/
```

**16**. restart everything, if needed

```
# /etc/init.d/mysql restart
# /etc/init.d/neo4j-service restart
# /etc/init.d/nginx restart
# /etc/init.d/tomcat7 restart
```

# updates

**1**. update repository contents

```
$ pushd datamanagement-platform; git pull; popd
$ pushd dmp-graph; git pull; popd
$ pushd dmp-backoffice-web; git pull; popd
```

**2**. repeat steps 12 to 15 from installation as necessary



TODO: move data from mysql to /data
TODO: move data from neo4j to /data
TODO: tomcat server.xml
```
<Connector port="8088" protocol="HTTP/1.1"
               connectionTimeout="20000"
               maxPostSize="104857600"
               URIEncoding="UTF-8"
               redirectPort="8443" />
```



