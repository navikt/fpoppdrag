# fpoppdrag

Payment simulation and oppdragslager service used in the Foreldrepenger payment flow.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic          | Details                                                          |
|----------------|------------------------------------------------------------------|
| Role           | Provides simulation results and persistence                      |
| Consumers      | `fp-sak` and `fptilbake` (dates for warning letter)              |
| Tech stack     | Standard fp Java backend                                         |
| Integrations   | `fp-ws-proxy`, names from PDL and EREG                           |
| Data           | Oracle; FSS deployment; long-term storage of simulation results  |

## Entry points

- `SimuleringRestTjeneste`: start simulations, fetch result variants

## Verification

- For integration impact, verify via `navikt/fp-autotest`.
- Most relevant suite: `verdikjede`.
