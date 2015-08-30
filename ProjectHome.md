# Introduction #

This project brings [LegStar](http://www.legsem.com/legstar/) and [Pentaho Data Integration](http://www.pentaho.com/product/data-integration) (Kettle) together.

The objective is to simplify integration with IBM mainframe data files such as QSAM, VSAM or IMS-DB segments.

[LegStar](http://www.legsem.com/legstar/) is a code generator that uses COBOL copybooks to generate java [Transformers](http://www.legsem.com/legstar/legstar-core/legstar-coxbgen) classes.

**LegStar for PDI** is a PDI step that reads mainframe records from a file and transforms them into PDI rows.

You can combine the LegStar for PDI step with any of the native [PDI steps](http://wiki.pentaho.com/display/EAI/Pentaho+Data+Integration+Steps) to form complex PDI transformations:

![http://legstar-pdi.googlecode.com/svn/wiki/images/welcome-complextrans.png](http://legstar-pdi.googlecode.com/svn/wiki/images/welcome-complextrans.png)

# Use case #

IBM mainframe file records are usually described by COBOL copybooks. Records are fixed or variable length.

Records are likely to contain data items encoded in mainframe specific formats:

  * EBCDIC character strings without delimiters

  * compressed numerics (COMP, COMP-3)

  * numerics with overpunch sign characters

  * variable size arrays (depending on)

  * redefines, ...

> [.md](.md)

It is relatively easy to copy such files to a machine running Pentaho Data Integration but what is needed is some means of reading the content and convert it to a format which makes it easy for PDI steps to manipulate.

# Status #

This is an early, but operational, release of the product.

We are looking for feedbacks from the community.

You are welcome to [install](Installation.md) the product and follow instructions on the [get started](GetStarted.md) document.
