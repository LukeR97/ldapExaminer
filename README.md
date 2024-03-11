ldapExaminer is a Java project in which a user account from a specified LDAP server is provided and performs a check on the most common problems associated with user accounts to help troubleshoot any user specific issues.

An example of this would be the user account uid=johndoe. A check would be performed to see if this users uidNumber is shared with any other user accounts, if the users group membership is configured correctly, if the users account password is expired or if the account is disabled.

This application assumes that your users exist in ou=people and that groups are in ou=groups
