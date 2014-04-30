#!/bin/bash

DIR="$( dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )" )"

MYSQL_PW=${MYSQL_PW:="dmp_mysql"}
MYSQL_UN=${MYSQL_UN:="dmp"}
MYSQL_DB=${MYSQL_DB:="dmp_dev"}

NEO4J_URL=${NEO4J_URL:="http://localhost:7474/graph"}

cd "${DIR}"

mysql -f -u${MYSQL_UN} -p${MYSQL_PW} ${MYSQL_DB} < persistence/src/main/resources/schema.sql
mysql -f -u${MYSQL_UN} -p${MYSQL_PW} ${MYSQL_DB} < persistence/src/main/resources/functions.sql
mysql -f -u${MYSQL_UN} -p${MYSQL_PW} ${MYSQL_DB} < persistence/src/main/resources/init_internal_schema.sql

curl -v -X DELETE "${NEO4J_URL}/maintain/delete"
