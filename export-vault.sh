#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/defaultDSconfig/jdbc_url;
then
  export DEFAULTDS_URL=$(cat /var/run/secrets/nais.io/defaultDSconfig/jdbc_url)
  echo "Setting DEFAULTDS_URL to $DEFAULTDS_URL"
else
  >&2 echo "Kunne ikke sette DEFAULTDS_URL"
  exit 1
fi

if test -f /var/run/secrets/nais.io/defaultDS/username;
then
  export DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/defaultDS/username)
  echo "Setting DEFAULTDS_USERNAME"
else
  >&2 echo "Kunne ikke sette nødvendig property DEFAULTDS_USERNAME"
  exit 1
fi

if test -f /var/run/secrets/nais.io/defaultDS/password;
then
  export DEFAULTDS_PASSWORD=$(cat /var/run/secrets/nais.io/defaultDS/password)
  echo "Setting DEFAULTDS_PASSWORD"
else
  >&2 echo "Kunne ikke sette nødvendig property DEFAULTDS_PASSWORD"
  exit 1
fi

if test -f /var/run/secrets/nais.io/serviceuser/username;
then
  export SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/serviceuser/username)
  echo "Setting SYSTEMBRUKER_USERNAME"
else
  >&2 echo "Kunne ikke sette nødvendig property SYSTEMBRUKER_USERNAME"
  exit 1
fi

if test -f /var/run/secrets/nais.io/serviceuser/password;
then
  export SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/serviceuser/password)
  echo "Setting SYSTEMBRUKER_PASSWORD"
else
  >&2 echo "Kunne ikke sette nødvendig property SYSTEMBRUKER_PASSWORD"
  exit 1
fi

if test -f /var/run/secrets/nais.io/ldap/username;
then
  export LDAP_USERNAME=$(cat /var/run/secrets/nais.io/ldap/username)
  echo "Setting LDAP_USERNAME"
else
  echo "Kunne ikke sette LDAP_USERNAME"
fi

if test -f /var/run/secrets/nais.io/ldap/password;
then
  export LDAP_PASSWORD=$(cat /var/run/secrets/nais.io/ldap/password)
  echo "Setting LDAP_PASSWORD"
else
  echo "Kunne ikke sette LDAP_PASSWORD"
fi
