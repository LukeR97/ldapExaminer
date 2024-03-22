package com.app;

import java.util.ArrayList;
import java.util.List;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;

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

    public static List<Attribute> analyzeLDAP(com.unboundid.ldap.sdk.SearchResultEntry attribs, LDAPConnection ldapConnection){
        List<Attribute> results = new ArrayList<>();
        Integer uidNum = Integer.valueOf(attribs.getAttributeValue("uidNumber"));
        String loginShell = attribs.getAttributeValue("loginShell");
        if(loginShell.matches("/bin/bash")){
            Attribute shell = new Attribute("Login Shell", "OK");
            results.add(shell);
        } else {
            Attribute shell = new Attribute("Login Shell", loginShell);
            results.add(shell);
        }
        List<Attribute> results1 = checkUidNumber(uidNum, ldapConnection);
        results.addAll(results1);
        return results;
    }

    //ANALYSIS FUNCTIONS
    private static List<Attribute> checkUidNumber(Integer uidNumber, LDAPConnection ldapConnection){
        List<Attribute> uidNumResults = new ArrayList<>();
        try{
            com.unboundid.ldap.sdk.SearchResult searchResult = ldapConnection.search("ou=people,dc=example,dc=com", com.unboundid.ldap.sdk.SearchScope.SUB, "(uidNumber=" + uidNumber + ")", "uid");
            if(searchResult.getEntryCount() > 1){
                for (SearchResultEntry entry: searchResult.getSearchEntries()){
                    uidNumResults.add(entry.getAttribute("uid"));
                }
                return uidNumResults;
            }
        } catch (LDAPException e){
            e.printStackTrace();
        }
        uidNumResults.add(new Attribute("Duplicate UID's", "None"));
        return uidNumResults;
    }
}
