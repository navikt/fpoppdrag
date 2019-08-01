UPDATE GR_SIMULERING gr1
SET aktiv = 'N'
WHERE aktiv = 'J'
AND EXISTS ( SELECT *
             FROM GR_SIMULERING gr2
             WHERE gr1.behandling_id = gr2.behandling_id
             AND gr2.aktiv = 'J'
             AND gr2.opprettet_tid > gr1.opprettet_tid
           );