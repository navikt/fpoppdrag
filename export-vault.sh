#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/vault/oracle/config/jdbc_url; 
then
  export DEFAULTDS_URL=$(cat /var/run/secrets/nais.io/vault/oracle/config/jdbc_url)
  echo "Setting DEFAULTDS_URL to $DEFAULTDS_URL"
fi

if test -f /var/run/secrets/nais.io/vault/oracle/cred/username; 
then
  export DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/vault/oracle/cred/username)
  echo "Setting DEFAULTDS_USERNAME"
fi

if test -f /var/run/secrets/nais.io/vault/oracle/cred/password; 
then
  export DEFAULTDS_PASSWORD=$(cat /var/run/secrets/nais.io/vault/oracle/cred/password)
  echo "Setting DEFAULTDS_PASSWORD"
fi

if test -f /var/run/secrets/nais.io/vault/serviceuser/username; 
then
  export SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/vault/serviceuser/username)
  echo "Setting SYSTEMBRUKER_USERNAME"
fi

if test -f /var/run/secrets/nais.io/vault/serviceuser/password; 
then
  export SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/vault/serviceuser/password)
  echo "Setting SYSTEMBRUKER_PASSWORD"
fi
