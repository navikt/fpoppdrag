#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/defaultDS/username;
then
  export DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/defaultDS/username)
  echo "Setting DEFAULTDS_USERNAME"
fi

if test -f /var/run/secrets/nais.io/defaultDS/password;
then
  export DEFAULTDS_PASSWORD=$(cat /var/run/secrets/nais.io/defaultDS/password)
  echo "Setting DEFAULTDS_PASSWORD"
fi

if test -f /var/run/secrets/nais.io/serviceuser/username;
then
  export SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/serviceuser/username)
  echo "Setting SYSTEMBRUKER_USERNAME"
fi

if test -f /var/run/secrets/nais.io/serviceuser/password;
then
  export SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/serviceuser/password)
  echo "Setting SYSTEMBRUKER_PASSWORD"
fi
