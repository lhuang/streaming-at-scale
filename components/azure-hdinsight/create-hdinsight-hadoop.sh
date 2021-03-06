#!/bin/bash

set -euo pipefail

echo 'creating storage container'
echo ". name: $HDINSIGHT_YARN_NAME"

az storage container create --account-name $AZURE_STORAGE_ACCOUNT -n $HDINSIGHT_YARN_NAME \
    -o tsv >> log.txt

echo 'creating HDInsight cluster'
echo ". name: $HDINSIGHT_YARN_NAME"

az hdinsight create -t hadoop -g $RESOURCE_GROUP -n $HDINSIGHT_YARN_NAME \
  -p "$HDINSIGHT_PASSWORD" \
  --version 4.0 \
  --zookeepernode-size Standard_D2_V2 \
  --headnode-size Standard_E2_V3 \
  --workernode-size $HDINSIGHT_HADOOP_WORKER_SIZE --workernode-count $HDINSIGHT_HADOOP_WORKERS \
  --vnet-name $VNET_NAME --subnet ingestion-subnet \
  --storage-account $AZURE_STORAGE_ACCOUNT \
  --storage-container $HDINSIGHT_YARN_NAME \
  -o tsv >> log.txt

echo 'Enable HDInsight monitoring'
echo ". workspace: $LOG_ANALYTICS_WORKSPACE"

az hdinsight monitor enable -g $RESOURCE_GROUP -n $HDINSIGHT_YARN_NAME --workspace $LOG_ANALYTICS_WORKSPACE \
  -o tsv >> log.txt
