#!/usr/bin/env bash

export DEFAULTDS_URL=$(cat /var/run/secrets/nais.io/vault/oracle/config/jdbc_url)
export DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/vault/oracle/cred/username)
export DEFAULTDS_PASSWORD=$(cat /var/run/secrets/nais.io/vault/oracle/cred/password)
export SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/vault/serviceuser/username)
export SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/vault/serviceuser/password)
