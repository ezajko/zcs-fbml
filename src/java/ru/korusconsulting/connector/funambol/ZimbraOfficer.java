package ru.korusconsulting.connector.funambol;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import ru.korusconsulting.connector.base.ZimbraPort;
import ru.korusconsulting.connector.exceptions.SoapRequestException;

import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Cred;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.security.AbstractOfficer;
import com.funambol.framework.server.Sync4jUser;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.beans.BeanInitializationException;
import com.funambol.framework.tools.beans.LazyInitBean;
import com.funambol.server.config.Configuration;

public class ZimbraOfficer extends AbstractOfficer implements Serializable, LazyInitBean{
    private String zimbraUrl;
    private boolean autoRegisterPrincipal;
    private static final String ROLE_USER = "sync_user";

    @Override
    public Sync4jUser authenticateUser(Cred credential) {
        String userName = null, password = null;
        Authentication auth = credential.getAuthentication();

        String userpwd = new String(Base64.decode(auth.getData()));

        int p = userpwd.indexOf(':');

        if (p == -1) {
            userName = userpwd;
            password = "";
        } else {
            userName = (p > 0) ? userpwd.substring(0, p) : "";
            password = (p == (userpwd.length() - 1)) ? "" :
                       userpwd.substring(p + 1);
        }
        
        
        Sync4jUser user = new Sync4jUser();
        user.setUsername(userName.toLowerCase());
        user.setPassword(password);
        user.setRoles(new String[] {ROLE_USER});
        
        
        try {
            ZimbraPort zimbraPort = new ZimbraPort(new URL(zimbraUrl));
            zimbraPort.requestAutorization(user);
            
//            Configuration.getConfiguration().getStore().read(arg0, arg1)
            
            
            
            return user;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (SoapRequestException e) {
            e.printStackTrace();
            return null;
        }
        
        
        
    }

    public void init() throws BeanInitializationException {
        // TODO Auto-generated method stub
        System.out.println("zimbraUrl="+zimbraUrl);
        System.out.println("autoRegisterPrincipal="+autoRegisterPrincipal);
        
    }

    public String getZimbraUrl() {
        return zimbraUrl;
    }

    public void setZimbraUrl(String zimbraUrl) {
        this.zimbraUrl = zimbraUrl;
    }

    public boolean isAutoRegisterPrincipal() {
        return autoRegisterPrincipal;
    }

    public void setAutoRegisterPrincipal(boolean autoRegisterPrincipal) {
        this.autoRegisterPrincipal = autoRegisterPrincipal;
    }
    
    
}
