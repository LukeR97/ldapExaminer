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

    public static List<Attribute> analyzeLDAP(com.unboundid.ldap.sdk.SearchResultEntry attribs, LDAPConnection ldapConnection){
        List<Attribute> results = new ArrayList<>();
        //Attributes ---------------------------------------
        Integer uidNum = getIntegerValue(attribs.getAttributeValue("uidNumber"));
        String loginShell = getStringValue(attribs.getAttributeValue("loginShell"));
        String uidName = getStringValue(attribs.getAttributeValue("uid"));
        String homeDir = getStringValue(attribs.getAttributeValue("homeDirectory"));
        String expiryDate = getStringValue(attribs.getAttributeValue("passwordExpirationDate"));
        //---------------------------------------------------
        results.add(expiredPass(expiryDate));
        results.add(loginShell(loginShell));
        results.add(homeDirectory(uidName, homeDir));
        List<Attribute> results1 = checkUidNumber(uidNum, ldapConnection);
        results.addAll(results1);
        return results;
    }

    //Functions for if a given attribute doesn't exist
    private static Integer getIntegerValue(String value) {
        try {
            return(value != null) ? Integer.valueOf(value) : null;
        } catch(NumberFormatException e){
            return null;
        }
    }

    private static String getStringValue(String value) {
        return(value != null) ? value : "";
    }
    //-----------------------------------------------

    //ANALYSIS FUNCTIONS
    private static List<Attribute> checkUidNumber(Integer uidNumber, LDAPConnection ldapConnection){
        List<Attribute> uidNumResults = new ArrayList<>();
        try{
            com.unboundid.ldap.sdk.SearchResult searchResult = ldapConnection.search("ou=people,dc=example,dc=com", com.unboundid.ldap.sdk.SearchScope.SUB, "(uidNumber=" + uidNumber + ")", "uid");
            if(searchResult.getEntryCount() > 1){
                uidNumResults.add(new Attribute("Duplicate uid Numbers found!", "See Below"));
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
            return new Attribute("Home Directory doesn't match UID: ", homeDirectory);
        }
    }

    private static Attribute expiredPass(String expiryDate) {
    //     /* Calculate if password is expired
    //      * Logic here is that we get the current date and the expiry date from LDAP
    //      * They Need to be in the same format to compare
    //      * We also get the current date plus 10 days to give a warning if the password will expire soon
    //      */
        if (expiryDate.isEmpty()) {
            return new Attribute("Password Expiry Date", "No expiry date set");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime expiryDateTime = LocalDateTime.parse(expiryDate.split("\\.")[0], formatter);
        LocalDateTime tenDaysLater = currentDate.plusDays(10);
        if (currentDate.isAfter(expiryDateTime)) {
            return new Attribute("Password Expiry", "Expired Password");
        } else if (tenDaysLater.isAfter(expiryDateTime)) {
            return new Attribute("Password Expiry Date", "Password not expired, but will expire in less than 10 days");
        } else {
            return new Attribute("Password Expiry Date", "Password Not Expired");
        }
    }
}
