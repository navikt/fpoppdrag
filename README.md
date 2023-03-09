FP-OPPDRAG
===============
[![Bygg og deploy](https://github.com/navikt/fpoppdrag/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/fpoppdrag/actions/workflows/build.yml)
[![Promote](https://github.com/navikt/fpoppdrag/actions/workflows/promote.yml/badge.svg?branch=master)](https://github.com/navikt/fpoppdrag/actions/workflows/promote.yml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_fpoppdrag)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=coverage)](https://sonarcloud.io/summary/new_code?id=navikt_fpoppdrag)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fpoppdrag)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_fpoppdrag)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=navikt_fpoppdrag)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_fpoppdrag)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=navikt_fpoppdrag)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpoppdrag&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_fpoppdrag)

Simulerer utfall av økonomioppdrag for å gi mulighet til å opprette tilbakebetaling

### Sikkerhet
Det er mulig å kalle tjenesten med bruk av følgende tokens
- Azure CC
- Azure OBO med følgende rettigheter:
    - fpsak-saksbehandler
    - fpsak-veileder
    - fpsak-drift
- STS (fases ut)
