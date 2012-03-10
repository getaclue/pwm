/*
 * Password Management Servlets (PWM)
 * http://code.google.com/p/pwm/
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2012 The PWM Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package password.pwm.ws.server.rest;

import com.google.gson.Gson;
import com.novell.ldapchai.ChaiFactory;
import com.novell.ldapchai.ChaiUser;
import password.pwm.ContextManager;
import password.pwm.PwmApplication;
import password.pwm.PwmSession;
import password.pwm.error.PwmOperationalException;
import password.pwm.util.PwmLogger;
import password.pwm.util.operations.PasswordUtility;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/setpassword")
public class RestSetPasswordServer {
    private static final PwmLogger LOGGER = PwmLogger.getLogger(RestSetPasswordServer.class);

    @Context
    HttpServletRequest request;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String doPostSetPassword(
            final @FormParam("username") String username,
            final @FormParam("password") String password
    ) {
        try {
            final PwmSession pwmSession = PwmSession.getPwmSession(request);
            final PwmApplication pwmApplication = ContextManager.getPwmApplication(request);

            final Map<String,String> outputMap = new HashMap<String,String>();

            if (!pwmSession.getSessionStateBean().isAuthenticated()) {
                outputMap.put("success","false");
                outputMap.put("errorMsg","authentication required");
            } else {
                try {
                    if (username != null && username.length() > 0) {
                        final ChaiUser chaiUser = ChaiFactory.createChaiUser(username, pwmSession.getSessionManager().getChaiProvider());
                        PasswordUtility.helpdeskSetUserPassword(pwmSession, chaiUser, pwmApplication, password);
                    } else {
                        PasswordUtility.setUserPassword(pwmSession, pwmApplication, password);
                    }
                    outputMap.put("success","true");
                } catch (PwmOperationalException e) {
                    outputMap.put("success","false");
                    outputMap.put("errorCode", String.valueOf(e.getError().getErrorCode()));
                    outputMap.put("errorMsg", e.getErrorInformation().getDetailedErrorMsg());
                }
            }

            final Gson gson = new Gson();
            return gson.toJson(outputMap);
        } catch (Exception e) {
            LOGGER.error("unexpected error building json response for /randompassword rest service: " + e.getMessage());
        }

        return "";
    }
}