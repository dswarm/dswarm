# initial installation

premise:
- empty trusty (14.04)
- three additional partitions: /data/log, /data/mysql, /data/neo4j
- let the $HOME of the less privileged user be '/home/user'
- all commands boxes start as the less privileges user and in their $HOME.
- Requiring root is explicitly markes (with `su` rather than `sudo ...`)

_Note: some commands require user input, this is no unattended installation_


**These steps require root level access**


**1**. install system packages required for running the software

```
su
apt-get install --no-install-recommends --yes mysql-server nginx tomcat7 openjdk-7-jdk
```

- note: neo4j suggest to install oracle-jdk instead of openjdk, to do so, please execute the following commands (see also http://community.linuxmint.com/tutorial/view/1414)

```
su
add-apt-repository ppa:webupd8team/java
apt-get update
apt-get install oracle-java7-installer
```

- you can check your java version with

```
java -version
```

- if it's not oracle-jdk, then

```
su
update-java-alternatives -s java-7-oracle
```

- optionally, you can also set the environment variables to oracle-jdk with

```
su
apt-get install oracle-java7-set-default
```

**2**. install system packages required for building the software

```
su
apt-get install --no-install-recommends --yes git-core maven nodejs npm build-essential
```

**3**. install Neo4j

```
su
wget -O - http://debian.neo4j.org/neotechnology.gpg.key| apt-key add -
echo 'deb http://debian.neo4j.org/repo stable/' > /etc/apt/sources.list.d/neo4j.list
apt-get update
apt-get install --no-install-recommends --yes neo4j
```

**4**. make sure, permissions are correctly

```
su
chown -R tomcat7:tomcat7 /data/log
chown -R mysql:mysql /data/mysql
chown -R neo4j:adm /data/neo4j
```

**5**. install build environment for frontend

```
su
ln -s /usr/bin/nodejs /usr/bin/node
npm install -g grunt-cli karma bower
```

**6**. setup MySQL

```
echo <<EOT | mysql -uroot -p
CREATE DATABASE IF NOT EXISTS dmp DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_bin;
CREATE USER 'dmp'@'localhost' IDENTIFIED BY 'dmp';
GRANT ALL PRIVILEGES ON dmp.* TO 'dmp'@localhost IDENTIFIED BY 'dmp';
FLUSH PRIVILEGES;
EOT
```

then, open `/etc/mysql/my.cnf` and add the following line to the section `[mysqld]` (around line 45)

```
wait_timeout = 1209600
```

in the same file, same sections, change `datadir` to `/data/mysql` (around line 40)

```
datadir         = /data/mysql
```

and add this directory to AppArmor

```
su
echo "alias /var/lib/mysql/ -> /data/mysql/," >> /etc/apparmor.d/tunables/alias
/etc/init.d/apparmor reload
```


**7**  setup Nginx

edit `/etc/nginx/sites-available/default` and add this just below the `location /` block

```
location /dmp {
        client_max_body_size 100M;
        proxy_pass http://127.0.0.1:8080$uri$is_args$args;
}
```

move old content root and link the new one. lookout for the correct user path! (the directory will be created later on)

```
su
mv /usr/share/nginx/{html,-old}
ln -s /home/user/dmp-backoffice-web/yo/publish /usr/share/nginx/html
```

**8**. setup tomcat

open /etc/tomcat7/server.xml at line 33 and add a `driverManagerProtection="false"` so that the line reads

```
<Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" driverManagerProtection="false" />
```

at line 73, same file, add this option `maxPostSize="104857600"`, so that the Connector block reads

```
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               maxPostSize="104857600"
               URIEncoding="UTF-8"
               redirectPort="8443" />
```

then, give tomcat some more memory

```
su
echo 'CATALINA_OPTS="-Xms4G -Xmx4G -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:MaxPermSize=512M"' >> /usr/share/tomcat7/bin/setenv.sh
```

**9**. setup neo4j

increase file handlers at `/etc/security/limits.conf`

```
root   soft    nofile  40000
root   hard    nofile  40000
```

plus add `ulimit -n 40000` into your neo4j service script (under `/etc/init.d', e.g., `/etc/init.d/neo4j-service`) before starting the daemon

edit `/etc/neo4j/neo4j.properties` and:

- insert some storage tweaks (from http://blog.bruggen.com/2014/02/some-neo4j-import-tweaks-what-and-where.html)

```
use_memory_mapped_buffers=true
neostore.nodestore.db.mapped_memory=1024M
neostore.relationshipstore.db.mapped_memory=4G
neostore.propertystore.db.mapped_memory=1024M
neostore.propertystore.db.strings.mapped_memory=1024M
cache_type=weak
```

edit `/etc/neo4j/neo4j-server.properties` and:

- at line 12, change the database location

```
org.neo4j.server.database.location=/data/neo4j/data/graph.db
```

- at line 53, change the database location

```
org.neo4j.server.webadmin.rrdb.location=/data/neo4j/data/rrd
```

- at line 75 and add our graph extension

```
org.neo4j.server.thirdparty_jaxrs_classes=de.avgl.dmp.graph.resources=/graph
```

edit `/etc/neo4j/neo4j-wrapper.conf` and:

- insert an additional parameter (if your server is x64)

```
wrapper.java.additional.1=-d64
```

- tweak the java heap space size to an appropriate value according to your server ram memory, e.g.,

```
wrapper.java.initmemory=512
wrapper.java.maxmemory=8192
```

edit `/etc/neo4j/logging.properties` and change line 54 to be

```
java.util.logging.FileHandler.pattern=/data/neo4j/log/neo4j.%u.%g.log
```

then, create a symlink from the previous log location to the external partition

```
su
mv /var/lib/neo4j/data/{log,-old}
ln -s /data/neo4j/log /var/lib/neo4j/data/log
mkdir /data/neo4j/log
```

* * *

**These steps require less privileged access**


**10**. create ssh key

```
ssh-keygen -t rsa -b 2048 -f ~/.ssh/id_rsa -N ''
```

**11**. add ssh key to deployment hooks in gitlab

- copy the contents of the public key at `~/.ssh/id_rsa.pub`
- open https://git.slub-dresden.de/dmp/datamanagement-platform/deploy_keys/new to add a new deploy key
  - enter a title (e.g. user@host)
  - paste the public key
- open https://git.slub-dresden.de/dmp/dmp-graph/deploy_keys and click on `Enable` next to the just added key
- repeat for https://git.slub-dresden.de/dmp/dmp-backoffice-web/deploy_keys

**12**. clone repositories

```
git clone --depth 1 --branch builds/unstable git@git.slub-dresden.de:dmp/datamanagement-platform.git
git clone --depth 1 --branch master git@git.slub-dresden.de:dmp/dmp-graph.git
git clone --depth 1 --branch builds/unstable git@git.slub-dresden.de:dmp/dmp-backoffice-web.git
```

**13**. build neo4j extension

```
pushd dmp-graph
mvn -U -PRELEASE -DskipTests clean package
popd
mv dmp-graph/target/graph-1.0-jar-with-dependencies.jar dmp-graph.jar
```

**14**. build backend

```
pushd datamanagement-platform
mvn -U -PSDVDSWARM01 -DskipTests clean install
pushd controller
mvn -U -PSDVDSWARM01 -DskipTests war:war
popd; popd
mv datamanagement-platform/controller/target/controller-0.1-SNAPSHOT.war dmp.war
```

**15**. build frontend

```
pushd dmp-backoffice-web; pushd yo
npm install
bower install
STAGE=unstable DMP_HOME=../../datamanagement-platform grunt build
popd
rsync --delete --verbose --recursive yo/dist/ yo/publish
popd
```

* * *

**These steps require root level access**


**16**. wire everything together

lookout for the correct path (/home/user)

```
su
rm /var/lib/tomcat7/webapps/dmp.war
rm -r /var/lib/tomcat7/webapps/dmp
cp /home/user/dmp.war /var/lib/tomcat7/webapps/
cp /home/user/dmp-graph.jar /usr/share/neo4j/plugins/
```


**17**. restart everything, if needed

```
su
/etc/init.d/mysql restart
/etc/init.d/neo4j-service restart
/etc/init.d/nginx restart
/etc/init.d/tomcat7 restart
```


# updates

**1**. update repository contents

```
pushd datamanagement-platform; git pull; popd
pushd dmp-graph; git pull; popd
pushd dmp-backoffice-web; git pull; popd
```

**2**. repeat steps 13 to 17 from installation as necessary
