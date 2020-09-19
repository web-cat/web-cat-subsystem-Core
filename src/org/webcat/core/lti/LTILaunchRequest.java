/*==========================================================================*\
 |  Copyright (C) 2018-2021 Virginia Tech
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


package org.webcat.core.lti;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.webcat.core.Application;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.Course;
import org.webcat.core.CourseOffering;
import org.webcat.core.Department;
import org.webcat.core.Semester;
import org.webcat.core.User;
import org.webcat.woextensions.WCEC;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * Represents an LTI launch request from an LTI Consumer.
 *
 * @author  Stephen Edwards
 */
public class LTILaunchRequest
{
    //~ Instance/static fields ................................................
    public static final String CONTEXT_ID = "context_id";
    public static final String CONTEXT_LABEL = "context_label";
    public static final String CONTEXT_TITLE = "context_title";
    public static final String CUSTOM_CANVAS_API_DOMAIN =
        "custom_canvas_api_domain";
    public static final String  CUSTOM_CANVAS_ASSIGNMENT_ID =
        "custom_canvas_assignment_id";
    public static final String CUSTOM_CANVAS_ASSIGNMENT_POINTS_POSSIBLE =
        "custom_canvas_assignment_points_possible";
    public static final String CUSTOM_CANVAS_ASSIGNMENT_TITLE =
        "custom_canvas_assignment_title";
    public static final String CUSTOM_CANVAS_COURSE_ID =
        "custom_canvas_course_id";
    public static final String CUSTOM_CANVAS_ENROLLMENT_STATE =
        "custom_canvas_enrollment_state";
    public static final String CUSTOM_CANVAS_USER_ID = "custom_canvas_user_id";
    public static final String CUSTOM_CANVAS_USER_LOGIN_ID =
        "custom_canvas_user_login_id";
    public static final String CUSTOM_CANVAS_WORKFLOW_STATE =
        "custom_canvas_workflow_state";
    public static final String EXT_IMS_LIS_BASIC_OUTCOME_URL =
        "ext_ims_lis_basic_outcome_url";
    public static final String EXT_LTI_ASSIGNMENT_ID = "ext_lti_assignment_id";
    public static final String EXT_OUTCOME_DATA_VALUES_ACCEPTED =
        "ext_outcome_data_values_accepted";
    public static final String EXT_OUTCOME_RESULT_TOTAL_SCORE_ACCEPTED =
        "ext_outcome_result_total_score_accepted";
    public static final String EXT_OUTCOMES_TOOL_PLACEMENT_URL =
        "ext_outcomes_tool_placement_url";
    public static final String EXT_ROLES = "ext_roles";
    public static final String LAUNCH_PRESENTATION_DOCUMENT_TARGET =
        "launch_presentation_document_target";
    public static final String LAUNCH_PRESENTATION_LOCALE =
        "launch_presentation_locale";
    public static final String LAUNCH_PRESENTATION_RETURN_URL =
        "launch_presentation_return_url";
    public static final String LIS_COURSE_OFFERING_SOURCEDID =
        "lis_course_offering_sourcedid";
    public static final String LIS_OUTCOME_SERVICE_URL =
        "lis_outcome_service_url";
    public static final String LIS_PERSON_CONTACT_EMAIL_PRIMARY =
        "lis_person_contact_email_primary";
    public static final String LIS_PERSON_NAME_FAMILY =
        "lis_person_name_family";
    public static final String LIS_PERSON_NAME_FULL = "lis_person_name_full";
    public static final String LIS_PERSON_NAME_GIVEN = "lis_person_name_given";
    public static final String LIS_PERSON_SOURCEDID = "lis_person_sourcedid";
    public static final String LIS_RESULT_SOURCEDID =
        "lis_result_sourcedid";
    public static final String LTI_MESSAGE_TYPE = "lti_message_type";
    public static final String LTI_VERSION = "lti_version";
    public static final String RESOURCE_LINK_ID = "resource_link_id";
    public static final String RESOURCE_LINK_TITLE = "resource_link_title";
    public static final String ROLES = "roles";
    public static final String TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE =
        "tool_consumer_info_product_family_code";
    public static final String TOOL_CONSUMER_INFO_VERSION =
        "tool_consumer_info_version";
    public static final String TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL =
        "tool_consumer_instance_contact_email";
    public static final String TOOL_CONSUMER_INSTANCE_GUID =
        "tool_consumer_instance_guid";
    public static final String TOOL_CONSUMER_INSTANCE_NAME =
        "tool_consumer_instance_name";
    public static final String USER_ID = "user_id";
    public static final String USER_IMAGE = "user_image";


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public LTILaunchRequest(WOContext context, WCEC ec)
    {
        this.context = context;
        this.request = context.request();
        this.ec = ec;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean isValid()
    {
        String consumerKey = get(OAUTH_CONSUMER_KEY);
        if (consumerKey == null)
        {
            return false;
        }

        LMSInstance lms = LMSInstance.uniqueObjectMatchingValues(
            ec, LMSInstance.CONSUMER_KEY_KEY, consumerKey);

        if (lms == null)
        {
            return false;
        }
        String consumerSecret = lms.consumerSecret();
        String apiUrl = Application.completeURLWithRequestHandlerKey(context,
            "wa", "ltiLaunch", null, true, 0);
        List<OAuth.Parameter> params = new ArrayList<OAuth.Parameter>();
        for (String key : request.formValueKeys())
        {
            for (Object value : request.formValuesForKey(key))
            {
                params.add(new OAuth.Parameter(key, value.toString()));
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("lti launch request received: " + params);
        }
        OAuthMessage message = new OAuthMessage("POST", apiUrl, params);
        OAuthConsumer consumer =
            new OAuthConsumer(null, consumerKey, consumerSecret, null);
        OAuthAccessor accessor = new OAuthAccessor(consumer);

        try
        {
            VALIDATOR.validateMessage(message, accessor);
            return true;
        }
        catch (Exception e)
        {
            log.error("request is invalid: " + e.getClass().getSimpleName()
                + ": " + e.getMessage());
            return false;
        }
    }


    // ----------------------------------------------------------
    public String get(String key)
    {
        return request.stringFormValueForKey(key);
    }


    // ----------------------------------------------------------
    public LMSInstance lmsInstance()
    {
        if (lmsInstance == null)
        {
            lmsInstance = LMSInstance.uniqueObjectMatchingQualifier(ec,
                LMSInstance.consumerKey.is(
                request.stringFormValueForKey(OAUTH_CONSUMER_KEY)));
        }
        return lmsInstance;
    }


    // ----------------------------------------------------------
    public String userId()
    {
        return request.stringFormValueForKey(USER_ID);
    }


    // ----------------------------------------------------------
    public User user()
    {
        if (user == null)
        {
            AuthenticationDomain d = lmsInstance().authenticationDomain();
            LMSIdentity identity = LMSIdentity.firstObjectMatchingQualifier(
                ec, LMSIdentity.lmsUserId.is(userId()), null);
            if (identity != null)
            {
                user = identity.user();
            }
            else
            {
                String email = get(LIS_PERSON_CONTACT_EMAIL_PRIMARY);
                if (email != null)
                {
                    NSArray<User> users = User.objectsMatchingQualifier(ec,
                        User.email.is(email)
                        .and(User.authenticationDomain.is(d)));
                    if (users.size() > 1)
                    {
                        String msg = "Multiple users with e-mail address '"
                            + email + "' in domain " + d;
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                    if (users.size() == 0)
                    {
                        users = User.objectsMatchingQualifier(ec,
                            User.email.is(email));
                        if (users.size() > 1)
                        {
                            String msg = "Multiple users with e-mail "
                                + "address '" + email + "'";
                            log.error(msg);
                            throw new IllegalStateException(msg);
                        }
                        else if (users.size() == 1)
                        {
                            user = users.get(0);
                            log.error("user '" + email
                                + "' logging in through LMS Instance "
                                + lmsInstance()
                                + " associated with " + d
                                + ", but user belongs to "
                                + user.authenticationDomain());
                        }
                    }
                    if (users.size() == 0)
                    {
                        String userName = get(CUSTOM_CANVAS_USER_LOGIN_ID);
                        if (userName != null)
                        {
                            users = User.objectsMatchingQualifier(ec,
                                User.userName.is(userName)
                                .and(User.authenticationDomain.is(d)));
                            if (users.size() > 1)
                            {
                                String msg = "Multiple users with user name '"
                                    + userName + "' in domain " + d;
                                log.error(msg);
                                throw new IllegalStateException(msg);
                            }
                        }
                    }
                    if (users.size() == 0)
                    {
                        String userName = email;
                        int pos = userName.indexOf('@');
                        if (pos > 0)
                        {
                            userName = userName.substring(0, pos);
                        }
                        users = User.objectsMatchingQualifier(ec,
                            User.userName.is(userName)
                            .and(User.authenticationDomain.is(d)));
                        if (users.size() > 1)
                        {
                            String msg = "Multiple users with user name '"
                                + userName + "' in domain " + d;
                            log.error(msg);
                            throw new IllegalStateException(msg);
                        }
                    }

                    if (users.size() == 1)
                    {
                        user = users.get(0);
                    }
                }
                if (user == null)
                {
                    String userName = get(CUSTOM_CANVAS_USER_LOGIN_ID);
                    if (userName != null)
                    {
                        NSArray<User> users = User.objectsMatchingQualifier(ec,
                            User.userName.is(userName)
                            .and(User.authenticationDomain.is(d)));
                        if (users.size() == 1)
                        {
                            user = users.get(0);
                        }
                        else if (users.size() > 1)
                        {
                            String msg = "Multiple users with userName '"
                                + userName + "' in domain " + d;
                            log.error(msg);
                            throw new IllegalStateException(msg);
                        }
                    }
                }

                if (user != null)
                {
                    // Add LMSIdentity, since it is missing
                    LMSIdentity.create(ec, userId(), lmsInstance, user);
                    ec.saveChangesTolerantly();
                }
            }

            if (user == null)
            {
                // Automatically add user
                String email = get(LIS_PERSON_CONTACT_EMAIL_PRIMARY);
                String userName = get(CUSTOM_CANVAS_USER_LOGIN_ID);
                if (userName == null)
                {
                    if (email != null)
                    {
                        userName = email;
                        int pos = userName.indexOf('@');
                        if (pos > 0)
                        {
                            userName = userName.substring(0, pos);
                        }
                    }
                    if (userName == null)
                    {
                        // FIXME: need a better choice here
                        userName = userId();
                    }
                    if (User.firstObjectMatchingQualifier(ec,
                        User.userName.is(userName)
                        .and(User.authenticationDomain.is(d)), null) != null)
                    {
                        String msg = "User not found with LTI user id "
                            + userId() + ", but cannot create new user with "
                            + "userName '" + userName + "' since one "
                            + "already exists in " + d;
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }

                user = User.createUser(
                    userName,
                    null,  // DO NOT MIRROR PASSWORD IN DATABASE
                           // for security reasons
                    d,
                    User.STUDENT_PRIVILEGES,
                    ec
                );
                if (email != null)
                {
                    user.setEmail(email);
                }
                LMSIdentity.create(ec, userId(), lmsInstance, user);
                ec.saveChangesTolerantly();
            }

            if (user != null)
            {
                if (user.accessLevel() < User.INSTRUCTOR_PRIVILEGES
                    && isInstructor())
                {
                    // Promote User to instructor-level access, if needed
                    user.setAccessLevel(User.INSTRUCTOR_PRIVILEGES);
                }
                if (user.firstName() == null)
                {
                    String first = get(LIS_PERSON_NAME_GIVEN);
                    if (first != null)
                    {
                        user.setFirstName(first);
                    }
                }
                if (user.lastName() == null)
                {
                    String last = get(LIS_PERSON_NAME_FAMILY);
                    if (last != null)
                    {
                        user.setLastName(last);
                    }
                }
                if (user.changesFromCommittedSnapshot().size() > 0)
                {
                    ec.saveChangesTolerantly();
                }
            }
        }
        return user;
    }


    // ----------------------------------------------------------
    public boolean isInstructor()
    {
        return request.formValuesForKey(ROLES).contains("Instructor");
    }


    // ----------------------------------------------------------
    public String lisOutcomeServiceUrl()
    {
        return request.stringFormValueForKey(LIS_OUTCOME_SERVICE_URL);
    }


    // ----------------------------------------------------------
    public String lisResultSourcedId()
    {
        return request.stringFormValueForKey(LIS_RESULT_SOURCEDID);
    }


    // ----------------------------------------------------------
    public String courseName()
    {
        return request.stringFormValueForKey(CONTEXT_TITLE);
    }


    // ----------------------------------------------------------
    public String courseLabel()
    {
        return request.stringFormValueForKey(CONTEXT_LABEL);
    }


    // ----------------------------------------------------------
    public String courseOfferingId()
    {
        return request.stringFormValueForKey(CONTEXT_ID);
    }


    // ----------------------------------------------------------
    public NSArray<CourseOffering> courseOfferings()
    {
        String contextId = get(CONTEXT_ID);
        NSArray<CourseOffering> result = CourseOffering
            .objectsMatchingQualifier(ec,
            CourseOffering.lmsContextId.is(contextId));
        {
            String canvasId = get(CUSTOM_CANVAS_COURSE_ID);
            NSArray<CourseOffering> canvasResult = null;
            if (canvasId != null && !"".equals(canvasId))
            {
                canvasResult = CourseOffering.objectsMatchingQualifier(ec,
                    CourseOffering.lmsContextId.is(canvasId));
            }
            else
            {
                canvasResult = new NSArray<CourseOffering>();
            }
            String labelId = get(CONTEXT_LABEL);
            NSArray<CourseOffering> labelResult = null;
            if (labelId != null && !"".equals(labelId))
            {
                labelResult = CourseOffering.objectsMatchingQualifier(ec,
                    CourseOffering.lmsContextId.is(labelId));
            }
            else
            {
                labelResult = new NSArray<CourseOffering>();
            }
            if (canvasResult.count() > 0 || labelResult.count() > 0)
            {
                NSMutableArray<CourseOffering> newResults =
                    new NSMutableArray<CourseOffering>();
                if (!contextId.equals(canvasId))
                {
                    newResults.addAll(canvasResult);
                }
                if (!contextId.equals(labelId))
                {
                    newResults.addAll(labelResult);
                }

                // Update all matching course ids to the proper value
                boolean needsSave = false;
                for (CourseOffering co : newResults)
                {
                    if (!contextId.equals(co.lmsContextId()))
                    {
                        co.setLmsContextId(contextId);
                        needsSave = true;
                    }
                }
                if (needsSave)
                {
                    ec.saveChangesTolerantly();
                }
                newResults.addAll(result);
                result = newResults;
            }
        }
        LMSInstance lms = lmsInstance();
        if (lms != null && result.count() > 0)
        {
            // Update all matching courses to the proper lmsInstance value
            boolean needsSave = false;
            for (CourseOffering co : result)
            {
                LMSInstance thisLms = co.lmsInstance();
                if (!lms.equals(thisLms))
                {
                    if (thisLms != null)
                    {
                        log.error("course offering " + co + " matches LTI "
                            + "request from LTI consumer " + lms + " but "
                            + "is associated with " + thisLms + ": forcing to "
                            + lms);
                    }
                    co.setLmsInstanceRelationship(lms);
                    needsSave = true;
                }
            }
            if (needsSave)
            {
                ec.saveChangesTolerantly();
            }
        }
        log.debug("courseOfferings() = " + result);
        return result;
    }


    // ----------------------------------------------------------
    public NSArray<CourseOffering> filterCoursesForUser(
        User aUser, NSArray<CourseOffering> courses)
    {
        if (courses.count() > 0 && aUser != null)
        {
            NSMutableArray<CourseOffering> filtered =
                new NSMutableArray<CourseOffering>();
            for (CourseOffering co : courses)
            {
                if (co.students().contains(aUser) || co.isStaff(aUser))
                {
                    filtered.add(co);
                }
            }
            return filtered;
        }
        return courses;
    }


    // ----------------------------------------------------------
    public NSArray<CourseOffering> suggestedCourseOfferings(User aUser)
    {
        return suggestedCourseOfferings(aUser,
            Semester.allObjectsOrderedByStartDate(ec).get(0));
    }


    // ----------------------------------------------------------
    public NSArray<CourseOffering> suggestedCourseOfferings(
        User aUser, Semester semester)
    {
        NSArray<CourseOffering> result = null;
        String courseId = get(CONTEXT_LABEL);
        if (courseId != null && aUser != null)
        {
            log.debug("processing course label: " + courseId);
            courseId = courseId.replaceAll("[^A-Za-z0-9 ]", " ");
            log.debug("processing course label: " + courseId);
            courseId = courseId.replaceAll("([0-9]+) .*$", "$1");
            log.debug("processing course label: " + courseId);
            String courseNumber =
                courseId.replaceAll("^[^0-9]*([0-9]+).*$", "$1");
            log.debug("course number: " + courseNumber);
            try
            {
                int num = Integer.parseInt(courseNumber);
                // Could eventually add dept abbrev too
                result = CourseOffering.objectsMatchingQualifier(ec,
                    CourseOffering.course.dot(Course.number).is(num).and(
                    CourseOffering.course.dot(Course.department)
                    .dot(Department.institution)
                    .is(lmsInstance().authenticationDomain())).and(
                    CourseOffering.semester.is(semester)));

                // Filter to courses this user can manage
                NSMutableArray<CourseOffering> filtered =
                    new NSMutableArray<CourseOffering>();
                for (CourseOffering co : result)
                {
                    if (co.isInstructor(aUser) || aUser.hasAdminPrivileges())
                    {
                        filtered.add(co);
                    }
                }
                result = filtered;
            }
            catch (Exception e)
            {
                log.info("Unable to extract course number from '"
                    + courseId + "'", e);
            }
        }
        if (result == null)
        {
            result = new NSArray<CourseOffering>();
        }
        log.debug("suggestedCourseOfferings() = " + result);
        return result;
    }


    // ----------------------------------------------------------
    public WORequest rawRequest()
    {
        return request;
    }


    //~ Instance/static fields ................................................

    private WOContext context;
    private WORequest request;
    protected WCEC ec;

    private LMSInstance lmsInstance;
    private User user;


    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static OAuthValidator VALIDATOR = new SimpleOAuthValidator();

    static Logger log = Logger.getLogger(LTILaunchRequest.class);
}
