package com.app;

import java.util.ArrayList;
import java.util.List;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime; 

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

    //NOTE
    // NEED TO MAKE THESE WORK REGARDLESS OF IF ATTRIBUTE EXISTS!!
    public static List<Attribute> analyzeLDAP(com.unboundid.ldap.sdk.SearchResultEntry attribs, LDAPConnection ldapConnection){
        List<Attribute> results = new ArrayList<>();
        Integer uidNum = Integer.valueOf(attribs.getAttributeValue("uidNumber"));
        String loginShell = attribs.getAttributeValue("loginShell");
        String uidName = attribs.getAttributeValue("uid");
        String homeDir = attribs.getAttributeValue("homeDirectory");
        String expiryDate = attribs.getAttributeValue("passwordExpirationDate");
        results.add(expiredPass(expiryDate));
        results.add(loginShell(loginShell));
        results.add(homeDirectory(uidName, homeDir));
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

    private static Attribute loginShell(String shell){
        if(shell.matches("/bin/bash")){
            return new Attribute("Login Shell", "OK");
        } else {
            return new Attribute("Login Shell", shell);
        }
    }

    private static Attribute homeDirectory(String uidName, String homeDirectory){
        if(homeDirectory.matches("/home/"+uidName)){
            return new Attribute("Home Directory", "OK");
        } else {
            return new Attribute("Home Directory", homeDirectory);
        }
    }

    // Update this to send a warning if close to expired
    private static Attribute expiredPass (String expiryDate){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        expiryDate = expiryDate.split("\\.")[0];
        Long expiryDateNum = Long.valueOf(expiryDate);
        LocalDateTime currentDate = LocalDateTime.now();
        String currentDateString = dtf.format(currentDate);
        Long currentDateInteger = Long.valueOf(currentDateString);

        if(currentDateInteger > expiryDateNum){
            return new Attribute("Password Expiry", "Expired Password");
        } else {
            return new Attribute("Password Expiry Date", "Password Not Expired");
        }
    }
}
