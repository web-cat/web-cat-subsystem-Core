/*==========================================================================*\
 |  $Id: CoreDatabaseUpdates.java,v 1.6 2014/07/08 17:35:28 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU Affero General Public License as published
 |  by the Free Software Foundation; either version 3 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU Affero General Public License
 |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package org.webcat.core;

import org.webcat.dbupdate.UpdateSet;
import java.sql.SQLException;

// -------------------------------------------------------------------------
/**
 * This class captures the SQL database schema for the database tables
 * underlying the Core subsystem and the Core.eomodeld.  Logging output
 * for this class uses its parent class' logger.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.6 $, $Date: 2014/07/08 17:35:28 $
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
        super("core");
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
            "UPDATE TUSER SET CUPDATEMUTABLEFIELDS = 0");
    }


    // ----------------------------------------------------------
    /**
     * Changes course CRNs to strings.
     * @throws SQLException on error
     */
    public void updateIncrement2() throws SQLException
    {
        database().executeSQL(
            "alter table TCOURSEOFFERING change CCRN CCRN TINYTEXT");
    }


    // ----------------------------------------------------------
    /**
     * Add institution id to departments.
     * @throws SQLException on error
     */
    public void updateIncrement3() throws SQLException
    {
        database().executeSQL(
            "alter table TDEPARTMENT add CINSTITUTIONID INTEGER");
    }


    // ----------------------------------------------------------
    /**
     * Changes login session ids to text.
     * @throws SQLException on error
     */
    public void updateIncrement4() throws SQLException
    {
        database().executeSQL(
            "alter table TLOGINSESSION change CSESSIONID CSESSIONID TINYTEXT");
    }


    // ----------------------------------------------------------
    /**
     * Adds timeFormat and dateFormat keys to AuthenticationDomain.
     * @throws SQLException on error
     */
    public void updateIncrement5() throws SQLException
    {
        database().executeSQL(
            "alter table TAUTHENTICATIONDOMAIN add CTIMEFORMAT TINYTEXT");
        database().executeSQL(
            "alter table TAUTHENTICATIONDOMAIN add CDATEFORMAT TINYTEXT");
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
            + "change CTINYTEXT CDEFAULTURLPATTERN TINYTEXT");
    }


    // ----------------------------------------------------------
    /**
     * Adds LoggedError and PasswordChangeRequest tables.
     * @throws SQLException on error
     */
    public void updateIncrement7() throws SQLException
    {
        createLoggedErrorTable();
        createPasswordChangeRequestTable();
    }


    // ----------------------------------------------------------
    /**
     * Adds label key to CourseOffering.
     * @throws SQLException on error
     */
    public void updateIncrement8() throws SQLException
    {
        database().executeSQL(
            "alter table TCOURSEOFFERING add CLABEL TINYTEXT");
    }


    // ----------------------------------------------------------
    /**
     * Adds theme ID column to TUSER.
     * @throws SQLException on error
     */
    public void updateIncrement9() throws SQLException
    {
        createThemesTable();
        database().executeSQL(
            "ALTER TABLE TUSER ADD CTHEMEID INTEGER");
    }


    // ----------------------------------------------------------
    /**
     * Creates the broadcast messaging selections and user messaging
     * selections tables.
     * @throws SQLException on error
     */
    public void updateIncrement10() throws SQLException
    {
        // This update increment used to create the
        // BroadcastMessageSubscription and UserMessageSubscription tables, but
        // they have now been moved into the Notification subsystem.
    }


    // ----------------------------------------------------------
    /**
     * Creates the protocol settings table and adds an association to it for
     * users.
     * @throws SQLException on error
     */
    public void updateIncrement11() throws SQLException
    {
        // This update increment used to create the ProtocolSettings table, but
        // it has now been moved into the Notification subsystem.

        database().executeSQL(
            "ALTER TABLE TUSER ADD protocolSettingsId INTEGER");
    }


    // ----------------------------------------------------------
    /**
     * Creates the sent message table and the shadow table for the to-many
     * relationship between users and sent messages.
     * @throws SQLException on error
     */
    public void updateIncrement12() throws SQLException
    {
        createSentMessageTable();
        createUserSentMessageTable();
    }


    // ----------------------------------------------------------
    /**
     * Adds the isBroadcast field to the SentMessage table.
     * @throws SQLException on error
     */
    public void updateIncrement13() throws SQLException
    {
        database().executeSQL(
            "ALTER TABLE SentMessage ADD isBroadcast BIT NOT NULL");
    }


    // ----------------------------------------------------------
    /**
     * Creates the ObjectQuery table.
     * @throws SQLException on error
     */
    public void updateIncrement14() throws SQLException
    {
        createObjectQueryTable();
    }


    // ----------------------------------------------------------
    /**
     * Drops the protocolSettingsId field from the TUSER table. This
     * relationship has now been inverted since ProtocolSettings was moved up
     * into the Notifications subsystem.
     * @throws SQLException on error
     */
    public void updateIncrement15() throws SQLException
    {
        database().executeSQL("ALTER TABLE TUSER DROP protocolSettingsId");
    }


    // ----------------------------------------------------------
    /**
     * Add indexes for better performance.
     * @throws SQLException on error
     */
    public void updateIncrement16() throws SQLException
    {
        // Indices for CoreSelections
        createIndexFor("TCORESELECTIONS", "CUSERID");
        createIndexFor("TCORESELECTIONS", "CCOURSEID");
        createIndexFor("TCORESELECTIONS", "CCOURSEOFFERINGID");

        // Indices for Course
        createIndexFor("TCOURSE", "CDEPARTMENTID");

        // Indices for CourseOffering
        createIndexFor("TCOURSEOFFERING", "CCOURSEID");
        createIndexFor("TCOURSEOFFERING", "CCRN(8)");
        createIndexFor("TCOURSEOFFERING", "CSEMESTER");

        // Indices for LoginSession
        createIndexFor("TLOGINSESSION", "CSESSIONID(10)");
        createIndexFor("TLOGINSESSION", "CUSERID");

        // Indices for ObjectQuery
        createIndexFor("ObjectQuery", "userId");

        // Indices for Semester
        createIndexFor("TSEMESTER", "CSEMESTERSTARTDATE");

        // Indices for Theme
        createIndexFor("TTHEMES", "CNAME(12)");

        // Indices for User
        createIndexFor("TUSER", "CUSERNAME(8)");
        createIndexFor("TUSER", "CAUTHENTICATIONDOMAINID");

        // Indices for UserSentMessage
        createIndexFor("UserSentMessage", "userId");
        createIndexFor("UserSentMessage", "sentMessageId");
    }


    // ----------------------------------------------------------
    /**
     * Add UsagePeriod.
     * @throws SQLException on error
     */
    public void updateIncrement17() throws SQLException
    {
        createUsagePeriodTable();
        createIndexFor("UsagePeriod", "userId");
        createIndexFor("UsagePeriod", "startTime");
        database().executeSQL(
            "ALTER TABLE TLOGINSESSION ADD usagePeriodId INTEGER NOT NULL");
        // Clear all existing login sessions, which happens at startup anyway
        database().executeSQL("DELETE FROM TLOGINSESSION");
        createIndexFor("TLOGINSESSION", "usagePeriodId");
    }


    // ----------------------------------------------------------
    /**
     * Adds the salt and iterations columns to the user table.
     * @throws SQLException on error
     */
    public void updateIncrement18() throws SQLException
    {
        database().executeSQL("ALTER TABLE TUSER ADD salt TINYTEXT");
        database().executeSQL("ALTER TABLE TUSER ADD iterations INTEGER");
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Create the EO_PK_TABLE table, if needed.
     * @throws SQLException on error
     */
    private void createEoPkTable() throws SQLException
    {
        if (!database().hasTable("EO_PK_TABLE", "PK", "1"))
        {
            log.info("creating table EO_PK_TABLE");
            database().executeSQL(
                "CREATE TABLE EO_PK_TABLE "
                + "(NAME CHAR(40) PRIMARY KEY, PK INT)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TAUTHENTICATIONDOMAIN table, if needed.
     * @throws SQLException on error
     */
    private void createAuthenticationDomainTable() throws SQLException
    {
        if (!database().hasTable("TAUTHENTICATIONDOMAIN"))
        {
            log.info("creating table TAUTHENTICATIONDOMAIN");
            database().executeSQL(
                "CREATE TABLE TAUTHENTICATIONDOMAIN "
                + "(CDEFAULTEMAILDOMAIN TINYTEXT , CTINYTEXT TINYTEXT , "
                + "CDISPLAYABLENAME TINYTEXT , OID INTEGER NOT NULL, "
                + "CPROPERTYNAME TINYTEXT , CTIMEZONENAME TINYTEXT )");
            database().executeSQL(
                "ALTER TABLE TAUTHENTICATIONDOMAIN ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCORESELECTIONS table, if needed.
     * @throws SQLException on error
     */
    private void createCoreSelectionsTable() throws SQLException
    {
        if (!database().hasTable("TCORESELECTIONS"))
        {
            log.info("creating table TCORESELECTIONS");
            database().executeSQL(
                "CREATE TABLE TCORESELECTIONS "
                + "(CCOURSEID INTEGER , CCOURSEOFFERINGID INTEGER , "
                + "OID INTEGER NOT NULL, CUSERID INTEGER )");
            database().executeSQL(
                "ALTER TABLE TCORESELECTIONS ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSE table, if needed.
     * @throws SQLException on error
     */
    private void createCourseTable() throws SQLException
    {
        if (!database().hasTable("TCOURSE"))
        {
            log.info("creating table TCOURSE");
            database().executeSQL(
                "CREATE TABLE TCOURSE "
                + "(CDEPARTMENTID INTEGER , OID INTEGER NOT NULL, "
                + "CNAME TINYTEXT NOT NULL, CNUMBER SMALLINT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE TCOURSE ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSEOFFERING table, if needed.
     * @throws SQLException on error
     */
    private void createCourseOfferingTable() throws SQLException
    {
        if (!database().hasTable("TCOURSEOFFERING"))
        {
            log.info("creating table TCOURSEOFFERING");
            database().executeSQL(
                "CREATE TABLE TCOURSEOFFERING "
                + "(CCOURSEID INTEGER , CCRN INTEGER , "
                + "OID INTEGER NOT NULL, CMOODLEGROUPID INTEGER , "
                + "CMOODLEID INTEGER , CSEMESTER INTEGER , URL TINYTEXT )");
            database().executeSQL(
                "ALTER TABLE TCOURSEOFFERING ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSESTUDENT table, if needed.
     * @throws SQLException on error
     */
    private void createCourseStudentTable() throws SQLException
    {
        if (!database().hasTable("TCOURSESTUDENT", "CID", "1"))
        {
            log.info("creating table TCOURSESTUDENT");
            database().executeSQL(
                "CREATE TABLE TCOURSESTUDENT "
                + "(CID INT NOT NULL, CID1 INT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE TCOURSESTUDENT ADD PRIMARY KEY (CID, CID1)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TCOURSETA table, if needed.
     * @throws SQLException on error
     */
    private void createCourseTATable() throws SQLException
    {
        if (!database().hasTable("TCOURSETA", "CID", "1"))
        {
            log.info("creating table TCOURSETA");
            database().executeSQL(
                "CREATE TABLE TCOURSETA "
                + "(CID INT NOT NULL, CID1 INT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE TCOURSETA ADD PRIMARY KEY (CID, CID1)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TDEPARTMENT table, if needed.
     * @throws SQLException on error
     */
    private void createDepartmentTable() throws SQLException
    {
        if (!database().hasTable("TDEPARTMENT"))
        {
            log.info("creating table TDEPARTMENT");
            database().executeSQL(
                "CREATE TABLE TDEPARTMENT "
                + "(CABBREVIATION TINYTEXT NOT NULL, OID INTEGER NOT NULL, "
                + "CNAME TINYTEXT )");
            database().executeSQL(
                "ALTER TABLE TDEPARTMENT ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TINSTRUCTORCOURSE table, if needed.
     * @throws SQLException on error
     */
    private void createInstructorCourseTable() throws SQLException
    {
        if (!database().hasTable("TINSTRUCTORCOURSE", "CID", "1"))
        {
            log.info("creating table TINSTRUCTORCOURSE");
            database().executeSQL(
                "CREATE TABLE TINSTRUCTORCOURSE "
                + "(CID INT NOT NULL, CID1 INT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE TINSTRUCTORCOURSE ADD PRIMARY KEY (CID, CID1)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TLANGUAGE table, if needed.
     * @throws SQLException on error
     */
    private void createLanguageTable() throws SQLException
    {
        if (!database().hasTable("TLANGUAGE"))
        {
            log.info("creating table TLANGUAGE");
            database().executeSQL(
                "CREATE TABLE TLANGUAGE "
                + "(CCOMPILER TINYTEXT , OID INTEGER NOT NULL, "
                + "CNAME TINYTEXT , CVERSION TINYTEXT )");
            database().executeSQL(
                "ALTER TABLE TLANGUAGE ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TLOGINSESSION table, if needed.
     * @throws SQLException on error
     */
    private void createLoginSessionTable() throws SQLException
    {
        if (!database().hasTable("TLOGINSESSION"))
        {
            log.info("creating table TLOGINSESSION");
            database().executeSQL(
                "CREATE TABLE TLOGINSESSION "
                + "(CEXPIRETIME DATETIME , OID INTEGER NOT NULL, "
                + "CSESSIONID TINYBLOB , CUSERID INTEGER )");
            database().executeSQL(
                "ALTER TABLE TLOGINSESSION ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TSEMESTER table, if needed.
     * @throws SQLException on error
     */
    private void createSemesterTable() throws SQLException
    {
        if (!database().hasTable("TSEMESTER"))
        {
            log.info("creating table TSEMESTER");
            database().executeSQL(
                "CREATE TABLE TSEMESTER "
                + "(OID INTEGER NOT NULL, CSEASON TINYINT , "
                + "CSEMESTERENDDATE DATE , CSEMESTERSTARTDATE DATE , "
                + "CYEAR SMALLINT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE TSEMESTER ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TUSER table, if needed.
     * @throws SQLException on error
     */
    private void createUserTable() throws SQLException
    {
        if (!database().hasTable("TUSER"))
        {
            log.info("creating table TUSER");
            database().executeSQL(
                "CREATE TABLE TUSER "
                + "(CACCESSLEVEL TINYINT NOT NULL, "
                + "CAUTHENTICATIONDOMAINID INTEGER NOT NULL, "
                + "CEMAIL TINYTEXT , CFIRSTNAME TINYTEXT , "
                + "OID INTEGER NOT NULL, CLASTNAME TINYTEXT , "
                + "PASSWORD TINYTEXT , CPREFERENCES BLOB , "
                + "CUNIVERSITYIDNO TINYTEXT , "
                + "CUPDATEMUTABLEFIELDS BIT NOT NULL, URL TINYTEXT , "
                + "CUSERNAME TINYTEXT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE TUSER ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the LoggedError table, if needed.
     * @throws SQLException on error
     */
    private void createLoggedErrorTable() throws SQLException
    {
        if (!database().hasTable("LoggedError"))
        {
            log.info("creating table LoggedError");
            database().executeSQL(
                "CREATE TABLE LoggedError "
                + "(component TINYTEXT , exceptionName TINYTEXT , "
                + "OID INTEGER NOT NULL, inClass TINYTEXT , "
                + "inMethod TINYTEXT , line INTEGER NOT NULL, "
                + "message TINYTEXT , mostRecent DATETIME , "
                + "occurrences INTEGER NOT NULL, page TINYTEXT , "
                + "stackTrace MEDIUMTEXT)");
            database().executeSQL(
                "ALTER TABLE LoggedError ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the PasswordChangeRequest table, if needed.
     * @throws SQLException on error
     */
    private void createPasswordChangeRequestTable() throws SQLException
    {
        if (!database().hasTable("PasswordChangeRequest"))
        {
            log.info("creating table PasswordChangeRequest");
            database().executeSQL(
                "CREATE TABLE PasswordChangeRequest "
                + "(code TINYTEXT , expireTime DATETIME , "
                + "OID INTEGER NOT NULL, userId INTEGER NOT NULL)");
            database().executeSQL(
                "ALTER TABLE PasswordChangeRequest ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the TTHEMES table, if needed.
     * @throws SQLException on error
     */
    private void createThemesTable() throws SQLException
    {
        if (!database().hasTable("TTHEMES"))
        {
            log.info("creating table TTHEMES");
            database().executeSQL(
                "CREATE TABLE TTHEMES "
                + "(CDIRNAME TINYTEXT NOT NULL, OID INTEGER NOT NULL, "
                + "CISFORTHEMEDEVELOPERS BIT , "
                + "CLASTUPDATE DATETIME , CNAME TINYTEXT , "
                + "CPROPERTIES BLOB , CUPDATEMUTABLEFIELDS BIT NOT NULL )");
            database().executeSQL(
                "ALTER TABLE TTHEMES ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the SentMessage table, if needed.
     * @throws SQLException on error
     */
    private void createSentMessageTable() throws SQLException
    {
        if (!database().hasTable("SentMessage"))
        {
            log.info("creating table SentMessage");
            database().executeSQL(
                "CREATE TABLE SentMessage "
                + "(OID INTEGER NOT NULL, "
                + "sentTime DATETIME, "
                + "messageType MEDIUMTEXT, "
                + "title MEDIUMTEXT, "
                + "shortBody MEDIUMTEXT, "
                + "links BLOB, "
                + "CUPDATEMUTABLEFIELDS BIT NOT NULL )");
            database().executeSQL(
                "ALTER TABLE SentMessage ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the SentMessage table, if needed.
     * @throws SQLException on error
     */
    private void createUserSentMessageTable() throws SQLException
    {
        if (!database().hasTable("UserSentMessage"))
        {
            log.info("creating table UserSentMessage");
            database().executeSQL(
                "CREATE TABLE UserSentMessage "
                + "(userId INT NOT NULL, sentMessageId INT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE UserSentMessage ADD PRIMARY KEY "
                + "(userId, sentMessageId)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Creates the ObjectQuery table, if needed.
     * @throws SQLException on error
     */
    private void createObjectQueryTable() throws SQLException
    {
        if (!database().hasTable("ObjectQuery"))
        {
            log.info("creating table ObjectQuery");

            database().executeSQL("CREATE TABLE ObjectQuery ("
                + "OID INTEGER NOT NULL , "
                + "description MEDIUMTEXT , "
                + "userId INTEGER ,"
                + "objectType TINYTEXT , "
                + "queryInfo BLOB , "
                + "CUPDATEMUTABLEFIELDS BIT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE ObjectQuery ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Creates the UsagePeriod table, if needed.
     * @throws SQLException on error
     */
    private void createUsagePeriodTable() throws SQLException
    {
        if (!database().hasTable("UsagePeriod"))
        {
            log.info("creating table UsagePeriod");

            database().executeSQL("CREATE TABLE UsagePeriod ("
                + "OID INTEGER NOT NULL , "
                + "userId INTEGER NOT NULL,"
                + "startTime DATETIME NOT NULL, "
                + "endTime DATETIME NOT NULL, "
                + "isLoggedOut BIT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE UsagePeriod ADD PRIMARY KEY (OID)");
        }
    }
}
