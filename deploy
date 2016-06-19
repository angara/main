#!/bin/bash

PROD_HOST="anga@mph"
PROD_PATH="/www/usr"

# ssh ${PROD_HOST} "(cd $PROD_PATH && mkdir -p inc var)"

scp tmp/uberjar/angara-usr.jar ${PROD_HOST}:${PROD_PATH}
scp var/prod.edn var/bb-categ.edn ${PROD_HOST}:${PROD_PATH}/var/
rsync -e ssh -avz inc/ ${PROD_HOST}:${PROD_PATH}/inc/
#touch run.sh
#scp run.sh ${PROD_HOST}:${PROD_PATH}

ssh ${PROD_HOST} touch ${PROD_PATH}/run.sh

#.
