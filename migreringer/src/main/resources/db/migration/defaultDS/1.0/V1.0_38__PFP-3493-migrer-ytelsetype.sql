UPDATE GR_SIMULERING GR1 SET YTELSE_TYPE = 'FP'
WHERE GR1.YTELSE_TYPE = '-'
AND EXISTS (SELECT 0 FROM GR_SIMULERING GR2 inner join SIMULERING_MOTTAKER SM ON GR2.SIMULERING_ID = SM.SIMULERING_ID
                                            inner join SIMULERT_POSTERING SP ON SM.ID = SP.SIMULERING_MOTTAKER_ID
            WHERE GR2.ID = GR1.ID
            AND SP.FAG_OMRAADE_KODE IN ('FP', 'FPREF'));


UPDATE GR_SIMULERING GR1 SET YTELSE_TYPE = 'ES'
WHERE GR1.YTELSE_TYPE = '-'
AND EXISTS (SELECT 0 FROM GR_SIMULERING GR2 inner join SIMULERING_MOTTAKER SM ON GR2.SIMULERING_ID = SM.SIMULERING_ID
                                            inner join SIMULERT_POSTERING SP ON SM.ID = SP.SIMULERING_MOTTAKER_ID
            WHERE GR2.ID = GR1.ID
            AND SP.FAG_OMRAADE_KODE = 'REFUTG');