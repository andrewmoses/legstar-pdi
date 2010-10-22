       PROCESS NOSEQ LIB OPTIMIZE(FULL) CODEPAGE(37) DBCS
       IDENTIFICATION DIVISION.
       PROGRAM-ID. TCOBWVB.
      * ----------------------------------------------------------------
      * A SAMPLE PROGRAM TO GENERATE A SEQUENTIAL FILE
      * ----------------------------------------------------------------
      * WRITES IN A RECFM=VB FILE
      * COBOL LOGICAL LENGTH IS BETWEEN 58 AND 183
      * QSAM LOGICAL RECORD IS BETWEEN 62 AND 187 (COBOL + RDW)
      * ----------------------------------------------------------------

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
      *SOURCE-COMPUTER. IBM-390 WITH DEBUGGING MODE.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT OUTPUT-FILE
           ASSIGN TO OUTFILE
           ORGANIZATION IS SEQUENTIAL
           ACCESS MODE IS SEQUENTIAL
           FILE STATUS IS OUTPUT-FILE-STATUS.

       DATA DIVISION.
       FILE SECTION.
       FD OUTPUT-FILE
           RECORDING MODE IS V
           BLOCK CONTAINS 2 RECORDS
           RECORD CONTAINS 58 TO 183 CHARACTERS.
       01  CUSTOMER-DATA.
           05 CUSTOMER-ID             PIC 9(6).
           05 PERSONAL-DATA.
              10 CUSTOMER-NAME        PIC X(20).
              10 CUSTOMER-ADDRESS     PIC X(20).
              10 CUSTOMER-PHONE       PIC X(8).
           05 TRANSACTIONS.
              10 TRANSACTION-NBR      PIC 9(9) COMP.
              10 TRANSACTION OCCURS 0 TO 5
                 DEPENDING ON TRANSACTION-NBR. 
                 15 TRANSACTION-DATE         PIC X(8).
                 15 FILLER REDEFINES TRANSACTION-DATE.
                    20 TRANSACTION-DAY       PIC X(2).
                    20 FILLER               PIC X.
                    20 TRANSACTION-MONTH     PIC X(2).
                    20 FILLER               PIC X.
                    20 TRANSACTION-YEAR      PIC X(2).
                 15 TRANSACTION-AMOUNT       PIC S9(13)V99 COMP-3.
                 15 TRANSACTION-COMMENT      PIC X(9).

       WORKING-STORAGE SECTION.
       01  W-I                        PIC 9(4) COMP.
       01  W-J                        PIC 9(4) COMP.
       01  OUTPUT-FILE-STATUS         PIC 9(2).

       PROCEDURE DIVISION.
           OPEN OUTPUT OUTPUT-FILE.
           IF OUTPUT-FILE-STATUS NOT = ZERO
              DISPLAY 'ERROR OPENING OUTPUT-FILE='
                      OUTPUT-FILE-STATUS
              GO TO PROGRAM-EXIT
           END-IF.
           PERFORM VARYING W-I FROM 1 BY 1 UNTIL W-I > 10
               MOVE W-I          TO CUSTOMER-ID
               MOVE 'JOHN SMITH' TO CUSTOMER-NAME
               MOVE 'CAMBRIDGE UNIVERSITY' TO CUSTOMER-ADDRESS
               MOVE '44012565' TO CUSTOMER-PHONE
               COMPUTE TRANSACTION-NBR = 5 * FUNCTION RANDOM
               PERFORM VARYING W-J FROM 1 BY 1
                       UNTIL W-J > TRANSACTION-NBR
                   MOVE '10/04/11' TO TRANSACTION-DATE (W-J)
                   MOVE 235.56 TO TRANSACTION-AMOUNT (W-J)
                   MOVE '*********' TO TRANSACTION-COMMENT (W-J)
               END-PERFORM
                      
               WRITE CUSTOMER-DATA
               IF OUTPUT-FILE-STATUS NOT = ZERO
                  DISPLAY 'ERROR WRITING TO OUTPUT-FILE='
                          OUTPUT-FILE-STATUS
                  GO TO PROGRAM-EXIT
               END-IF
           END-PERFORM.

       PROGRAM-EXIT.

           CLOSE OUTPUT-FILE.

           GOBACK.

       END PROGRAM TCOBWVB.