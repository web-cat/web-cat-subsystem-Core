/*==========================================================================*\
 |  Copyright (C) 2026 Virginia Tech
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

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.log4j.Logger;
import org.webcat.woextensions.WCFetchSpecification;

// -------------------------------------------------------------------------
/**
 * Represents a user-specific bearer access token for authenticating API requests,
 * Git operations, and MCP interactions.
 *
 * <p>Tokens are generated with 256 bits of cryptographic entropy and stored exclusively
 * as SHA-256 hashes. The raw token string is only returned once upon generation.</p>
 *
 * @author Stephen Edwards
 */
public class AccessToken
    extends _AccessToken
{
    //~ Constants .............................................................

    public static final String DEFAULT_PREFIX = "wcat_pat_";
    public static final int TOKEN_BYTE_LENGTH = 32;

    // Standard Scopes
    public static final String SCOPE_ALL       = "all";
    public static final String SCOPE_GIT_READ  = "git:read";
    public static final String SCOPE_GIT_WRITE = "git:write";
    public static final String SCOPE_API_READ  = "api:read";
    public static final String SCOPE_API_WRITE = "api:write";
    public static final String SCOPE_MCP_READ  = "mcp:read";
    public static final String SCOPE_MCP_WRITE = "mcp:write";

    private static final Logger log = Logger.getLogger(AccessToken.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new AccessToken object.
     */
    public AccessToken()
    {
        super();
    }


    //~ Inner Classes .........................................................

    // ----------------------------------------------------------
    /**
     * Container holding the newly created AccessToken entity and the
     * one-time raw token string to display to the user.
     */
    public static class GenerationResult
    {
        public final AccessToken accessToken;
        public final String rawToken;

        public GenerationResult(AccessToken accessToken, String rawToken)
        {
            this.accessToken = accessToken;
            this.rawToken = rawToken;
        }
    }


    //~ Public Static Methods .................................................

    // ----------------------------------------------------------
    /**
     * Generate a new personal access token for a user.
     *
     * @param ec The editing context in which to insert the new token
     * @param user The user owning the token
     * @param name A descriptive name (e.g. "VS Code Git", "Claude MCP")
     * @param scopes An array of granted scopes (e.g. SCOPE_GIT_READ)
     * @param expiresAt Optional expiration date (null for no expiration)
     * @return GenerationResult containing the AccessToken entity and raw token string
     */
    public static GenerationResult generateToken(
        EOEditingContext ec,
        User user,
        String name,
        NSArray<String> scopes,
        NSTimestamp expiresAt)
    {
        String secret = generateRandomHex(TOKEN_BYTE_LENGTH);
        String rawToken = DEFAULT_PREFIX + secret;
        String tokenHash = hashToken(rawToken);
        String prefix = rawToken.substring(0, Math.min(16, rawToken.length()));

        AccessToken token = (AccessToken) create(
            ec,
            new NSTimestamp(),
            false,
            name,
            tokenHash,
            prefix,
            user);

        if (scopes != null && scopes.count() > 0)
        {
            token.setScopes(scopes.componentsJoinedByString(" "));
        }
        token.setExpiresAt(expiresAt);

        return new GenerationResult(token, rawToken);
    }


    // ----------------------------------------------------------
    /**
     * Validate an incoming raw bearer token and return the associated user if valid.
     *
     * @param ec The editing context
     * @param rawToken The raw token string provided by the client
     * @param requiredScope The scope required for the requested action (null to bypass)
     * @param clientIp The remote IP address of the client for audit logging (or null)
     * @return The authenticated User if the token is valid, non-revoked, non-expired,
     *         and possesses the required scope; otherwise null.
     */
    public static User validateToken(
        EOEditingContext ec,
        String rawToken,
        String requiredScope,
        String clientIp)
    {
        AccessToken token = findToken(ec, rawToken);
        if (token != null && token.isValid() && token.hasScope(requiredScope))
        {
            token.recordUse(clientIp);
            return token.user();
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Look up an AccessToken record by its raw token string.
     *
     * @param ec The editing context
     * @param rawToken The raw token string
     * @return The matching AccessToken, or null if not found
     */
    public static AccessToken findToken(EOEditingContext ec, String rawToken)
    {
        if (rawToken == null)
        {
            return null;
        }
        String trimmed = rawToken.trim();
        if (trimmed.isEmpty())
        {
            return null;
        }

        String hash = hashToken(trimmed);

        WCFetchSpecification<AccessToken> fspec =
            new WCFetchSpecification<AccessToken>(
                ENTITY_NAME,
                tokenHash.eq(hash),
                null);
        fspec.setFetchLimit(1);
        fspec.setRefreshesRefetchedObjects(true);

        NSArray<AccessToken> matches =
            AccessToken.objectsWithFetchSpecification(ec, fspec);

        return (matches.count() > 0) ? matches.objectAtIndex(0) : null;
    }


    // ----------------------------------------------------------
    /**
     * Computes the SHA-256 hash of a raw token string as a lowercase hex string.
     *
     * @param rawToken The raw token to hash
     * @return Hex-encoded SHA-256 hash
     */
    public static String hashToken(String rawToken)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }


    //~ Public Instance Methods ...............................................

    // ----------------------------------------------------------
    /**
     * Check whether this token is active (not revoked and not expired).
     *
     * @return true if valid
     */
    public boolean isValid()
    {
        return !isRevoked() && !isExpired();
    }


    // ----------------------------------------------------------
    /**
     * Check whether this token has expired.
     *
     * @return true if expired
     */
    public boolean isExpired()
    {
        NSTimestamp exp = expiresAt();
        return exp != null && exp.before(new NSTimestamp());
    }


    // ----------------------------------------------------------
    /**
     * Revoke this access token immediately.
     */
    public void revoke()
    {
        setIsRevoked(true);
    }


    // ----------------------------------------------------------
    /**
     * Check whether this token grants a specific scope.
     *
     * @param requiredScope The scope to check (e.g. SCOPE_GIT_READ)
     * @return true if granted, or if requiredScope is null or empty
     */
    public boolean hasScope(String requiredScope)
    {
        if (requiredScope == null || requiredScope.trim().isEmpty())
        {
            return true;
        }

        String scopesStr = scopes();
        if (scopesStr == null || scopesStr.trim().isEmpty())
        {
            return false;
        }

        String[] parts = scopesStr.trim().split("\\s+");
        for (String s : parts)
        {
            if (s.equals(SCOPE_ALL) || s.equals("*") || s.equalsIgnoreCase(requiredScope))
            {
                return true;
            }
            if (s.endsWith(":*"))
            {
                String prefix = s.substring(0, s.length() - 1);
                if (requiredScope.startsWith(prefix))
                {
                    return true;
                }
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Record a successful authentication use of this token.
     *
     * @param ipAddress The remote IP address (or null)
     */
    public void recordUse(String ipAddress)
    {
        setLastUsedAt(new NSTimestamp());
        if (ipAddress != null && !ipAddress.trim().isEmpty())
        {
            setLastUsedIp(ipAddress.trim());
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns a masked display string (e.g. "wcat_pat_3f8a4b...****").
     *
     * @return masked token for UI display
     */
    public String maskedToken()
    {
        String p = tokenPrefix();
        return (p != null ? p : DEFAULT_PREFIX) + "...****";
    }


    // ----------------------------------------------------------
    /**
     * Returns the scopes as a list of strings.
     *
     * @return NSArray of granted scope strings
     */
    public NSArray<String> scopeList()
    {
        String s = scopes();
        if (s == null || s.trim().isEmpty())
        {
            return NSArray.emptyArray();
        }
        return new NSArray<String>(s.trim().split("\\s+"));
    }


    //~ Private Helper Methods ................................................

    // ----------------------------------------------------------
    private static String generateRandomHex(int byteLength)
    {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return toHex(bytes);
    }


    // ----------------------------------------------------------
    private static String toHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
