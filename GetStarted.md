# Introduction #

This document guides you through the process of creating a PDI Transformation with 2 steps:

  * The LegStar **z/OS File Input** step which will read raw binary data from a file received from a mainframe

  * The **Excel Output** step which will create an excel worksheet with the transformed data

# Preliminary task #

Get the mainframe binary file samples from [this link](http://legstar-pdi.googlecode.com/svn/trunk/legstar.pdi.zosfile/src/main/file). Copy these files to your machine at a location of your choice.

These file records are variable length.

The content of these 2 files is almost identical. The only difference is that the one with suffix RDW contains record descriptor words, while the other one does not. Processing variable record files with RDW is faster but LegStar can handle both types.

See [How the sample z/OS files were created](#How_the_sample_z/OS_files_were_created.md) for information on how these files were produced. Observe that there are no conversions performed on the mainframe. These files are binary images of mainframe files, obtained via FTP.

This is the COBOL structure that describes records in these files:
```
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
                    20 FILLER                PIC X.
                    20 TRANSACTION-MONTH     PIC X(2).
                    20 FILLER                PIC X.
                    20 TRANSACTION-YEAR      PIC X(2).
                 15 TRANSACTION-AMOUNT       PIC S9(13)V99 COMP-3.
                 15 TRANSACTION-COMMENT      PIC X(9).

```
Transforming a record conforming to this COBOL structure into an Excel worksheet row is not straightforward without LegStar's help. Here are some characteristics of this structure which are common in COBOL:

  * The structure is a 4 levels deep hierarchy that needs to be flatten to fit in a row
  * The content of the CUSTOMER-NAME character string is in EBCDIC
  * TRANSACTION-NBR is an integer in Big endian byte ordering
  * TRANSACTION is a variable size array which size is given by TRANSACTION-NBR
  * TRANSACTION-DATE is being redefined by an, anonymous, FILLER structure
  * TRANSACTION-AMOUNT is a compressed numeric

So let's see how LegStar for PDI deals with such a structure and let's start creating our PDI transformation.

# Create a PDI Transformation #

Start or restart the PDI spoon UI ($KETTLE\_DIR/spoon.sh or %KETTLE\_DIR%/Spoon.bat).

Drag/Drop the **z/OS File Input** step from the Input category and the **Microsoft Excel Output** step from the Output category onto the canvas.

Create a hop between these steps. You should now have a screen looking like this:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-stepsandhop-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-stepsandhop-scr.png)

# Setup the PDI steps #

Double click on the z/OS File Input step to bring up the setup dialog:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-setupinitial-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-setupinitial-scr.png)

You can change the step name if you like.

## Locate the z/OS input file ##

We need to tell the step where the z/OS file resides. You can use the browse button to locate the files you [downloaded](http://legstar-pdi.googlecode.com/svn/trunk/legstar.pdi.zosfile/src/main/file) in the preliminary step.

You must also check the "Variable Length" option.

You have 2 sample files. One where records start with a 4 bytes record descriptor word and the second one without such an RDW. Check the RDW option depending on the file you selected.

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-filelocation-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-filelocation-scr.png)

The next parameter we will be setting is the host character set. The default IBM01140 character set is for US English EBCDIC. Your own mainframe might be setup for a different EBCDIC character set such as IBM1147 for France:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-hostcharset-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-hostcharset-scr.png)

The character sets listed are taken directly from your java VM. If you don't see any IBMxxx character sets listed, you need to get a JDK or an international version of the JRE.

In the case of the files you downloaded, the character set is IBM01140 so you can keep the default.

## Specify the COBOL structure that describes records ##

Select the COBOL tab on the setup dialog:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-cobolinputtype-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-cobolinputtype-scr.png)

Here we will select the COBOL structure that describes the z/OS file records.

The **Import COBOL** button allows you to pick up COBOL source code from you file system.

Alternatively, you can copy/paste the code. Just select all the lines from the [COBOL structure we saw previously](#Preliminary_task.md) and paste them to the text box.

By default, COBOL code is assumed to be fixed format which means it should start after column 7 and not extend past column 72. We will see later how you can [customize legstar-pdi](#Customizing_the_COBOL_Tranformer_generation.md) to accept free format COBOL.

Your screen should now look like this:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-copypasteresult-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-copypasteresult-scr.png)

## Get the PDI fields ##

Select the last tab called Fields from the setup dialog. It should be empty at this stage:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-getfields-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-getfields-scr.png)

You will need to click on the **Get Fields** button to populate the list.

There is quite a bit of processing that goes on to infer a list of PDI fields from the COBOL code that was previously specified.

A progress dialog, that you can cancel at any time, should appear while the background process is going on:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-progressdialog-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-progressdialog-scr.png)

Back from this dialog, you will notice that the fields list is now populated. Each field maps to an elementary data item from the corresponding COBOL structure:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-showfields-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-showfields-scr.png)

LegStar for PDI makes a number of inferences based on the COBOL data item types and the PDI constraints. One important PDI constraint is that data must fit the row model (in the sense of a table row) in order to move from step to step. Hierarchical structures, such as COBOL structures, have therefore to be flattened, arrays are not directly supported, etc.

## Preview ##

Once fields are generated, the setup is complete.

You can now click on the **Preview** button, this dialog should popup:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-previewsize-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-previewsize-scr.png)

Once you select the number of lines you would like to see and click on **OK**, you should get your first glimpse at the transformed mainframe data:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-previewresult-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-previewresult-scr.png)

## Setup the Excel step and save ##

It is now time to turn to the Excel Output step. If you double-click on the step, the only parameter you need to set is the location of the output Excel worksheet:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-exceloutput-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-exceloutput-scr.png)

Probably a good time to save the PDI transformation. You can give it whatever name you like and store it on your file system as a .ktr file or in a PDI repository.

# Run the PDI transformation #

You can run the transformation directly or in debug mode. On this dialog simply click launch:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-launch-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-launch-scr.png)

Upon return, the metrics should display the number of rows produced (written) by the z/OS File Input step and read by the Excel Output step to be finally written to the worksheet:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-launchmetrics-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-launchmetrics-scr.png)

Finally, if you open the excel worksheet that you produced, you should get something like this:

![http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-excelworksheet-scr.png](http://legstar-pdi.googlecode.com/svn/wiki/images/getstarted-excelworksheet-scr.png)

At this stage you have a working example of a transformation that takes raw binary mainframe data as input and turns it into an excel worksheet.

# Additional info for the curious #

## Customizing the COBOL Tranformer generation ##

When you clicked on the **Get Fields** button, the LegStar COBOL Transformer generator was launched in the background.

This generator takes the COBOL source you provided and generates java code which will be able to transform raw z/OS records to java objects. Internally, LegStar refers to these generated classes as COBOL Transformers.

The COBOL Transformers are based on [JAXB](http://www.oracle.com/technetwork/articles/javase/index-140168.html), a standard java technology. LegStar adds COBOL annotations to the standard JAXB classes.

There are a number of parameters that you can set to change the behavior of the LegStar COBOL Transformers generator. These parameters values come from a file called cob2trans.properties located in folder $KETTLE\_DIR/plugins/legstar.pdi.zosfile/conf.

We won't discuss all these options here but there is at least one important parameter you might have to set: if your COBOL code is free format (meaning it does not start at column 8), you will need to set:
```
codeFormat = FREE_FORMAT
```

You need to do this before you click on the **Get Fields** button. If you don't change that parameter, LegStar will ignore anything starting before column 8.

## How the sample z/OS files were created ##

These files were created on z/OS using a COBOL batch program called [PCUSTWVB](http://code.google.com/p/legstar-pdi/source/browse/trunk/legstar.pdi.zosfile/src/main/cobol/PCUSTWVB.cbl).

The JCL used to produce the sequential file looks like this:
```
//P390RWVB   JOB  (ACCT#),'FADY',
//           MSGCLASS=X,NOTIFY=&SYSUID,PRTY=14,REGION=0M
//*********************************************************************
//RUN  EXEC PGM=PCUSTWVB,REGION=1024K,PARM='000010000'
//STEPLIB  DD DSN=P390.DEV.LOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//OUTFILE  DD DISP=(MOD,CATLG,DELETE),DSN=P390.DEV.FCUSTDAT,
//           SPACE=(TRK,(45,15)),UNIT=3390,
//           DCB=(BLKSIZE=374,LRECL=187,RECFM=VB)
```

The ZOS.FCUSTDAT.bin file was received from the mainframe with a simple FTP in binary mode with the following command:
```
ftp> bin
200 Representation type is Image
ftp> get DEV.FCUSTDAT ZOS.FCUSTDAT.bin
200 Port request OK.
125 Sending data set P390.DEV.FCUSTDAT
250 Transfer completed successfully.
```

The second one, ZOS.FCUSTDAT.RDW.bin was also obtained using FTP but a special z/OS FTP command **site RDW** that preserves the Record Descriptor Words. The RDW is a 4 bytes header that z/OS prepends to variable records. The RDW contains the record actual length:
```
ftp> bin
200 Representation type is Image
ftp> quote site RDW
200 SITE command was accepted
ftp> get DEV.FCUSTDAT ZOS.FCUSTDAT.RDW.bin
200 Port request OK.
125 Sending data set P390.DEV.FCUSTDAT
250 Transfer completed successfully.
```

## What is the COBOL-annotated JAXB class option used for ##

You might have noticed on the [z/OS File Input setup dialog](#Specify_the_COBOL_structure_that_describes_records.md), on the COBOL tab, that there was an alternative option to COBOL source.

This is useful if you are already a LegStar user and might have some COBOL Transformers you created with LegStar, outside the PDI designer, that you wish to use with PDI.

If you have LegStar COBOL Transformers created outside PDI, just package them in a jar archive and drop the archive in $KETTLE\_DIR/plugins/legstar.pdi.zosfile/user.

Then when you setup the z/OS File input step in the PDI designer, select the **COBOL-annotated JAXB class** option on the COBOL tab rather than importing COBOL source code.

You are then presented with a list of JAXB classes with COBOL annotations produced by LegStar. You can pick up the one that describes your z/OS file record and then click on "Get Fields" to populate the PDI fields tab.