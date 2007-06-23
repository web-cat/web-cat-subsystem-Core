/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import net.sf.webcat.dbupdate.UpdateSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * This class captures the SQL database schema for the database tables
 * underlying the Core subsystem and the Core.eomodeld.  Logging output
 * for this class uses its parent class' logger.
 *
 * @author  Stephen Edwards
 * @version $Id$
 */
public class CoreDatabaseUpdates
    extends UpdateSet
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * The default constructor uses the name "core" as the unique identifier
     * for this subsystem and EOModel.
     */
    public CoreDatabaseUpdates()
    {
        super( "core" );
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Creates all tables in their baseline configuration, as needed.
     * @throws SQLException on error
     */
    public void updateIncrement0() throws SQLException
    {
        createEoPkTable();
        createAuthenticationDomainTable();
        createCoreSelectionsTable();
        createCourseTable();
        createCourseOfferingTable();
        createCourseStudentTable();
        createCourseTATable();
        createDepartmentTable();
        createInstructorCourseTable();
        createLanguageTable();
        createLoginSessionTable();
        createSemesterTable();
        createUserTable();
    }


    // ----------------------------------------------------------
    /**
     * This performs some simple column value maintenance to repair a
     * bug in an earlier Web-CAT version.  It resets all the
     * updateMutableFields columns to all zeroes.
     * @throws SQLException on error
     */
    public void updateIncrement1() throws SQLException
    {
        database().executeSQL(
            "UPDATE TUSER SET CUPDATEMUTABLEFIELDS = 0" );
    }


    // ----------------------------------------------------------
    /**
     * Changes course CRNs to strings.
     * @throws SQLException on error
     */
    public void updateIncrement2() throws SQLException
    {
        database().executeSQL(
            "alter table TCOURSEOFFERING change CCRN CCRN TINYTEXT" );
    }


    // ----------------------------------------------------------
    /**
     * Changes course CRNs to strings.
     * @throws SQLException on error
     */
    public void updateIncrement3() throws SQLException
    {
        database().executeSQL(
            "alter table TDEPARTMENT add CINSTITUTIONID INTEGER" );
    }


    // ----------------------------------------------------------
    /**
     * Changes login session ids to text.
     * @throws SQLException on error
     */
    public void updateIncrement4() throws SQLException
    {
        database().executeSQL(
            "alter table TLOGINSESSION change CSESSIONID CSESSIONID TINYTEXT" );
    }


    // ----------------------------------------------------------
    /**
     * Adds timeFormat and dateFormat keys to AuthenticationDomain.
     * @throws SQLException on error
     */
    public void updateIncrement5() throws SQLException
    {
        database().executeSQL(
            "alter table TAUTHENTICATIONDOMAIN add CTIMEFORMAT TINYTEXT" );
        database().executeSQL(
            "alter table TAUTHENTICATIONDOMAIN add CDATEFORMAT TINYTEXT" );
    }


    // ----------------------------------------------------------
    /**
     * Adds timeZoneName keys to AuthenticationDomain.
     * @throws SQLException on error
     */
    public void updateIncrement6() throws SQLException
    {
        database().executeSQL(
            "alter table TAUTHENTICATIONDOMAIN "
            + "change CTINYTEXT CDEFAULTURLPATTERN TINYTEXT" );
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Create the EO_PK_TABLE table, if needed.
     * @throws SQLException on error
     */
    private void createEoPkTable() throws SQLException
    {
        if ( !database().hasTable( "EO_PK_TABLE", "PK", "1" ) )
        {
            log.info( "creating table EO_PK_TABLE" );
            database().executeSQL(
                "CREATE TABLE EO_PK_TABLE "
                + "(NAME CHAR(40) PRIMARY KEY, PK INT)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TAUTHENTICATIONDOMAIN table, if needed.
     * @throws SQLException on error
     */
    private void createAuthenticationDomainTable() throws SQLException
    {
        if ( !database().hasTable( "TAUTHENTICATIONDOMAIN" ) )
        {
            log.info( "creating table TAUTHENTICATIONDOMAIN" );
            database().executeSQL(
                "CREATE TABLE TAUTHENTICATIONDOMAIN "
                + "(CDEFAULTEMAILDOMAIN TINYTEXT , CTINYTEXT TINYTEXT , "
                + "CDISPLAYABLENAME TINYTEXT , OID INTEGER NOT NULL, "
                + "CPROPERTYNAME TINYTEXT , CTIMEZONENAME TINYTEXT )" );
            database().executeSQL(
                "ALTER TABLE TAUTHENTICATIONDOMAIN ADD PRIMARY KEY (OID)" );

            // need to populate with a default authentication domain
//            log.info( "creating default authentication domain for "
//                      + "\"Support Personnel\"" );
//            database().executeSQL(
//                "INSERT INTO TAUTHENTICATIONDOMAIN VALUES ('vt.edu', NULL, "
//                + "'Support Personnel', 1, 'authenticator.DBAuth', NULL)" );
//            database().executeSQL(
//                "INSERT INTO EO_PK_TABLE VALUES ('TAUTHENTICATIONDOMAIN', 1)" );
//            finishInitializingDefaultDomain = true;
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCORESELECTIONS table, if needed.
     * @throws SQLException on error
     */
    private void createCoreSelectionsTable() throws SQLException
    {
        if ( !database().hasTable( "TCORESELECTIONS" ) )
        {
            log.info( "creating table TCORESELECTIONS" );
            database().executeSQL(
                "CREATE TABLE TCORESELECTIONS "
                + "(CCOURSEID INTEGER , CCOURSEOFFERINGID INTEGER , "
                + "OID INTEGER NOT NULL, CUSERID INTEGER )" );
            database().executeSQL(
                "ALTER TABLE TCORESELECTIONS ADD PRIMARY KEY (OID)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSE table, if needed.
     * @throws SQLException on error
     */
    private void createCourseTable() throws SQLException
    {
        if ( !database().hasTable( "TCOURSE" ) )
        {
            log.info( "creating table TCOURSE" );
            database().executeSQL(
                "CREATE TABLE TCOURSE "
                + "(CDEPARTMENTID INTEGER , OID INTEGER NOT NULL, "
                + "CNAME TINYTEXT NOT NULL, CNUMBER SMALLINT NOT NULL)" );
            database().executeSQL(
                "ALTER TABLE TCOURSE ADD PRIMARY KEY (OID)" );

            // add default sandbox course?
//            log.info( "creating default course \"CS 1: Sandbox\"" );
//            database().executeSQL(
//                "INSERT INTO TCOURSE VALUES "
//                + "(1, 1, 'Sandbox', 1)" );
//            database().executeSQL(
//                "INSERT INTO EO_PK_TABLE VALUES ('TCOURSE', 1)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSEOFFERING table, if needed.
     * @throws SQLException on error
     */
    private void createCourseOfferingTable() throws SQLException
    {
        if ( !database().hasTable( "TCOURSEOFFERING" ) )
        {
            log.info( "creating table TCOURSEOFFERING" );
            database().executeSQL(
                "CREATE TABLE TCOURSEOFFERING "
                + "(CCOURSEID INTEGER , CCRN INTEGER , "
                + "OID INTEGER NOT NULL, CMOODLEGROUPID INTEGER , "
                + "CMOODLEID INTEGER , CSEMESTER INTEGER , URL TINYTEXT )" );
            database().executeSQL(
                "ALTER TABLE TCOURSEOFFERING ADD PRIMARY KEY (OID)" );

            // add default sandbox course?
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSESTUDENT table, if needed.
     * @throws SQLException on error
     */
    private void createCourseStudentTable() throws SQLException
    {
        if ( !database().hasTable( "TCOURSESTUDENT", "CID", "1" ) )
        {
            log.info( "creating table TCOURSESTUDENT" );
            database().executeSQL(
                "CREATE TABLE TCOURSESTUDENT "
                + "(CID INT NOT NULL, CID1 INT NOT NULL)" );
            database().executeSQL(
                "ALTER TABLE TCOURSESTUDENT ADD PRIMARY KEY (CID, CID1)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSETA table, if needed.
     * @throws SQLException on error
     */
    private void createCourseTATable() throws SQLException
    {
        if ( !database().hasTable( "TCOURSETA", "CID", "1" ) )
        {
            log.info( "creating table TCOURSETA" );
            database().executeSQL(
                "CREATE TABLE TCOURSETA "
                + "(CID INT NOT NULL, CID1 INT NOT NULL)" );
            database().executeSQL(
                "ALTER TABLE TCOURSETA ADD PRIMARY KEY (CID, CID1)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TDEPARTMENT table, if needed.
     * @throws SQLException on error
     */
    private void createDepartmentTable() throws SQLException
    {
        if ( !database().hasTable( "TDEPARTMENT" ) )
        {
            log.info( "creating table TDEPARTMENT" );
            database().executeSQL(
                "CREATE TABLE TDEPARTMENT "
                + "(CABBREVIATION TINYTEXT NOT NULL, OID INTEGER NOT NULL, "
                + "CNAME TINYTEXT )" );
            database().executeSQL(
                "ALTER TABLE TDEPARTMENT ADD PRIMARY KEY (OID)" );

            // add default department
//            log.info( "creating default department \"CS: Computer Science\"" );
//            database().executeSQL(
//                "INSERT INTO TDEPARTMENT VALUES "
//                + "('CS', 1, 'Computer Science')" );
//            database().executeSQL(
//                "INSERT INTO EO_PK_TABLE VALUES ('TDEPARTMENT', 1)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TINSTRUCTORCOURSE table, if needed.
     * @throws SQLException on error
     */
    private void createInstructorCourseTable() throws SQLException
    {
        if ( !database().hasTable( "TINSTRUCTORCOURSE", "CID", "1" ) )
        {
            log.info( "creating table TINSTRUCTORCOURSE" );
            database().executeSQL(
                "CREATE TABLE TINSTRUCTORCOURSE "
                + "(CID INT NOT NULL, CID1 INT NOT NULL)" );
            database().executeSQL(
                "ALTER TABLE TINSTRUCTORCOURSE ADD PRIMARY KEY (CID, CID1)" );

            // make admin the instructor for the sandbox course?
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TLANGUAGE table, if needed.
     * @throws SQLException on error
     */
    private void createLanguageTable() throws SQLException
    {
        if ( !database().hasTable( "TLANGUAGE" ) )
        {
            log.info( "creating table TLANGUAGE" );
            database().executeSQL(
                "CREATE TABLE TLANGUAGE "
                + "(CCOMPILER TINYTEXT , OID INTEGER NOT NULL, "
                + "CNAME TINYTEXT , CVERSION TINYTEXT )" );
            database().executeSQL(
                "ALTER TABLE TLANGUAGE ADD PRIMARY KEY (OID)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TLOGINSESSION table, if needed.
     * @throws SQLException on error
     */
    private void createLoginSessionTable() throws SQLException
    {
        if ( !database().hasTable( "TLOGINSESSION" ) )
        {
            log.info( "creating table TLOGINSESSION" );
            database().executeSQL(
                "CREATE TABLE TLOGINSESSION "
                + "(CEXPIRETIME DATETIME , OID INTEGER NOT NULL, "
                + "CSESSIONID TINYBLOB , CUSERID INTEGER )" );
            database().executeSQL(
                "ALTER TABLE TLOGINSESSION ADD PRIMARY KEY (OID)" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TSEMESTER table, if needed.
     * @throws SQLException on error
     */
    private void createSemesterTable() throws SQLException
    {
        if ( !database().hasTable( "TSEMESTER" ) )
        {
            log.info( "creating table TSEMESTER" );
            database().executeSQL(
                "CREATE TABLE TSEMESTER "
                + "(OID INTEGER NOT NULL, CSEASON TINYINT , "
                + "CSEMESTERENDDATE DATE , CSEMESTERSTARTDATE DATE , "
                + "CYEAR SMALLINT NOT NULL)" );
            database().executeSQL(
                "ALTER TABLE TSEMESTER ADD PRIMARY KEY (OID)" );

            // create default semester
//            java.util.Calendar now = new java.util.GregorianCalendar();
//            int thisYear = now.get( java.util.Calendar.YEAR );
//            int thisMonth = now.get( java.util.Calendar.MONTH ) + 1;
//            int semester = Semester.defaultSemesterFor( now );
//            int startMonth = Semester.defaultStartingMonth( semester );
//            int startYear = thisYear;
//            if ( startMonth > thisMonth ) startYear--;
//            int endMonth = Semester.defaultEndingMonth( semester );
//            int endYear = thisYear;
//            if ( endMonth < thisMonth ) endYear++;
//            int startDay = 1;
//            int endDay = 30;
//            switch ( endMonth )
//            {
//                case 2: endDay = 28; break;
//                case 1:
//                case 3:
//                case 5:
//                case 7:
//                case 8:
//                case 10:
//                case 12: endDay = 31; break;
//            }
//            database().executeSQL(
//                "INSERT INTO TSEMESTER VALUES "
//                + "(1, " + semester + ", '" + endYear + "-" + endMonth
//                + "-" + endDay + "', '" + startYear + "-" + startMonth
//                + "-" + startDay + "', " + thisYear + ")" );
//            database().executeSQL(
//                "INSERT INTO EO_PK_TABLE VALUES ('TSEMESTER', 1)" );
        }
    }

    // ----------------------------------------------------------
    /**
     * Create the TUSER table, if needed.
     * @throws SQLException on error
     */
    private void createUserTable() throws SQLException
    {
        if ( !database().hasTable( "TUSER" ) )
        {
            log.info( "creating table TUSER" );
            database().executeSQL(
                "CREATE TABLE TUSER "
                + "(CACCESSLEVEL TINYINT NOT NULL, "
                + "CAUTHENTICATIONDOMAINID INTEGER NOT NULL, "
                + "CEMAIL TINYTEXT , CFIRSTNAME TINYTEXT , "
                + "OID INTEGER NOT NULL, CLASTNAME TINYTEXT , "
                + "PASSWORD TINYTEXT , CPREFERENCES BLOB , "
                + "CUNIVERSITYIDNO TINYTEXT , "
                + "CUPDATEMUTABLEFIELDS BIT NOT NULL, URL TINYTEXT , "
                + "CUSERNAME TINYTEXT NOT NULL)" );
            database().executeSQL(
                "ALTER TABLE TUSER ADD PRIMARY KEY (OID)" );

            // create default user
//            log.info( "creating default admin account" );
//            database().executeSQL(
//                "INSERT INTO TUSER VALUES "
//                + "(100, 1, NULL, NULL, 1, NULL, 'admin', "
//                + "NULL, NULL, 0, NULL, 'admin')" );
//            database().executeSQL(
//                "INSERT INTO EO_PK_TABLE VALUES ('TUSER', 1)" );
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( UpdateSet.class );
}
