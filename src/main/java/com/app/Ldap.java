package com.app;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

public class Ldap {
    
    public static LDAPConnection connectToLDAPServer(String serverAddress, int port, String bindDn, String password) throws LDAPException {
        LDAPConnection ldapConnection = new LDAPConnection(serverAddress, port, bindDn, password);
        return ldapConnection;
        }

    public static com.unboundid.ldap.sdk.SearchResult searchLDAP(String username, LDAPConnection ldapConnection) {
        // Fix this to use bindDN provided by user
        String baseDN = "ou=people,dc=example,dc=com";
        String filter = "(uid=" + username + ")";
        try{
            com.unboundid.ldap.sdk.SearchResult searchResult = ldapConnection.search(baseDN, com.unboundid.ldap.sdk.SearchScope.SUB, filter, "*");
            System.out.println(searchResult.getSearchEntries().get(0));
            if(searchResult.getEntryCount() > 0){
                return searchResult;
            }
        } catch (LDAPException e){
            e.printStackTrace();
        }
        return null;
    }

}
