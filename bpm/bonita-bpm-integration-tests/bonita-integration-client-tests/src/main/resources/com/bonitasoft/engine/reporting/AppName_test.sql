SELECT
    APS.PROCESSID AS APS_PROCESS_ID,
    APS.NAME AS APS_NAME
FROM process_definition APS
WHERE APS.PROCESSID = 1