#!/usr/bin/env bash

# Check that the required network is present
NETWORKS=$(docker network ls -f 'name=example_palisade' --format '{{.Name}}')
if [[ $(grep -c example_palisade <(echo $NETWORKS)) -lt 1 ]]; then
    echo "Docker network \"example_palisade\" is not present. Do you need to run the dockerComposeUp.sh script?"
    exit 1
fi

docker build -t example-docker-runner "../example-docker-services/client/"
docker run --network=example_palisade_network --rm example-docker-runner | ./formatOutput.sh
