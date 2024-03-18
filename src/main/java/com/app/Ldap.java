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
        String uidNum = attribs.getAttributeValue("uidNumber");
        try{
            com.unboundid.ldap.sdk.SearchResult searchResult = ldapConnection.search("ou=people,dc=example,dc=com", com.unboundid.ldap.sdk.SearchScope.SUB, "(uidNumber=" + uidNum + ")", "uid");
            //System.out.println(searchResult.getSearchEntries().get(0));
            if(searchResult.getEntryCount() > 1){
                //results.add(searchResult.getSearchEntries().get(1).toString());
                for (SearchResultEntry entry: searchResult.getSearchEntries()){
                    results.add(entry.getAttribute("uid"));
                }
                return results;
            }
        } catch (LDAPException e){
            e.printStackTrace();
        }
        return null;
    }

}
