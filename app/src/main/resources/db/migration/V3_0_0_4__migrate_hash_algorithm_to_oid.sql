UPDATE TSP_REQUEST
SET HASH_ALGORITHM_IDENTIFIER = CASE HASH_ALGORITHM
                                    WHEN 'SHA1' THEN '1.3.14.3.2.26'
                                    WHEN 'SHA256' THEN '2.16.840.1.101.3.4.2.1'
                                    WHEN 'SHA512' THEN '2.16.840.1.101.3.4.2.3'
    END;