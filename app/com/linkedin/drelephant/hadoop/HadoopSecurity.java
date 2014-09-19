package com.linkedin.drelephant.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;
import play.Play;

import java.io.IOException;
import java.security.PrivilegedAction;


public class HadoopSecurity {
  private static final Logger logger = Logger.getLogger(HadoopSecurity.class);

  private UserGroupInformation _loginUser = null;

  private String _keytabLocation;
  private String _keytabUser;
  private boolean _securityEnabled = false;

  public HadoopSecurity() throws IOException {
    Configuration conf = new Configuration();
    UserGroupInformation.setConfiguration(conf);
    _securityEnabled = UserGroupInformation.isSecurityEnabled();
    if (_securityEnabled) {
      _keytabLocation = Play.application().configuration().getString("keytab.location");
      _keytabUser = Play.application().configuration().getString("keytab.user");
      checkLogin();
    }
  }

  public UserGroupInformation getUGI() throws IOException {
    checkLogin();
    return _loginUser;
  }

  public void checkLogin() throws IOException {

    if (_loginUser == null) {
      logger.info("No login user. Creating login user");
      logger.info("Logging with " + _keytabUser + " and " + _keytabLocation);
      UserGroupInformation.loginUserFromKeytab(_keytabUser, _keytabLocation);
      _loginUser = UserGroupInformation.getLoginUser();
      logger.info("Logged in with user " + _loginUser);
    } else {
      _loginUser.checkTGTAndReloginFromKeytab();
    }

  }

  public <T> T doAs(PrivilegedAction<T> action) throws IOException {
    UserGroupInformation ugi = getUGI();
    if (ugi != null) {
      return ugi.doAs(action);
    }
    return null;
  }
}